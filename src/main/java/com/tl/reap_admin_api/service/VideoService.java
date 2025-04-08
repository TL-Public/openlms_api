package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.VideoDao;
import com.tl.reap_admin_api.dao.ChapterDao;
import com.tl.reap_admin_api.dto.LanguageCountDto;
import com.tl.reap_admin_api.dto.VideoDto;
import com.tl.reap_admin_api.model.Video;
import com.tl.reap_admin_api.model.Chapter;
import com.tl.reap_admin_api.model.ChapterVideo;
import com.tl.reap_admin_api.model.Playlist;
import com.tl.reap_admin_api.exception.VideoNotFoundException;
import com.tl.reap_admin_api.exception.ChapterNotFoundException;
import com.tl.reap_admin_api.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Service
public class VideoService {

    private final VideoDao videoDao;
    private final ChapterDao chapterDao;
    private final VideoMapper videoMapper;
    private final KPointService kPointService;

    @Autowired
    public VideoService(VideoDao videoDao, ChapterDao chapterDao, VideoMapper videoMapper, KPointService kPointService) {
        this.videoDao = videoDao;
        this.chapterDao = chapterDao;
        this.videoMapper = videoMapper;
        this.kPointService = kPointService;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public VideoDto createVideo(VideoDto videoDto) {
        Video video = videoMapper.toEntity(videoDto);
        video.setUuid(UUID.randomUUID());
        video.setCreatedAt(ZonedDateTime.now());
        video.setUpdatedAt(ZonedDateTime.now());
        video.setCreatedBy("system"); // Replace with actual user when authentication is implemented
        video.setUpdatedBy("system"); // Replace with actual user when authentication is implemented

        // Set chapter videos
        if (videoDto.getChapters() != null) {
            videoDto.getChapters().forEach(chapterDto -> {
                Chapter chapter = chapterDao.findByUuid(chapterDto.getUuid())
                        .orElseThrow(() -> new RuntimeException("Chapter not found"));
                chapterDto.getChapterVideos().forEach(chapterVideoDto -> {
                    Video existingVideo = videoDao.findByUuid(chapterVideoDto.getVideo().getUuid())
                            .orElseThrow(() -> new RuntimeException("Video not found"));
                    chapter.addVideo(existingVideo, chapterVideoDto.getOrderNumber());
                });
            });
        }

        Video savedVideo = videoDao.save(video);
        return videoMapper.toDto(savedVideo);
    }

    @Transactional(readOnly = true)
    public VideoDto getVideoByUuid(UUID uuid) {
        Video video = videoDao.findByUuid(uuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + uuid));
        return videoMapper.toDto(video);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllVideos(String courseName, String courseCode, String videoTitle, UUID courseUuid, Pageable pageable) {
        // Get videos with pagination as before
        List<Video> videos = videoDao.findAllFiltered(courseName, courseCode, videoTitle, courseUuid, pageable);
        Long total = videoDao.countAllFiltered(courseName, courseCode, videoTitle, courseUuid);
        
        List<VideoDto> videoDtos = videos.stream()
            .map(videoMapper::toDto)
            .collect(Collectors.toList());
        
        // Get language counts
        List<Object[]> languageCountsData = videoDao.countVideosByLanguage(courseName, courseCode, videoTitle, courseUuid);
        List<LanguageCountDto> languageCounts = languageCountsData.stream()
            .map(result -> new LanguageCountDto(
                result[0] != null ? (String) result[0] : "unknown", 
                (Long) result[1]))
            .collect(Collectors.toList());
        
        // Create response with existing structure plus language counts
        Map<String, Object> response = new HashMap<>();
        response.put("content", videoDtos);
        
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("size", pageable.getPageSize());
        pageInfo.put("number", pageable.getPageNumber());
        pageInfo.put("totalElements", total);
        pageInfo.put("totalPages", (int) Math.ceil((double) total / pageable.getPageSize()));
        response.put("page", pageInfo);
        
        // Add language counts
        response.put("languageCounts", languageCounts);
        
        return response;
    }

    @Transactional(readOnly = true)    
    public Page<VideoDto> getAllVideos(Pageable pageable) {
        Page<Video> videoPage = videoDao.findAllPaged(pageable);
        return videoPage.map(videoMapper::toDto);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public VideoDto updateVideo(UUID uuid, VideoDto videoDto) {
        Video existingVideo = videoDao.findByUuid(uuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + uuid));

        // Update the existing video entity with provided values
        videoMapper.updateEntityFromDto(existingVideo, videoDto);
        
        // Set the updated timestamp and user
        existingVideo.setUpdatedAt(ZonedDateTime.now());
        existingVideo.setUpdatedBy("system"); // Replace with actual user when authentication is implemented

        // Update chapters if provided
        if (videoDto.getChapters() != null && !videoDto.getChapters().isEmpty()) {
            // Keep the existing chapter videos if chapters are provided
            // This ensures we don't lose the relationship data
            Set<ChapterVideo> updatedChapterVideos = videoDto.getChapters().stream()
                    .map(chapterDto -> chapterDao.findByUuid(chapterDto.getUuid())
                            .orElseThrow(() -> new RuntimeException("Chapter not found with uuid: " + chapterDto.getUuid())))
                    .flatMap(chapter -> chapter.getChapterVideos().stream()
                            .filter(chapterVideo -> chapterVideo.getVideo().getUuid().equals(existingVideo.getUuid())))
                    .collect(Collectors.toSet());
            
            if (!updatedChapterVideos.isEmpty()) {
                existingVideo.setChapterVideos(updatedChapterVideos);
            }
        }

        Video savedVideo = videoDao.save(existingVideo);
        return videoMapper.toDto(savedVideo);
    }
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public void deleteVideo(UUID uuid) {
        Video video = videoDao.findByUuid(uuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + uuid));

        for (Playlist playlist : new HashSet<>(video.getPlaylists())) {
            playlist.getVideos().remove(video);
        }
        video.getPlaylists().clear();
        
        video.setUpdatedAt(ZonedDateTime.now());
        video.setUpdatedBy("system");
        videoDao.delete(video);
    }



    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public VideoDto addVideoToChapter(UUID videoUuid, UUID chapterUuid) {
        Video video = videoDao.findByUuid(videoUuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + videoUuid));
        Chapter chapter = chapterDao.findByUuid(chapterUuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));
        chapter.setUpdatedAt(ZonedDateTime.now());
        chapter.setUpdatedBy("system");

        video.addChapter(chapter);
        Video savedVideo = videoDao.save(video);
        return videoMapper.toDto(savedVideo);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public VideoDto removeVideoFromChapter(UUID videoUuid, UUID chapterUuid) {
        Video video = videoDao.findByUuid(videoUuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + videoUuid));
        Chapter chapter = chapterDao.findByUuid(chapterUuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));
        chapter.setUpdatedAt(ZonedDateTime.now());
        chapter.setUpdatedBy("system");

        video.removeChapter(chapter);
        Video savedVideo = videoDao.save(video);
        return videoMapper.toDto(savedVideo);
    }

    @Transactional(readOnly = true)
    public List<VideoDto> getVideosByChapter(UUID chapterUuid) {
        Chapter chapter = chapterDao.findByUuid(chapterUuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));
        return chapter.getChapterVideos().stream()
                .map(ChapterVideo::getVideo)
                .map(videoMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    
}

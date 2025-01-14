package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.VideoDao;
import com.tl.reap_admin_api.dao.ChapterDao;
import com.tl.reap_admin_api.dto.VideoDto;
import com.tl.reap_admin_api.model.Video;
import com.tl.reap_admin_api.model.Chapter;
import com.tl.reap_admin_api.model.ChapterVideo;
import com.tl.reap_admin_api.exception.VideoNotFoundException;
import com.tl.reap_admin_api.exception.ChapterNotFoundException;
import com.tl.reap_admin_api.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
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
    public Page<VideoDto> getAllVideos(String courseName, String courseCode, String videoTitle, UUID courseUuid, Pageable pageable) {
        List<Video> videos = videoDao.findAllFiltered(courseName, courseCode, videoTitle, courseUuid, pageable);
        Long total = videoDao.countAllFiltered(courseName, courseCode, videoTitle, courseUuid);
        
        List<VideoDto> videoDtos = videos.stream()
            .map(videoMapper::toDto)
            .collect(Collectors.toList());
        
        return new PageImpl<>(videoDtos, pageable, total);
    }

    @Transactional(readOnly = true)    
    public Page<VideoDto> getAllVideos(Pageable pageable) {
        Page<Video> videoPage = videoDao.findAllPaged(pageable);
        return videoPage.map(videoMapper::toDto);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public VideoDto updateVideo(UUID uuid, VideoDto videoDto) {
        Video existingVideo = videoDao.findByUuid(uuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + uuid));

        Video updatedVideo = videoMapper.toEntity(videoDto);
        updatedVideo.setId(existingVideo.getId());
        updatedVideo.setUuid(existingVideo.getUuid());
        updatedVideo.setCreatedAt(existingVideo.getCreatedAt());
        updatedVideo.setCreatedBy(existingVideo.getCreatedBy());
        updatedVideo.setUpdatedAt(ZonedDateTime.now());
        updatedVideo.setUpdatedBy("system"); // Replace with actual user when authentication is implemented

         // Update chapters
         if (videoDto.getChapters() != null) {
            existingVideo.setChapterVideos(videoDto.getChapters().stream()
                    .map(chapterDto -> chapterDao.findByUuid(chapterDto.getUuid())
                            .orElseThrow(() -> new RuntimeException("Chapter not found")))
                    .flatMap(chapter -> chapter.getChapterVideos().stream()
                            .filter(chapterVideo -> chapterVideo.getVideo().getUuid().equals(existingVideo.getUuid())))
                    .collect(Collectors.toSet()));
        }

        Video savedVideo = videoDao.save(updatedVideo);
        return videoMapper.toDto(savedVideo);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void deleteVideo(UUID uuid) {
        Video video = videoDao.findByUuid(uuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + uuid));
        videoDao.delete(video);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void deleteAllKPointPlaylist() {
        kPointService.deleteAllPlaylists();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void deleteAllKPointVideos() {
        kPointService.deleteAllVideos();
    } 

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public VideoDto addVideoToChapter(UUID videoUuid, UUID chapterUuid) {
        Video video = videoDao.findByUuid(videoUuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + videoUuid));
        Chapter chapter = chapterDao.findByUuid(chapterUuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));

        video.addChapter(chapter);
        Video savedVideo = videoDao.save(video);
        return videoMapper.toDto(savedVideo);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public VideoDto removeVideoFromChapter(UUID videoUuid, UUID chapterUuid) {
        Video video = videoDao.findByUuid(videoUuid)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with uuid: " + videoUuid));
        Chapter chapter = chapterDao.findByUuid(chapterUuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));

        video.removeChapter(chapter);
        Video savedVideo = videoDao.save(video);
        return videoMapper.toDto(savedVideo);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF', 'TRAINER', 'TRAINEE')")
    public List<VideoDto> getVideosByChapter(UUID chapterUuid) {
        Chapter chapter = chapterDao.findByUuid(chapterUuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + chapterUuid));
        return chapter.getChapterVideos().stream()
                .map(ChapterVideo::getVideo)
                .map(videoMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    
}

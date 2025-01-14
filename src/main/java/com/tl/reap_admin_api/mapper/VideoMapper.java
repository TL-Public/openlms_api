package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.ChapterDto;
import com.tl.reap_admin_api.dto.ChapterTranslationDto;
import com.tl.reap_admin_api.dto.ChapterVideoDto;
import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.VideoDto;
import com.tl.reap_admin_api.exception.LanguageNotFoundException;
import com.tl.reap_admin_api.model.Video;
import com.tl.reap_admin_api.model.Chapter;
import com.tl.reap_admin_api.model.ChapterTranslation;
import com.tl.reap_admin_api.model.ChapterVideo;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.CourseTranslation;
import com.tl.reap_admin_api.dto.CourseTranslationDto;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.service.LanguageService;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class VideoMapper {
    private final LanguageService languageService;
    
    @Autowired
    public VideoMapper(LanguageService languageService) {
        this.languageService = languageService;      
    }

    public VideoDto toDto(Video video) {
        if (video == null) {
            return null;
        }

        VideoDto dto = new VideoDto();        
        dto.setUuid(video.getUuid());
        dto.setName(video.getName());
        dto.setDescription(video.getDescription());
        dto.setThumbnail(video.getThumbnail());
        dto.setDuration(video.getDuration());
        dto.setExtId(video.getExtId());
        dto.setUrl(video.getUrl());
        dto.setStatus(video.getStatus());
        dto.setLanguageCode(video.getLanguage() != null ? video.getLanguage().getCode() : null);


        dto.setChapters(new ArrayList<>());
        dto.setCourses(new ArrayList<>());

        
        dto.setChapterVideos(video.getChapterVideos().stream()
                .map(chapterVideo -> {
                    ChapterVideoDto chapterVideoDto = new ChapterVideoDto();
                    chapterVideoDto.setChapterUuid(chapterVideo.getChapter().getUuid());
                    chapterVideoDto.setVideoUuid(dto.getUuid());
                    chapterVideoDto.setOrderNumber(chapterVideo.getOrderNumber());                    
                    return chapterVideoDto;
                })
                .collect(Collectors.toList()));

        for (ChapterVideo chapterVideo : video.getChapterVideos()) {            
            dto.getChapters().add(chapterToSimpleDto(chapterVideo.getChapter()));
            
            
            Course course = chapterVideo.getChapter().getCourse();
            if (course != null) {
                CourseDto courseDto = courseToSimpleDto(course);
                
                if (!dto.getCourses().contains(courseDto)) {
                    dto.getCourses().add(courseDto);
                }
            }
        }

        
        
        dto.setStatus(video.getStatus());
        System.out.println("\nvideo.getStatus() - "+ video.getStatus());
        return dto;
    }

    public Video toEntity(VideoDto dto) {
        if (dto == null) {
            return null;
        }

        Video video = new Video();
        video.setUuid(dto.getUuid());
        video.setName(dto.getName());
        video.setDescription(dto.getDescription());
        video.setThumbnail(dto.getThumbnail());
        video.setDuration(dto.getDuration());
        video.setExtId(dto.getExtId());
        video.setUrl(dto.getUrl());
        Language language = languageService.getLanguageByCode(dto.getLanguageCode());
        if (language == null) {
            throw new LanguageNotFoundException("Language not found with code: " + dto.getLanguageCode());
        }
        video.setLanguage(language);
        video.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());

        // Note: Chapters should be set in the service layer

        return video;
    }

    public Video updateEntityFromDto(Video video, VideoDto dto) {
        if (dto == null) {
            return null;
        }

        if(dto.getName() != null) video.setName(dto.getName());
        if(dto.getDescription() != null) video.setDescription(dto.getDescription());
        if(dto.getThumbnail() != null) video.setThumbnail(dto.getThumbnail());
        if(dto.getDuration() != null) video.setDuration(dto.getDuration());
        if (dto.getStatus() != null) {
            video.setStatus(dto.getStatus());
        }
        
        if(dto.getLanguageCode() != null)  {
            Language language = languageService.getLanguageByCode(dto.getLanguageCode());
            if (language == null) {
                throw new LanguageNotFoundException("Language not found with code: " + dto.getLanguageCode());
            }
            video.setLanguage(language);
        }
        video.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());

        // Note: Chapters should be set in the service layer

        return video;
    }


    private ChapterDto chapterToSimpleDto(Chapter chapter) {
        ChapterDto dto = new ChapterDto();
        dto.setUuid(chapter.getUuid()); 
        dto.setOrderNumber(chapter.getOrderNumber());
        dto.setNumberOfVideos(chapter.getChapterVideos().size());
        dto.setCourseUuid(chapter.getCourse().getUuid());
        if (chapter.getTranslations() != null) {
            Set<ChapterTranslationDto> translationDtos = new HashSet<>();
            for (ChapterTranslation translation : chapter.getTranslations()) {
                ChapterTranslationDto translationDto = new ChapterTranslationDto();
                translationDto.setLanguageCode(translation.getLanguage().getCode());
                translationDto.setTitle(translation.getTitle());
                translationDto.setDescription(translation.getDescription());
                translationDtos.add(translationDto);
            }
            dto.setTranslations(new ArrayList<>(translationDtos));
        }
        // Do not include videos here to break the circular reference
        return dto;
    }

    private CourseDto courseToSimpleDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setUuid(course.getUuid()); 
        dto.setCategory(course.getCategory());
        dto.setCourseCode(course.getCourseCode());
        dto.setImageUrl(course.getImageUrl());
                
        if (course.getTranslations() != null) {
            Set<CourseTranslationDto> translationDtos = new HashSet<>();
            for (CourseTranslation translation : course.getTranslations()) {
                CourseTranslationDto translationDto = new CourseTranslationDto();
                translationDto.setLanguageCode(translation.getLanguage().getCode());
                translationDto.setTitle(translation.getTitle());
                translationDto.setDescription(translation.getDescription());
                translationDtos.add(translationDto);
            }
            dto.setTranslations(new ArrayList<>(translationDtos));
        }

        // Do not include chapters here to break the circular reference
        return dto;
    }

    public VideoDto toSimpleDto(Video video) {
    
        VideoDto dto = new VideoDto();
        dto.setUuid(video.getUuid());
        dto.setName(video.getName());
        dto.setDescription(video.getDescription());
        dto.setThumbnail(video.getThumbnail());
        dto.setDuration(video.getDuration());
        dto.setExtId(video.getExtId());
        dto.setUrl(video.getUrl());
        dto.setLanguageCode(video.getLanguage().getCode());
        return dto;
    }

    public Set<VideoDto> toVideoDtoSet(Set<Video> videos) {
        return videos.stream()
                .map(video -> {
                    VideoDto dto = new VideoDto();
                    dto.setUuid(video.getUuid());
                    dto.setName(video.getName());
                    dto.setDescription(video.getDescription());
                    dto.setUrl(video.getUrl());
                    dto.setThumbnail(video.getThumbnail());
                    dto.setDuration(video.getDuration());
                    dto.setExtId(video.getExtId());                    
                    dto.setLanguageCode(video.getLanguage().getCode());
                    return dto;
                })
                .collect(Collectors.toSet());
    }
}

package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.ChapterDto;
import com.tl.reap_admin_api.model.Chapter;
import com.tl.reap_admin_api.model.ChapterTranslation;
import com.tl.reap_admin_api.model.ChapterVideo;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.service.LanguageService;
import com.tl.reap_admin_api.dto.ChapterTranslationDto;
import com.tl.reap_admin_api.dto.ChapterVideoDto;
import com.tl.reap_admin_api.dto.VideoDto;
import com.tl.reap_admin_api.exception.LanguageNotFoundException;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;



@Component
public class ChapterMapper {
    private final LanguageService languageService;
    private final VideoMapper videoMapper;

    @Autowired
    public ChapterMapper(LanguageService languageService, VideoMapper videoMapper) {
        this.languageService = languageService;
        this.videoMapper = videoMapper;
    }

    
    public ChapterDto toDto(Chapter chapter) {
        if (chapter == null) {
            return null;
        }

  /*      ChapterTranslation defaultTranslation = chapter.getTranslations().stream()
                .findFirst()
                .orElse(null);*/

        ChapterDto dto = new ChapterDto();
        dto.setUuid(chapter.getUuid());
        dto.setCourseUuid(chapter.getCourse().getUuid());
        dto.setOrderNumber(chapter.getOrderNumber());
        dto.setStatus(chapter.getStatus());
        dto.setTranslations(chapter.getTranslations().stream()
                 .map(this::toTranslationDto)
                 .collect(Collectors.toList()));
                 
       /*  dto.setChapterVideos(chapter.getChapterVideos().stream()
                 .map(this::toChapterVideoDto)
                 .collect(Collectors.toSet()));*/
        
        dto.setVideos(chapter.getChapterVideos().stream()
                 .map(this::toVideoDto)
                 .collect(Collectors.toSet()));
        return dto;
    }

    public ChapterDto toSimpleDto(Chapter chapter) {
        ChapterDto dto = new ChapterDto();
        dto.setUuid(chapter.getUuid()); 
        dto.setOrderNumber(chapter.getOrderNumber());
       
        dto.setVideos(chapter.getChapterVideos().stream()
                 .map(this::toVideoDto)
                 .collect(Collectors.toSet()));
                 
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

    public Chapter toEntity(ChapterDto dto) {
        if (dto == null) {
            return null;
        }

        Chapter chapter = new Chapter();
        chapter.setUuid(dto.getUuid());
        chapter.setOrderNumber(dto.getOrderNumber());

        if (dto.getTranslations() != null) {
            chapter.setTranslations(dto.getTranslations().stream()
                    .map(translationDto -> {
                        ChapterTranslation translation = toTranslationEntity(translationDto);
                        translation.setChapter(chapter);
                        return translation;
                    })
                    .collect(Collectors.toSet()));
        }

         if (dto.getChapterVideos() != null) {
            chapter.setChapterVideos(dto.getChapterVideos().stream()
                    .map(chapterVideoDto -> {
                        ChapterVideo chapterVideo = new ChapterVideo();
                        chapterVideo.setChapter(chapter);
                        chapterVideo.setVideo(videoMapper.toEntity(chapterVideoDto.getVideo()));
                        chapterVideo.setOrderNumber(chapterVideoDto.getOrderNumber());
                        return chapterVideo;
                    })
                    .collect(Collectors.toSet()));
        }
        return chapter;
    }

    public ChapterTranslationDto toTranslationDto(ChapterTranslation translation) {
        ChapterTranslationDto dto = new ChapterTranslationDto();
        dto.setId(translation.getId());
        dto.setTitle(translation.getTitle());
        dto.setDescription(translation.getDescription());
        dto.setLanguageCode(translation.getLanguage().getCode());       
        return dto;
    }

    private ChapterTranslation toTranslationEntity(ChapterTranslationDto dto) {
        ChapterTranslation translation = new ChapterTranslation();
        translation.setId(dto.getId());
        translation.setTitle(dto.getTitle());
        translation.setDescription(dto.getDescription());  

        Language language = languageService.getLanguageByCode(dto.getLanguageCode());
        if (language == null) {
            throw new LanguageNotFoundException("Language not found with code: " + dto.getLanguageCode());
        }
        translation.setLanguage(language);
        return translation;
    }

    private ChapterVideoDto toChapterVideoDto(ChapterVideo chapterVideo) {
        ChapterVideoDto dto = new ChapterVideoDto();
        dto.setVideo(videoMapper.toSimpleDto(chapterVideo.getVideo()));
        dto.setChapterUuid(chapterVideo.getChapter().getUuid());
        System.out.println("\ntoChapterVideoDto Ordernumeber : " + chapterVideo.getOrderNumber() +" \n");
        System.out.println("\ntoChapterVideoDto Video id : " + chapterVideo.getVideo().getId() +" \n");
        dto.setOrderNumber(chapterVideo.getOrderNumber());
        return dto;
    }

    private VideoDto toVideoDto(ChapterVideo chapterVideo) {
        VideoDto dto = new VideoDto();
        dto.setUuid(chapterVideo.getVideo().getUuid());
        dto.setName(chapterVideo.getVideo().getName());
        dto.setDescription(chapterVideo.getVideo().getDescription());
        dto.setThumbnail(chapterVideo.getVideo().getThumbnail());
        dto.setDuration(chapterVideo.getVideo().getDuration());
        dto.setExtId(chapterVideo.getVideo().getExtId());
        dto.setUrl(chapterVideo.getVideo().getUrl());
        dto.setLanguageCode(chapterVideo.getVideo().getLanguage().getCode());
        dto.setStatus(chapterVideo.getVideo().getStatus());
        dto.setOrderNumber(chapterVideo.getOrderNumber());
        return dto;
    }

    
}
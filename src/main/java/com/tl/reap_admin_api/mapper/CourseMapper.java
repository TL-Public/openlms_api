package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.ChapterDto;
import com.tl.reap_admin_api.model.Chapter;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.dto.CourseDto;

import com.tl.reap_admin_api.model.CourseTranslation;
import com.tl.reap_admin_api.dto.CourseTranslationDto;
import com.tl.reap_admin_api.exception.LanguageNotFoundException;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.service.LanguageService;
import com.tl.reap_admin_api.util.TranslationUtil;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    private final ChapterMapper chapterMapper;
    private final LanguageService languageService;

    @Autowired
    public CourseMapper(ChapterMapper chapterMapper, LanguageService languageService) {
        this.chapterMapper = chapterMapper;
        this.languageService = languageService;
    }
    
    public CourseDto toDto(Course course) {
        if (course == null) {
            return null;
        }

       /*  CourseTranslation defaultTranslation = course.getTranslations().stream()
                .findFirst()
                .orElse(null); */

        CourseDto dto = new CourseDto();
        dto.setUuid(course.getUuid());
        dto.setCourseCode(course.getCourseCode());
        dto.setDuration(course.getDuration());
        dto.setImageUrl(course.getImageUrl());
        dto.setNumberOfChapters(course.getChapters().size());
        dto.setNumberOfVideos(course.getChapters().stream().mapToInt(Chapter::getNumberOfVideos2).sum());
        dto.setCategory(course.getCategory());
        dto.setStatus(course.getStatus());
        dto.setDisplayCourseCode(course.getDisplayCourseCode());

       List<CourseTranslationDto> translationDtos = course.getTranslations().stream()
                .map(this::toTranslationDto)
                .collect(Collectors.toList());
        //dto.setTranslations(translationDtos);
        dto.setTranslations(TranslationUtil.addEnglishFallbackForHindi(
            translationDtos,
            "getLanguageCode",
            "setLanguageCode"
        ));


        List<ChapterDto> chapterDtos = course.getChapters().stream()
                .map(chapterMapper::toDto)
                .collect(Collectors.toList());        
      
        dto.setChapters(chapterDtos);

        dto.getChapters().forEach(chapterDto -> {
            chapterDto.setTranslations(TranslationUtil.addEnglishFallbackForHindi(
                chapterDto.getTranslations(),
                "getLanguageCode",
                "setLanguageCode"
            ));
        });
        
        return dto;
    }

    public Course toEntity(CourseDto dto) {
        if (dto == null) {
            return null;
        }

        Course course = new Course();
        course.setUuid(dto.getUuid());
        course.setCourseCode(dto.getCourseCode());
        course.setDuration(dto.getDuration());
        course.setCategory(dto.getCategory());
        course.setImageUrl(dto.getImageUrl());
        course.setDisplayCourseCode(dto.getDisplayCourseCode());

        if (dto.getTranslations() != null) {
            course.setTranslations(dto.getTranslations().stream()
                    .map(translationDto -> {
                        CourseTranslation translation = toTranslationEntity(translationDto);
                        translation.setCourse(course);
                        return translation;
                    })
                    .collect(Collectors.toSet()));
        }

        if (dto.getChapters() != null) {
            course.setChapters(dto.getChapters().stream()
                    .map(chapterDto -> {
                        Chapter chapter = chapterMapper.toEntity(chapterDto);
                        chapter.setCourse(course);
                        return chapter;
                    })
                    .collect(Collectors.toSet()));
        }

        return course;
    }

     private CourseTranslationDto toTranslationDto(CourseTranslation translation) {
        CourseTranslationDto dto = new CourseTranslationDto();
        dto.setId(translation.getId());
        dto.setTitle(translation.getTitle());
        dto.setDescription(translation.getDescription());
        dto.setAboutVideoUrl(translation.getAboutVideoUrl());
        dto.setAboutVideoExtid(translation.getAboutVideoExtid());
        dto.setLanguageCode(translation.getLanguage().getCode());
        return dto;
    }

    private CourseTranslation toTranslationEntity(CourseTranslationDto dto) {
        CourseTranslation translation = new CourseTranslation();
        translation.setId(dto.getId());
        translation.setTitle(dto.getTitle());
        translation.setDescription(dto.getDescription());
        translation.setAboutVideoUrl(dto.getAboutVideoUrl());
        translation.setAboutVideoExtid(dto.getAboutVideoExtid());
        
        Language language = languageService.getLanguageByCode(dto.getLanguageCode());
        if (language == null) {
            throw new LanguageNotFoundException("Language not found with code: " + dto.getLanguageCode());
        }
        translation.setLanguage(language);


        return translation;
    }

    public List<CourseDto> toDtoList(List<Course> courses) {
        return courses.stream()
                .map(course -> {
                    CourseDto dto = new CourseDto();
                    dto.setUuid(course.getUuid());
                    dto.setCourseCode(course.getCourseCode());
                    dto.setDuration(course.getDuration());
                    dto.setImageUrl(course.getImageUrl());
                    dto.setCategory(course.getCategory());
                    dto.setStatus(course.getStatus());
                    dto.setDisplayCourseCode(course.getDisplayCourseCode());
                    dto.setNumberOfChapters(course.getChapters().size());
                    dto.setNumberOfVideos(course.getChapters().stream()
                            .mapToInt(Chapter::getNumberOfVideos2)
                            .sum());
                    dto.setTranslations(course.getTranslations().stream()
                            .map(this::toTranslationDto)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}

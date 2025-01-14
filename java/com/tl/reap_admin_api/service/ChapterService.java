package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.ChapterDao;
import com.tl.reap_admin_api.dto.ChapterDto;
import com.tl.reap_admin_api.dto.ChapterTranslationDto;
import com.tl.reap_admin_api.model.Chapter;
import com.tl.reap_admin_api.model.ChapterTranslation;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.exception.ChapterNotFoundException;
import com.tl.reap_admin_api.mapper.ChapterMapper;
import com.tl.reap_admin_api.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChapterService {

    private final ChapterDao chapterDao;
    private final ChapterMapper chapterMapper;
    private final CourseService courseService;
    private final LanguageService languageService;
    private final CourseMapper courseMapper;

    @Autowired
    public ChapterService(ChapterDao chapterDao, ChapterMapper chapterMapper, CourseService courseService, LanguageService languageService, CourseMapper courseMapper) {
        this.chapterDao = chapterDao;
        this.chapterMapper = chapterMapper;
        this.courseService = courseService;
        this.languageService = languageService;
        this.courseMapper = courseMapper;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public ChapterDto createChapter(ChapterDto chapterDto) {
        Chapter chapter = chapterMapper.toEntity(chapterDto);
        CourseDto courseDto = courseService.getCourseByUuid(chapterDto.getCourseUuid());
        Course course = courseMapper.toEntity(courseDto);  
        chapter.setCourse(course);
        Chapter savedChapter = chapterDao.save(chapter);
        return chapterMapper.toDto(savedChapter);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF', 'TRAINER', 'TRAINEE')")
    public List<ChapterDto> getAllChapters() {
        return chapterDao.findAll().stream()
                .map(chapterMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF', 'TRAINER', 'TRAINEE')")
    public ChapterDto getChapterByUuid(UUID uuid) {
        Chapter chapter = chapterDao.findByUuid(uuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + uuid));
        return chapterMapper.toDto(chapter);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public ChapterDto updateChapter(UUID uuid, ChapterDto chapterDto) {
        Chapter existingChapter = chapterDao.findByUuid(uuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + uuid));

        existingChapter.setOrderNumber(chapterDto.getOrderNumber());
        existingChapter.setUpdatedAt(ZonedDateTime.now());

        // Update translations
        // Update existing translations and add new ones
        //   ChapterTranslationDto
        for (ChapterTranslationDto translationDto : chapterDto.getTranslations()) {
            ChapterTranslation translation = existingChapter.getTranslations().stream()
                    .filter(t -> t.getLanguage().getCode().equals(translationDto.getLanguageCode()))
                    .findFirst()
                    .orElseGet(() -> {
                        ChapterTranslation newTranslation = new ChapterTranslation();
                        newTranslation.setChapter(existingChapter);
                        newTranslation.setLanguage(languageService.getLanguageByCode(translationDto.getLanguageCode()));
                        existingChapter.addTranslation(newTranslation);
                        return newTranslation;
                    });

            translation.setTitle(translationDto.getTitle());
            translation.setDescription(translationDto.getDescription());
        }

        // Remove translations that are no longer present in the DTO
        existingChapter.getTranslations().removeIf(translation ->
                chapterDto.getTranslations().stream()
                        .noneMatch(dto -> dto.getLanguageCode().equals(translation.getLanguage().getCode())));

        Chapter updatedChapter = chapterDao.save(existingChapter);
        return chapterMapper.toDto(updatedChapter);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void deleteChapter(UUID uuid) {
        Chapter chapter = chapterDao.findByUuid(uuid)
                .orElseThrow(() -> new ChapterNotFoundException("Chapter not found with uuid: " + uuid));
        chapterDao.delete(chapter);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF', 'TRAINER', 'TRAINEE')")
    public List<ChapterDto> getChaptersByCourseId(UUID courseId) {
        return chapterDao.findByCourseId(courseId).stream()
                .map(chapterMapper::toDto)
                .collect(Collectors.toList());
    }
}
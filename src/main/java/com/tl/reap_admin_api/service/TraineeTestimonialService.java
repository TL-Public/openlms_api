package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.TraineeTestimonialDao;
import com.tl.reap_admin_api.dao.CourseDao;
import com.tl.reap_admin_api.dao.LanguageDao;
import com.tl.reap_admin_api.dto.TraineeTestimonialDto;
import com.tl.reap_admin_api.dto.TraineeTestimonialTranslationDto;
import com.tl.reap_admin_api.mapper.TraineeTestimonialMapper;
import com.tl.reap_admin_api.model.TraineeTestimonial;
import com.tl.reap_admin_api.model.TraineeTestimonialTranslation;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TraineeTestimonialService {

	private final TraineeTestimonialDao traineeTestimonialDao;
	private final TraineeTestimonialMapper traineeTestimonialMapper;
	private final LanguageDao languageDao;
	private final CourseDao courseDao;

	@Autowired
	public TraineeTestimonialService(TraineeTestimonialDao traineeTestimonialDao,
			TraineeTestimonialMapper traineeTestimonialMapper, LanguageDao languageDao, CourseDao courseDao) {
		this.traineeTestimonialDao = traineeTestimonialDao;
		this.traineeTestimonialMapper = traineeTestimonialMapper;
		this.languageDao = languageDao;
		this.courseDao = courseDao;
	}

	@Transactional(readOnly = true)
    public List<TraineeTestimonialDto> getTestimonials(UUID courseUuid) {
        List<TraineeTestimonial> testimonials;
        if (courseUuid != null) {
            testimonials = traineeTestimonialDao.findByCourseUuid(courseUuid);
        } else {
            testimonials = traineeTestimonialDao.findAll();
        }
        return testimonials.stream().map(traineeTestimonialMapper::toDTO).filter(Objects::nonNull).collect(Collectors.toList());
    }

	@Transactional(readOnly = true)
	public TraineeTestimonialDto getTestimonialByUuid(UUID uuid) {
		return traineeTestimonialDao.findByUuid(uuid).map(traineeTestimonialMapper::toDTO).orElse(null);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public TraineeTestimonialDto createTestimonial(TraineeTestimonialDto testimonialDto) {
		Course course = courseDao.findByUuid(testimonialDto.getCourseUuid())
				.orElseThrow(() -> new RuntimeException("Course not found"));
		TraineeTestimonial testimonial = traineeTestimonialMapper.toEntity(testimonialDto, course);
		testimonial.setUuid(UUID.randomUUID());
		setLanguagesForTranslations(testimonial);
		TraineeTestimonial savedTestimonial = traineeTestimonialDao.save(testimonial);
		return traineeTestimonialMapper.toDTO(savedTestimonial);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public TraineeTestimonialDto updateTestimonial(UUID uuid, TraineeTestimonialDto testimonialDto) {
		TraineeTestimonial testimonial = traineeTestimonialDao.findByUuid(uuid)
				.orElseThrow(() -> new RuntimeException("Testimonial not found with UUID: " + uuid));

		Course course = courseDao.findByUuid(testimonialDto.getCourseUuid()).orElseThrow(
				() -> new RuntimeException("Course not found with UUID: " + testimonialDto.getCourseUuid()));

		testimonial.setCourse(course);

		// Clear existing translations
		testimonial.getTranslations().clear();

		// Add new translations
		for (TraineeTestimonialTranslationDto translationDto : testimonialDto.getTranslations()) {
			TraineeTestimonialTranslation translation = new TraineeTestimonialTranslation();
			translation.setLanguageCode(translationDto.getLanguageCode());
			translation.setName(translationDto.getName());
			translation.setDesignation(translationDto.getDesignation());
			translation.setPlace(translationDto.getPlace());
			translation.setTestimonialText(translationDto.getTestimonialText());

			Language language = languageDao.findByCode(translationDto.getLanguageCode()).orElseThrow(
					() -> new RuntimeException("Language not found with code: " + translationDto.getLanguageCode()));
			translation.setLanguage(language);

			testimonial.addTranslation(translation);
		}

		TraineeTestimonial updatedTestimonial = traineeTestimonialDao.save(testimonial);
		return traineeTestimonialMapper.toDTO(updatedTestimonial);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public void deleteTestimonial(UUID uuid) {
		traineeTestimonialDao.deleteByUuid(uuid);
	}

	private void setLanguagesForTranslations(TraineeTestimonial testimonial) {
		for (TraineeTestimonialTranslation translation : testimonial.getTranslations()) {
			String languageCode = translation.getLanguageCode();
			if (languageCode == null || languageCode.isEmpty()) {
				throw new IllegalArgumentException("Language code is missing for a translation");
			}
			Language language = languageDao.findByCode(languageCode)
					.orElseThrow(() -> new RuntimeException("Language not found: " + languageCode));
			translation.setLanguage(language);
		}
	}
}
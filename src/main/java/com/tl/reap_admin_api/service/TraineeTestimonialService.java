package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.TraineeTestimonialDao;
import com.tl.reap_admin_api.dao.CourseDao;
import com.tl.reap_admin_api.dao.LanguageDao;
import com.tl.reap_admin_api.dao.TraineeRsetiDao;
import com.tl.reap_admin_api.dto.TraineeTestimonialDto;
import com.tl.reap_admin_api.dto.TraineeTestimonialTranslationDto;
import com.tl.reap_admin_api.mapper.TraineeTestimonialMapper;
import com.tl.reap_admin_api.model.TraineeTestimonial;
import com.tl.reap_admin_api.model.TraineeTestimonialTranslation;
import com.tl.reap_admin_api.model.User;

import jakarta.persistence.EntityNotFoundException;

import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.model.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
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
	private final TraineeRsetiDao traineeRsetiDao;
	private final UserService userService;

    @Autowired
    public TraineeTestimonialService(TraineeTestimonialDao traineeTestimonialDao,
                                     TraineeRsetiDao traineeRsetiDao,
                                     TraineeTestimonialMapper traineeTestimonialMapper,
                                     LanguageDao languageDao,
                                     CourseDao courseDao,
                                     UserService userService) {
        this.traineeTestimonialDao = traineeTestimonialDao;
        this.traineeRsetiDao = traineeRsetiDao;
        this.traineeTestimonialMapper = traineeTestimonialMapper;
        this.languageDao = languageDao;
        this.courseDao = courseDao;
        this.userService = userService;
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
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public TraineeTestimonialDto createTestimonial(TraineeTestimonialDto testimonialDto) {
		Course course = courseDao.findByUuid(testimonialDto.getCourseUuid())
				.orElseThrow(() -> new RuntimeException("Course not found"));
		TraineeTestimonial testimonial = traineeTestimonialMapper.toEntity(testimonialDto, course);
		testimonial.setUuid(UUID.randomUUID());
		testimonial.setCreatedAt(ZonedDateTime.now());
		testimonial.setUpdatedAt(ZonedDateTime.now());
		 // Get the current user
        User currentUser = userService.getCurrentUser();
        testimonial.setUpdatedBy(currentUser.getUsername());
        testimonial.setCreatedBy(currentUser.getUsername()); 

		setLanguagesForTranslations(testimonial);
		TraineeTestimonial savedTestimonial = traineeTestimonialDao.save(testimonial);
		return traineeTestimonialMapper.toDTO(savedTestimonial);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public TraineeTestimonialDto updateTestimonial(UUID uuid, TraineeTestimonialDto testimonialDto) {
		TraineeTestimonial testimonial = traineeTestimonialDao.findByUuid(uuid)
				.orElseThrow(() -> new RuntimeException("Testimonial not found with UUID: " + uuid));

		Course course = courseDao.findByUuid(testimonialDto.getCourseUuid()).orElseThrow(
				() -> new RuntimeException("Course not found with UUID: " + testimonialDto.getCourseUuid()));
		testimonial.setUpdatedAt(ZonedDateTime.now());
		 // Get the current user
        User currentUser = userService.getCurrentUser();
        testimonial.setUpdatedBy(currentUser.getUsername());

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
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public void deleteTestimonial(UUID uuid) {
		TraineeTestimonial testimonial = traineeTestimonialDao.findByUuid(uuid)
		        .orElseThrow(() -> new EntityNotFoundException("Testimonial not found with UUID: " + uuid));

		 // Get the current user
        User currentUser = userService.getCurrentUser();
        testimonial.setUpdatedBy(currentUser.getUsername());
        testimonial.setUpdatedAt(ZonedDateTime.now());
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

/* 	private void checkTestimonialReadPermission(TraineeTestimonial testimonial) {
        User currentUser = userService.getCurrentUser();
        Role userRole = currentUser.getRole();

        switch (userRole) {
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
                // These roles have access to all testimonials
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                if (!testimonial.getCourse().getRseti().getStateId().equals(currentUser.getUserProfile().getState().getExtId())) {
                    throw new AccessDeniedException("You don't have permission to access this testimonial");
                }
                break;
            case RSETI_ADMIN:
            case RSETI_STAFF:
                if (!testimonial.getCourse().getRseti().getUuid().equals(currentUser.getUserProfile().getRseti().getUuid())) {
                    throw new AccessDeniedException("You don't have permission to access this testimonial");
                }
                break;
            default:
                throw new AccessDeniedException("You don't have permission to access testimonial data");
        }
    }

    private void checkTestimonialWritePermission(TraineeTestimonial testimonial) {
        User currentUser = userService.getCurrentUser();
        Role userRole = currentUser.getRole();

        switch (userRole) {
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
                // These roles have write access to all testimonials
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                if (!testimonial.getCourse().getRseti().getStateId().equals(currentUser.getUserProfile().getState().getExtId())) {
                    throw new AccessDeniedException("You don't have permission to modify this testimonial");
                }
                break;
            default:
                throw new AccessDeniedException("You don't have permission to modify testimonial data");
        }
    }

    private void checkCourseWritePermission(Course course) {
        User currentUser = userService.getCurrentUser();
        Role userRole = currentUser.getRole();

        switch (userRole) {
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
                // These roles have write access to all courses
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                if (!course.getRseti().getStateId().equals(currentUser.getUserProfile().getState().getExtId())) {
                    throw new AccessDeniedException("You don't have permission to modify testimonials for this course");
                }
                break;
            default:
                throw new AccessDeniedException("You don't have permission to modify testimonial data");
        }
    } */
}
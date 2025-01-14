package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.TraineeTestimonialDto;
import com.tl.reap_admin_api.dto.TraineeTestimonialTranslationDto;
import com.tl.reap_admin_api.model.TraineeTestimonial;
import com.tl.reap_admin_api.model.TraineeTestimonialTranslation;
import com.tl.reap_admin_api.model.Course;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TraineeTestimonialMapper {

	public TraineeTestimonialDto toDTO(TraineeTestimonial testimonial) {
		if (testimonial == null || testimonial.getCourse() == null) {
			return null;
		}
		return new TraineeTestimonialDto(testimonial.getUuid(), testimonial.getCourse().getUuid(),
				testimonial.getTranslations().stream().map(this::toTranslationDTO).collect(Collectors.toList()));
	}

	public TraineeTestimonial toEntity(TraineeTestimonialDto dto, Course course) {
		if (dto == null) {
			return null;
		}
		TraineeTestimonial testimonial = new TraineeTestimonial();
		testimonial.setUuid(dto.getUuid());
		testimonial.setCourse(course);
		if (dto.getTranslations() != null) {
			dto.getTranslations().forEach(translationDto -> {
				TraineeTestimonialTranslation translation = toTranslationEntity(translationDto);
				testimonial.addTranslation(translation);
			});
		}
		return testimonial;
	}

	public void updateEntityFromDTO(TraineeTestimonialDto dto, TraineeTestimonial testimonial, Course course) {
		if (dto == null || testimonial == null) {
			return;
		}

		testimonial.setCourse(course);

		testimonial.getTranslations().clear();
		if (dto.getTranslations() != null) {
			dto.getTranslations().forEach(translationDto -> {
				TraineeTestimonialTranslation translation = toTranslationEntity(translationDto);
				testimonial.addTranslation(translation);
			});
		}
	}

	private TraineeTestimonialTranslationDto toTranslationDTO(TraineeTestimonialTranslation translation) {
		return new TraineeTestimonialTranslationDto(translation.getLanguageCode(), translation.getName(),
				translation.getDesignation(), translation.getPlace(), translation.getTestimonialText());
	}

	private TraineeTestimonialTranslation toTranslationEntity(TraineeTestimonialTranslationDto dto) {
		TraineeTestimonialTranslation translation = new TraineeTestimonialTranslation();
		translation.setLanguageCode(dto.getLanguageCode());
		translation.setName(dto.getName());
		translation.setDesignation(dto.getDesignation());
		translation.setPlace(dto.getPlace());
		translation.setTestimonialText(dto.getTestimonialText());
		return translation;
	}
}
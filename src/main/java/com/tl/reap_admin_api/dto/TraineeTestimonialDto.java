package com.tl.reap_admin_api.dto;

import java.util.List;
import java.util.UUID;

public class TraineeTestimonialDto {
	private UUID uuid;
	private UUID courseUuid;
	private List<TraineeTestimonialTranslationDto> translations;

	// Constructors
	public TraineeTestimonialDto() {
	}

	public TraineeTestimonialDto(UUID uuid, UUID courseUuid, List<TraineeTestimonialTranslationDto> translations) {
		this.uuid = uuid;
		this.courseUuid = courseUuid;
		this.translations = translations;
	}

	// Getters and Setters
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getCourseUuid() {
		return courseUuid;
	}

	public void setCourseUuid(UUID courseUuid) {
		this.courseUuid = courseUuid;
	}

	public List<TraineeTestimonialTranslationDto> getTranslations() {
		return translations;
	}

	public void setTranslations(List<TraineeTestimonialTranslationDto> translations) {
		this.translations = translations;
	}
}
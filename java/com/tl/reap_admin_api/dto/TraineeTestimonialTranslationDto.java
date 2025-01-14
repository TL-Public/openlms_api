package com.tl.reap_admin_api.dto;

public class TraineeTestimonialTranslationDto {
	private String languageCode;
	private String name;
	private String designation;
	private String place;
	private String testimonialText;

	// Constructors
	public TraineeTestimonialTranslationDto() {
	}

	public TraineeTestimonialTranslationDto(String languageCode, String name, String designation, String place,
			String testimonialText) {
		this.languageCode = languageCode;
		this.name = name;
		this.designation = designation;
		this.place = place;
		this.testimonialText = testimonialText;
	}

	// Getters and Setters
	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getTestimonialText() {
		return testimonialText;
	}

	public void setTestimonialText(String testimonialText) {
		this.testimonialText = testimonialText;
	}
}
package com.tl.reap_admin_api.dto;

public class TestimonialTranslationDTO {
	private Long id;
	private String languageCode;
	private String name;
	private String designation;
	private String testimonialText;

	// Constructors, getters, and setters

	public TestimonialTranslationDTO() {
	}

	public TestimonialTranslationDTO(Long id, String languageCode, String name, String designation,
			String testimonialText) {
		this.id = id;
		this.languageCode = languageCode;
		this.name = name;
		this.designation = designation;
		this.testimonialText = testimonialText;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public String getTestimonialText() {
		return testimonialText;
	}

	public void setTestimonialText(String testimonialText) {
		this.testimonialText = testimonialText;
	}
}
package com.tl.reap_admin_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "testimonial_translations")
public class TestimonialTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "testimonial_id", nullable = false)
	private Testimonial testimonial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id", nullable = false)
	private Language language;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String designation;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String testimonialText;

	// Getters and setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Testimonial getTestimonial() {
		return testimonial;
	}

	public void setTestimonial(Testimonial testimonial) {
		this.testimonial = testimonial;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
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
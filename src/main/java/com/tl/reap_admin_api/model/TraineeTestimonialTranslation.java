package com.tl.reap_admin_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "trainee_testimonial_translations")
public class TraineeTestimonialTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_testimonial_id", nullable = false)
	private TraineeTestimonial traineeTestimonial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id", nullable = false)
	private Language language;

	@Column(name = "language_code", nullable = false)
	private String languageCode;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String designation;

	@Column(nullable = false)
	private String place;

	@Column(name = "testimonial_text", nullable = false, columnDefinition = "TEXT")
	private String testimonialText;

	// Constructors
	public TraineeTestimonialTranslation() {
	}

	public TraineeTestimonialTranslation(String languageCode, String name, String designation, String place,
			String testimonialText) {
		this.languageCode = languageCode;
		this.name = name;
		this.designation = designation;
		this.place = place;
		this.testimonialText = testimonialText;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TraineeTestimonial getTraineeTestimonial() {
		return traineeTestimonial;
	}

	public void setTraineeTestimonial(TraineeTestimonial traineeTestimonial) {
		this.traineeTestimonial = traineeTestimonial;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
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
package com.tl.reap_admin_api.model;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trainee_testimonials")
public class TraineeTestimonial {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "uuid", updatable = false, nullable = false)
	private UUID uuid;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_uuid", referencedColumnName = "uuid", nullable = false)
	private Course course;

	@OneToMany(mappedBy = "traineeTestimonial", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TraineeTestimonialTranslation> translations = new ArrayList<>();
	

	@Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_by",  updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

	// Constructors

	

    public TraineeTestimonial() {
        this.uuid = UUID.randomUUID();
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }


	public TraineeTestimonial(Course course) {
		this();
		this.course = course;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public List<TraineeTestimonialTranslation> getTranslations() {
		return translations;
	}

	public void setTranslations(List<TraineeTestimonialTranslation> translations) {
		this.translations = translations;
	}

	// Helper methods for managing translations
	public void addTranslation(TraineeTestimonialTranslation translation) {
		translations.add(translation);
		translation.setTraineeTestimonial(this);
	}

	public void removeTranslation(TraineeTestimonialTranslation translation) {
		translations.remove(translation);
		translation.setTraineeTestimonial(null);
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	
}
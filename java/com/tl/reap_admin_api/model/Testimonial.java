package com.tl.reap_admin_api.model;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "testimonials")
public class Testimonial {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private UUID uuid;

	@Column(nullable = true)
	private String image;
	
	@Column(name = "video_url")
    private String videoUrl;

    @Column(name = "video_extid")
    private String videoExtId;

	@Column(name = "created_at", nullable = false)
	private ZonedDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private ZonedDateTime updatedAt;
	
	@Column(name = "created_by", nullable = false)
	private String createdBy;

	@Column(name = "updated_by", nullable = false)
	private String updatedBy;

	@OneToMany(mappedBy = "testimonial", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<TestimonialTranslation> translations = new HashSet<>();

	// Constructors, getters, and setters

	@PrePersist
	protected void onCreate() {
		this.uuid = UUID.randomUUID();
		this.createdAt = ZonedDateTime.now();
		this.updatedAt = ZonedDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = ZonedDateTime.now();
	}

	public void addTranslation(TestimonialTranslation translation) {
		translations.add(translation);
		translation.setTestimonial(this);
	}

	public void removeTranslation(TestimonialTranslation translation) {
		translations.remove(translation);
		translation.setTestimonial(null);
	}

	public void clearTranslations() {
		for (TestimonialTranslation translation : new HashSet<>(translations)) {
			removeTranslation(translation);
		}
	}

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

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
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

	public Set<TestimonialTranslation> getTranslations() {
		return translations;
	}

	public void setTranslations(Set<TestimonialTranslation> translations) {
		this.translations = translations;
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
	
	public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoExtId() {
        return videoExtId;
    }

    public void setVideoExtId(String videoExtId) {
        this.videoExtId = videoExtId;
    }
	
	

}
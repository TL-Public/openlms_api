package com.tl.reap_admin_api.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestimonialDTO {
	private UUID uuid;
	private String image;
	private String videoUrl;
    private String videoExtId;
	private Set<TestimonialTranslationDTO> translations = new HashSet<>();

	// Constructors, getters, and setters
	public TestimonialDTO() {
	}

	public TestimonialDTO(UUID uuid, String image) {
		this.uuid = uuid;
		this.image = image;
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

	public Set<TestimonialTranslationDTO> getTranslations() {
		return translations;
	}

	public void setTranslations(Set<TestimonialTranslationDTO> translations) {
		this.translations = translations != null ? translations : new HashSet<>();
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
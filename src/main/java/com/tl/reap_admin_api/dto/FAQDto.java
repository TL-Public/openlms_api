package com.tl.reap_admin_api.dto;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class FAQDto {
	private UUID uuid;
	private Integer categoryId;
	private Set<FAQTranslationDto> translations = new HashSet<>();

	// Constructors, getters, and setters

	public FAQDto() {
	}

	public FAQDto(UUID uuid, Integer categoryId) {
		this.uuid = uuid;
		this.categoryId = categoryId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}


	public Set<FAQTranslationDto> getTranslations() {
		return translations;
	}

	public void setTranslations(Set<FAQTranslationDto> translations) {
		this.translations = translations != null ? translations : new HashSet<>();
	}
}
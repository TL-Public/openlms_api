package com.tl.reap_admin_api.mapper;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.tl.reap_admin_api.dto.TestimonialDTO;
import com.tl.reap_admin_api.dto.TestimonialTranslationDTO;
import com.tl.reap_admin_api.model.Testimonial;
import com.tl.reap_admin_api.model.TestimonialTranslation;

@Component
public class TestimonialMapper {

	public TestimonialDTO toDTO(Testimonial testimonial) {
		if (testimonial == null) {
			return null;
		}

		TestimonialDTO dto = new TestimonialDTO();
		dto.setUuid(testimonial.getUuid());
		dto.setImage(testimonial.getImage());
		dto.setVideoUrl(testimonial.getVideoUrl());
        dto.setVideoExtId(testimonial.getVideoExtId());
        dto.setOrderNo(testimonial.getOrderNo());

		if (testimonial.getTranslations() != null) {
			dto.setTranslations(testimonial.getTranslations().stream().map(this::toDTO).collect(Collectors.toSet()));
		} else {
			dto.setTranslations(new HashSet<>());
		}

		return dto;
	}

	public TestimonialTranslationDTO toDTO(TestimonialTranslation translation) {
		if (translation == null) {
			return null;
		}

		TestimonialTranslationDTO dto = new TestimonialTranslationDTO();
		dto.setId(translation.getId());
		dto.setName(translation.getName());
		dto.setDesignation(translation.getDesignation());
		dto.setTestimonialText(translation.getTestimonialText());

		if (translation.getLanguage() != null) {
			dto.setLanguageCode(translation.getLanguage().getCode());
		}

		return dto;
	}

	public Testimonial toEntity(TestimonialDTO dto) {
		if (dto == null) {
			return null;
		}

		Testimonial testimonial = new Testimonial();
		testimonial.setUuid(dto.getUuid());
		testimonial.setImage(dto.getImage());
		testimonial.setVideoUrl(dto.getVideoUrl());
        testimonial.setVideoExtId(dto.getVideoExtId());
        testimonial.setOrderNo(dto.getOrderNo());

		if (dto.getTranslations() != null) {
			testimonial.setTranslations(dto.getTranslations().stream().map(this::toEntity).collect(Collectors.toSet()));
		} else {
			testimonial.setTranslations(new HashSet<>());
		}

		return testimonial;
	}

	public TestimonialTranslation toEntity(TestimonialTranslationDTO dto) {
		if (dto == null) {
			return null;
		}

		TestimonialTranslation translation = new TestimonialTranslation();
		translation.setId(dto.getId());
		translation.setName(dto.getName());
		translation.setDesignation(dto.getDesignation());
		translation.setTestimonialText(dto.getTestimonialText());

		return translation;
	}
}
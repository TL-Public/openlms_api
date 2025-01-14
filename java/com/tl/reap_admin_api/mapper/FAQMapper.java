package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.model.FAQ;
import com.tl.reap_admin_api.model.FAQTranslation;
import com.tl.reap_admin_api.dto.FAQDto;
import com.tl.reap_admin_api.dto.FAQTranslationDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.HashSet;

@Component
public class FAQMapper {

    public FAQDto toDto(FAQ faq) {
        if (faq == null) {
            return null;
        }

        FAQDto dto = new FAQDto();
        dto.setUuid(faq.getUuid());
        dto.setCategoryId(faq.getCategory().getExtId());
        if (faq.getTranslations() != null) {
            dto.setTranslations(faq.getTranslations().stream()
                    .map(this::toDto)
                    .collect(Collectors.toSet()));
        } else {
            dto.setTranslations(new HashSet<>());
        }

        return dto;
    }

    public FAQTranslationDto toDto(FAQTranslation translation) {
        if (translation == null) {
            return null;
        }

        FAQTranslationDto dto = new FAQTranslationDto();
        dto.setId(translation.getId());
        dto.setQuestion(translation.getQuestion());
        dto.setAnswer(translation.getAnswer());
        
        if (translation.getLanguage() != null) {
            dto.setLanguageCode(translation.getLanguage().getCode());
        }

        return dto;
    }

    public FAQ toEntity(FAQDto dto) {
        if (dto == null) {
            return null;
        }

        FAQ faq = new FAQ();
        faq.setUuid(dto.getUuid());

        if (dto.getTranslations() != null) {
            faq.setTranslations(dto.getTranslations().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toSet()));
        } else {
            faq.setTranslations(new HashSet<>());
        }

        return faq;
    }

    public FAQTranslation toEntity(FAQTranslationDto dto) {
        if (dto == null) {
            return null;
        }

        FAQTranslation translation = new FAQTranslation();
        translation.setId(dto.getId());
        translation.setQuestion(dto.getQuestion());
        translation.setAnswer(dto.getAnswer());

        return translation;
    }
}
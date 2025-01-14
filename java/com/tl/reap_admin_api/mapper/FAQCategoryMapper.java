package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.FAQCategoryDto;
import com.tl.reap_admin_api.model.FAQCategory;
import org.springframework.stereotype.Component;

@Component
public class FAQCategoryMapper {
    public FAQCategoryDto toDTO(FAQCategory faqCategory) {
        if (faqCategory == null) {
            return null;
        }
        return new FAQCategoryDto(
            faqCategory.getExtId(),
            faqCategory.getCategory(),
            faqCategory.getLanguageCode()
        );
    }

    public FAQCategory toEntity(FAQCategoryDto dto) {
        if (dto == null) {
            return null;
        }
        FAQCategory faqCategory = new FAQCategory();
        faqCategory.setExtId(dto.getExtId());
        faqCategory.setCategory(dto.getCategory());
        faqCategory.setLanguageCode(dto.getLanguageCode());
        return faqCategory;
    }

    public void updateEntityFromDTO(FAQCategoryDto dto, FAQCategory faqCategory) {
        if (dto == null || faqCategory == null) {
            return;
        }
        faqCategory.setCategory(dto.getCategory());
        faqCategory.setLanguageCode(dto.getLanguageCode());
    }
}
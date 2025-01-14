package com.tl.reap_admin_api.mapper;

import org.springframework.stereotype.Component;
import com.tl.reap_admin_api.dto.CategoryDto;
import com.tl.reap_admin_api.model.Category;

@Component
public class CategoryMapper {
    
    public CategoryDto toDTO(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(
            category.getExtId(),
            category.getName(),
            category.getLanguageCode()
        );
    }

    public Category toEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }
        Category category = new Category();
        category.setExtId(dto.getExtId());
        category.setName(dto.getName());
        category.setLanguageCode(dto.getLanguageCode());
        return category;
    }

    public void updateEntityFromDTO(CategoryDto dto, Category category) {
        if (dto == null || category == null) {
            return;
        }
        category.setName(dto.getName());
        category.setLanguageCode(dto.getLanguageCode());
    }
}


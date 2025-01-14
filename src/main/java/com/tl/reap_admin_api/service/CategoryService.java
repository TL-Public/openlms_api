package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.CategoryDao;
import com.tl.reap_admin_api.dto.CategoryDto;
import com.tl.reap_admin_api.mapper.CategoryMapper;
import com.tl.reap_admin_api.model.Category;
import com.tl.reap_admin_api.model.FAQCategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryDao categoryDao;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryService(CategoryDao categoryDao, CategoryMapper categoryMapper) {
        this.categoryDao = categoryDao;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public CategoryDto createCategory(CategoryDto categoryDto) {
        // Check if a category with the same name and language code already exists
        Optional<Category> existingCategory = categoryDao.findByNameAndLanguageCode(categoryDto.getName(), categoryDto.getLanguageCode());
        if (existingCategory.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists with the given name and language code");
        }

        Category categoryWithSameExtId = null;
        if (categoryDto.getExtId() != null) {
            List<Category> categoryList = categoryDao.findAllByExtId(categoryDto.getExtId());
            for (Category category : categoryList) {
                if (category.getLanguageCode().equals(categoryDto.getLanguageCode())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists with the given extId and language code");
                } else {
                    categoryWithSameExtId = category;
                }
            }
        }

        Integer extId;
        if (categoryWithSameExtId != null) {
            // Use the existing extId for the same name
            extId = categoryWithSameExtId.getExtId();
        } else {
            // Generate a new extId for a new category name
            Integer maxExtId = categoryDao.getMaxExtId();
            extId = (maxExtId == null) ? 1 : maxExtId + 1;
        }

        Category newCategory = categoryMapper.toEntity(categoryDto);
        newCategory.setExtId(extId);
        Category savedCategory = categoryDao.save(newCategory);
        return categoryMapper.toDTO(savedCategory);
    }

    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryDao.findAll();
        return categories.stream().map(categoryMapper::toDTO).collect(Collectors.toList());
    }

    public CategoryDto getCategoryByExtIdAndLanguageCode(Integer extId, String languageCode) {
        Category category = categoryDao.findByExtIdAndLanguageCode(extId, languageCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return categoryMapper.toDTO(category);
    }

   @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public CategoryDto updateCategory(Integer extId, CategoryDto categoryDto) {
        Category category = categoryDao.findByExtIdAndLanguageCode(extId, categoryDto.getLanguageCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        categoryMapper.updateEntityFromDTO(categoryDto, category);
        Category updatedCategory = categoryDao.save(category);
        return categoryMapper.toDTO(updatedCategory);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public boolean deleteCategoryByExtId(Integer extId) {
        int deletedCount = categoryDao.deleteByExtId(extId);
        return deletedCount > 0;
    }

    public List<CategoryDto> getAllCategoriesByExtId(Integer extId) {
        List<Category> categories = categoryDao.findAllByExtId(extId);
        return categories.stream().map(categoryMapper::toDTO).collect(Collectors.toList());
    }
}


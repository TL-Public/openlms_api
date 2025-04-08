package com.tl.reap_admin_api.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tl.reap_admin_api.dao.FAQCategoryDao;
import com.tl.reap_admin_api.dto.FAQCategoryDto;
import com.tl.reap_admin_api.mapper.FAQCategoryMapper;
import com.tl.reap_admin_api.model.FAQCategory;
import com.tl.reap_admin_api.model.User;

@Service
public class FAQCategoryService {

    private final FAQCategoryDao faqCategoryDAO;
    private final FAQCategoryMapper faqCategoryMapper;
    private final UserService userService;

    public FAQCategoryService(FAQCategoryDao faqCategoryDAO, FAQCategoryMapper faqCategoryMapper,UserService userService) {
        this.faqCategoryDAO = faqCategoryDAO;
        this.faqCategoryMapper = faqCategoryMapper;
        this.userService = userService;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public FAQCategoryDto createFAQCategory(FAQCategoryDto faqCategoryDto) {
        Optional<FAQCategory> existingCategoryByName = faqCategoryDAO.findByNameAndLanguageCode(faqCategoryDto.getCategory(), faqCategoryDto.getLanguageCode());
        if (existingCategoryByName.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "FAQ Category already exists with the given name and language code");
        }

        FAQCategory faqCategoryWithSameExtId = null;
        if (faqCategoryDto.getExtId() != null) {
            List<FAQCategory> faqCategoryList = faqCategoryDAO.findByExtId(faqCategoryDto.getExtId());
            for (FAQCategory category : faqCategoryList) {
                if (category.getLanguageCode().equals(faqCategoryDto.getLanguageCode())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "FAQ Category already exists with the given extId and language code");
                } else {
                    faqCategoryWithSameExtId = category;
                }
            }
        }

        Integer extId;
        if (faqCategoryWithSameExtId != null) {
            extId = faqCategoryWithSameExtId.getExtId();
            System.out.println("extId --" + extId);
         
        } else {
            Integer maxExtId = faqCategoryDAO.getMaxExtId();
            extId = (maxExtId == null) ? 1 : maxExtId + 1;
            System.out.println("newExtId --" + extId);
            
        }

        FAQCategory newCategory = new FAQCategory(faqCategoryDto.getCategory(), extId, faqCategoryDto.getLanguageCode());
        User currentUser = userService.getCurrentUser();
        newCategory.setCreatedBy(currentUser.getUsername());
        newCategory.setCreatedAt(ZonedDateTime.now());
        newCategory.setUpdatedBy(currentUser.getUsername());
        newCategory.setUpdatedAt(ZonedDateTime.now());
        
        FAQCategory savedCategory = faqCategoryDAO.save(newCategory);
        return faqCategoryMapper.toDTO(savedCategory);
    }

    
    public List<FAQCategoryDto> getAllFAQCategories() {
        List<FAQCategory> categories = faqCategoryDAO.findAll();
        return categories.stream().map(faqCategoryMapper::toDTO).collect(Collectors.toList());
    }

    
    public FAQCategoryDto getFAQCategoryByExtIdAndLanguageCode(Integer extId, String languageCode) {
        FAQCategory category = faqCategoryDAO.findByExtIdAndLanguageCode(extId, languageCode)
                .orElseThrow(() -> new RuntimeException("FAQ Category not found"));
        return faqCategoryMapper.toDTO(category);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public FAQCategoryDto updateFAQCategory(Integer extId, FAQCategoryDto faqCategoryDto) {
        FAQCategory category = faqCategoryDAO.findByExtIdAndLanguageCode(extId, faqCategoryDto.getLanguageCode())
                .orElseThrow(() -> new RuntimeException("FAQ Category not found"));
        faqCategoryMapper.updateEntityFromDTO(faqCategoryDto, category);
        FAQCategory updatedCategory = faqCategoryDAO.update(category);
        User currentUser = userService.getCurrentUser();
        updatedCategory.setUpdatedBy(currentUser.getUsername());
        updatedCategory.setUpdatedAt(ZonedDateTime.now());

        return faqCategoryMapper.toDTO(updatedCategory);
    }
    
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public boolean deleteFAQCategoryByExtId(String extId) {
        User currentUser = userService.getCurrentUser();
        int deletedCount = faqCategoryDAO.deleteByExtId(extId);
        if (deletedCount > 0) {
            FAQCategory updatedCategory = new FAQCategory();
            updatedCategory.setUpdatedBy(currentUser.getUsername());
            updatedCategory.setUpdatedAt(ZonedDateTime.now());
        }
        return deletedCount > 0;
    }
    
  
    public List<FAQCategoryDto> getAllFAQCategories(String extId) {
        List<FAQCategory> categories = faqCategoryDAO.findAll(extId); 
        return categories.stream().map(faqCategoryMapper::toDTO).collect(Collectors.toList());
    }


}
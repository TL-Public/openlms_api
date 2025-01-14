package com.tl.reap_admin_api.service;

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

@Service
public class FAQCategoryService {

    private final FAQCategoryDao faqCategoryDAO;
    private final FAQCategoryMapper faqCategoryMapper;

    public FAQCategoryService(FAQCategoryDao faqCategoryDAO, FAQCategoryMapper faqCategoryMapper) {
        this.faqCategoryDAO = faqCategoryDAO;
        this.faqCategoryMapper = faqCategoryMapper;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
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

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public FAQCategoryDto updateFAQCategory(Integer extId, FAQCategoryDto faqCategoryDto) {
        FAQCategory category = faqCategoryDAO.findByExtIdAndLanguageCode(extId, faqCategoryDto.getLanguageCode())
                .orElseThrow(() -> new RuntimeException("FAQ Category not found"));
        faqCategoryMapper.updateEntityFromDTO(faqCategoryDto, category);
        FAQCategory updatedCategory = faqCategoryDAO.update(category);
        return faqCategoryMapper.toDTO(updatedCategory);
    }
    
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public boolean deleteFAQCategoryByExtId(String extId) {
        int deletedCount = faqCategoryDAO.deleteByExtId(extId);
        return deletedCount > 0;
    }
    
  
    public List<FAQCategoryDto> getAllFAQCategories(String extId) {
        List<FAQCategory> categories = faqCategoryDAO.findAll(extId); 
        return categories.stream().map(faqCategoryMapper::toDTO).collect(Collectors.toList());
    }


}
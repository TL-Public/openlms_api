package com.tl.reap_admin_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tl.reap_admin_api.dto.FAQCategoryDto;
import com.tl.reap_admin_api.service.FAQCategoryService;

@RestController
@RequestMapping("/apis/v1/faqcategories")
public class FAQCategoryController {

    private final FAQCategoryService faqCategoryService;

    public FAQCategoryController(FAQCategoryService faqCategoryService) {
        this.faqCategoryService = faqCategoryService;
    }

    @PostMapping
    public ResponseEntity<?> createFAQCategory(@RequestBody FAQCategoryDto faqCategoryDto) {
        try {
            FAQCategoryDto createdFAQCategory = faqCategoryService.createFAQCategory(faqCategoryDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFAQCategory);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllFAQCategories() {
        try {
            List<FAQCategoryDto> faqCategories = faqCategoryService.getAllFAQCategories();
            return ResponseEntity.ok(faqCategories);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{extId}/{languageCode}")
    public ResponseEntity<?> getFAQCategoryByExtIdAndLanguageCode(@PathVariable Integer extId, @PathVariable String languageCode) {
        try {
            FAQCategoryDto faqCategory = faqCategoryService.getFAQCategoryByExtIdAndLanguageCode(extId, languageCode);
            return ResponseEntity.ok(faqCategory);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{extId}")
    public ResponseEntity<?> updateFAQCategory(@PathVariable Integer extId, @RequestBody FAQCategoryDto faqCategoryDto) {
        try {
            FAQCategoryDto updatedFAQCategory = faqCategoryService.updateFAQCategory(extId, faqCategoryDto);
            return ResponseEntity.ok(updatedFAQCategory);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
    
    @DeleteMapping("/{extId}")
    public ResponseEntity<?> deleteFAQCategoryByExtId(@PathVariable String extId) {
        try {
            boolean deleted = faqCategoryService.deleteFAQCategoryByExtId(extId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FAQ Category(ies) not found");
            }
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
    
    @GetMapping("/{extId}")
    public ResponseEntity<?> getAllFAQCategories(@PathVariable String extId) {
        try {
            List<FAQCategoryDto> faqCategories = faqCategoryService.getAllFAQCategories(extId);
            return ResponseEntity.ok(faqCategories);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

}
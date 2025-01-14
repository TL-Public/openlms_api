package com.tl.reap_admin_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tl.reap_admin_api.dto.FAQDto;
import com.tl.reap_admin_api.service.FAQService;

@RestController
@RequestMapping("/apis/v1/faqs")
public class FAQController {

    @Autowired
    private FAQService faqService;

    @PostMapping
    public ResponseEntity<FAQDto> createFAQ(@RequestBody FAQDto faqDto) {
        FAQDto createdFAQ = faqService.createFAQ(faqDto);
        return new ResponseEntity<>(createdFAQ, HttpStatus.CREATED);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<FAQDto> getFAQ(@PathVariable UUID uuid) {
        FAQDto faq = faqService.getFAQByUuid(uuid);
        return ResponseEntity.ok(faq);
    }

    @GetMapping
    public ResponseEntity<List<FAQDto>> getAllFAQs() {
        List<FAQDto> faqs = faqService.getAllFAQs();
        return ResponseEntity.ok(faqs);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<FAQDto> updateFAQ(@PathVariable UUID uuid, @RequestBody FAQDto faqDto) {
        FAQDto updatedFAQ = faqService.updateFAQ(uuid, faqDto);
        return ResponseEntity.ok(updatedFAQ);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteFAQ(@PathVariable UUID uuid) {
        faqService.deleteFAQ(uuid);
        return ResponseEntity.noContent().build();
    }

     @PostMapping("/bulk-upload")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public ResponseEntity<?> bulkUploadFAQs(@RequestParam("file") MultipartFile file) {
        try {
            String errorReportUrl = faqService.bulkUploadFAQs(file);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            if (errorReportUrl != null) {
                response.put("errorReportUrl", errorReportUrl);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }

}
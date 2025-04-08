package com.tl.reap_admin_api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;


import java.io.*;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tl.reap_admin_api.dao.FAQCategoryDao;
import com.tl.reap_admin_api.dao.FAQDao;
import com.tl.reap_admin_api.dto.FAQDto;
import com.tl.reap_admin_api.dto.FAQTranslationDto;
import com.tl.reap_admin_api.mapper.FAQMapper;
import com.tl.reap_admin_api.model.FAQ;
import com.tl.reap_admin_api.model.FAQCategory;
import com.tl.reap_admin_api.model.FAQTranslation;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.model.User;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class FAQService {
    
    @Autowired
    private FAQDao faqDao;
   
    @Autowired
    private FAQMapper mapper;
    
    @Autowired
    private UserService userService;

    @Autowired
    private FAQCategoryDao faqCategoryDao;
       
    @Autowired
    private AmazonS3 amazonS3Client;

    @Value("${aws.s3.crsimg.bucket-name}")
    private String bucketName;

    private static final String DEFAULT_LANGUAGE_CODE = "en";

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public FAQDto createFAQ(FAQDto dto) {
        FAQ faq = new FAQ();
        FAQCategory category = faqCategoryDao.findByExtIdAndLanguageCode(dto.getCategoryId(), DEFAULT_LANGUAGE_CODE)
                .orElseThrow(() -> new RuntimeException("Category not found with ext id: " + dto.getCategoryId() +" and language code " + DEFAULT_LANGUAGE_CODE));
        faq.setCategory(category);
        User currentUser = userService.getCurrentUser();
        faq.setCreatedBy(currentUser.getUsername());
        faq.setUpdatedBy(currentUser.getUsername());
        faq.setCreatedAt(ZonedDateTime.now());
        faq.setUpdatedAt(ZonedDateTime.now());
        updateTranslations(faq, dto.getTranslations());
        faq = faqDao.save(faq);
        return mapper.toDto(faq);
    }
   
    @Transactional
    public FAQDto getFAQByUuid(UUID uuid) {
        FAQ faq = faqDao.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        return mapper.toDto(faq);
    }

    @Transactional
    public List<FAQDto> getAllFAQs() {
        return faqDao.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public FAQDto updateFAQ(UUID uuid, FAQDto dto) {
        FAQ faq = faqDao.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        
        FAQCategory category = faqCategoryDao.findByExtIdAndLanguageCode(dto.getCategoryId(), DEFAULT_LANGUAGE_CODE)
                .orElseThrow(() -> new RuntimeException("Category not found with ext id: " + dto.getCategoryId() +" and language code " + DEFAULT_LANGUAGE_CODE));
        faq.setCategory(category);
        User currentUser = userService.getCurrentUser();
        faq.setUpdatedBy(currentUser.getUsername());
        faq.setUpdatedAt(ZonedDateTime.now());
        updateTranslations(faq, dto.getTranslations());

        faq = faqDao.save(faq);
        return mapper.toDto(faq);
    }

    private void updateTranslations(FAQ faq, Set<FAQTranslationDto> translationDtos) {
        faq.clearTranslations();

        if (translationDtos != null) {
            for (FAQTranslationDto translationDto : translationDtos) {
                FAQTranslation translation = new FAQTranslation();
                translation.setQuestion(translationDto.getQuestion());
                translation.setAnswer(translationDto.getAnswer());
                Language language = faqDao.findLanguageByCode(translationDto.getLanguageCode())
                        .orElseThrow(() -> new RuntimeException("Language not found: " + translationDto.getLanguageCode()));
                translation.setLanguage(language);
                faq.addTranslation(translation);
            }
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public void deleteFAQ(UUID uuid) {
    	FAQ faq = faqDao.findByUuid(uuid).orElseThrow(() -> new EntityNotFoundException("FAQ not found"));
    	 User currentUser = userService.getCurrentUser();
         faq.setUpdatedBy(currentUser.getUsername());
         faq.setUpdatedAt(ZonedDateTime.now());
         faqDao.save(faq);
        faqDao.deleteByUuid(uuid);
    }


    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public String bulkUploadFAQs(MultipartFile file) throws IOException {
        List<Map<String, Object>> errorRows = new ArrayList<>();
        Map<String, FAQCategory> categoryCache = new HashMap<>();
        // Pre-fill category cache
        List<FAQCategory> allCategories = faqCategoryDao.findAll();
        for (FAQCategory category : allCategories) {
            String cacheKey = category.getCategory().toLowerCase() + "_" + category.getLanguageCode();
            System.out.println("categrory cacheKey --" + cacheKey+"--"+category);
            categoryCache.put(cacheKey, category);
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Get header row
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // Skip header row
            rows.next();

            String currentUser = userService.getCurrentUser().getUsername();


            while (rows.hasNext()) {
                Row row = rows.next();
                Map<String, Object> rowData = new LinkedHashMap<>();
                
                // Store all cell values
                for (int i = 0; i < headers.size(); i++) {
                    rowData.put(headers.get(i), getStringCellValue(row.getCell(i)));
                }

                String categoryName = (String) rowData.get("Categories");
                String question = (String) rowData.get("Question");
                String answer = (String) rowData.get("Answer");
                String languageCode = (String) rowData.get("language_code");

                // Validate required fields
                if (isBlank(categoryName) || isBlank(question) || isBlank(answer) || isBlank(languageCode)) {
                    rowData.put("Error", "Missing required fields");
                    errorRows.add(rowData);
                    continue;
                }

                try {
                    // Get or create category                  
                    String catCacheKey = categoryName.toLowerCase() + "_" + languageCode;
                    FAQCategory category = categoryCache.get(catCacheKey);
                    
                    System.out.println("catCacheKey --" +catCacheKey + "--"+category);
                    if(category == null) {
                        category = createFAQCategory(categoryName, languageCode);
                        if(category != null) {
                            categoryCache.put(catCacheKey, category);
                        } else {
                            System.out.println("Category with name - " + categoryName + " creation failed");
                            continue;
                        }                       
                    }
                    
                    // Check for duplicate question in same category and language
                    if (isDuplicateQuestion(category.getId(), question, languageCode)) {
                        rowData.put("Error", "Duplicate question in same category and language");
                        errorRows.add(rowData);
                        continue;
                    }

                    // Create FAQ
                    FAQ faq = new FAQ();
                    faq.setCategory(category);
                    faq.setCreatedBy(currentUser);
                    faq.setUpdatedBy(currentUser);

                    // Create Translation
                    FAQTranslation translation = new FAQTranslation();
                    translation.setQuestion(question);
                    translation.setAnswer(answer);
                    
                    Language language = faqDao.findLanguageByCode(languageCode)
                            .orElseThrow(() -> new RuntimeException("Language not found: " + languageCode));
                    translation.setLanguage(language);
                    
                    faq.addTranslation(translation);
                    
                    faqDao.save(faq);

                } catch (Exception e) {
                    rowData.put("Error", e.getMessage());
                    errorRows.add(rowData);
                }
            }
        }

        if (!errorRows.isEmpty()) {
            return generateAndUploadErrorReport(errorRows);
        }

        return null;
    }

    private FAQCategory createFAQCategory(String categoryName, String languageCode) {
        String cacheKey = categoryName.toLowerCase() + "_" + languageCode;       
        FAQCategory category = null;

        if (category == null) {
            // Category doesn't exist in cache, create a new one
            category = new FAQCategory();
            category.setCategory(categoryName);
            category.setLanguageCode(languageCode);
            Integer maxExtId = faqCategoryDao.getMaxExtId();
            category.setExtId(maxExtId == null ? 1 : maxExtId + 1);
            category = faqCategoryDao.save(category);            
        }

        return category;
    }


    private boolean isDuplicateQuestion(Long categoryId, String question, String languageCode) {
        // Convert question to lowercase for case-insensitive comparison
        String lowercaseQuestion = question.toLowerCase().trim();

        // Query the database for existing FAQs in the given category and language
        List<FAQ> existingFAQs = faqDao.findByCategoryId(categoryId);

        // Check if any existing FAQ has a matching question in the given language
        for (FAQ faq : existingFAQs) {
            for (FAQTranslation translation : faq.getTranslations()) {
                if (translation.getLanguage().getCode().equals(languageCode) &&
                    translation.getQuestion().toLowerCase().trim().equals(lowercaseQuestion)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String generateAndUploadErrorReport(List<Map<String, Object>> errorRows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) { 
            Sheet sheet = workbook.createSheet("Error Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            List<String> headers = new ArrayList<>(errorRows.get(0).keySet());
            
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }

            // Populate error rows
            for (int i = 0; i < errorRows.size(); i++) {
                Map<String, Object> rowData = errorRows.get(i);
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    Object value = rowData.get(header);
                    if (value != null) {
                        row.createCell(j).setCellValue(value.toString());
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Generate file name with current date and time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String fileName = "faq/faq_errors_" + now.format(formatter) + ".xlsx";

            // Write to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            // Upload to S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(outputStream.size());
            metadata.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));

            // Generate and return the S3 URL
            return amazonS3Client.getUrl(bucketName, fileName).toString();
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
package com.tl.reap_admin_api.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tl.reap_admin_api.dao.BankDao;
import com.tl.reap_admin_api.dao.LanguageDao;
import com.tl.reap_admin_api.dao.RsetiDao;
import com.tl.reap_admin_api.dao.StateDao;
import com.tl.reap_admin_api.dto.RsetiDto;
import com.tl.reap_admin_api.dto.RsetiListDto;
import com.tl.reap_admin_api.dto.RsetiTranslationDto;
import com.tl.reap_admin_api.exception.DuplicateExtIdException;
import com.tl.reap_admin_api.mapper.RsetiMapper;
import com.tl.reap_admin_api.model.Bank;
import com.tl.reap_admin_api.model.District;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.Role;
import com.tl.reap_admin_api.model.RsetiTranslation;
import com.tl.reap_admin_api.model.State;
import com.tl.reap_admin_api.model.User;

@Service
public class RsetiService {

    @Value("${aws.s3.crsimg.bucket-name}")
    private String bucketName;

    private AmazonS3 amazonS3Client;
    private final RsetiDao rsetiDao;
    private final LanguageDao languageDao;   
    private StateDao stateDao;
    private BankDao bankDao;
    private UserService userService;

    private final RsetiMapper rsetiMapper;

    private static final String DEFAULT_LANGUAGE_CODE = "1";

    @Autowired
    public RsetiService(RsetiDao rsetiDao, LanguageDao languageDao, RsetiMapper rsetiMapper, StateDao stateDao, BankDao bankDao, AmazonS3 amazonS3Client, UserService userService) {
        this.rsetiDao = rsetiDao;
        this.languageDao = languageDao;
        this.rsetiMapper = rsetiMapper;
        this.stateDao = stateDao;
        this.bankDao = bankDao;
        this.amazonS3Client = amazonS3Client;
        this.userService = userService;
    }

   
    private Role getCurrentUserRole() {
        User currentUser = userService.getCurrentUser();
        return currentUser.getRole();
    }

    @Transactional(readOnly = true)
    public List<RsetiDto> getAllRsetis() {
        List<RSETI> rsetis = rsetiDao.findAll();
        return rsetis.stream().map(rsetiMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RsetiListDto> getAllRsetisWithCourseCount() {

        Role currentUserRole = getCurrentUserRole();
        List<Object[]> results;
        switch (currentUserRole) {
            case PUBLIC:
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
                results = rsetiDao.findAllWithCourseCount();
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                Integer stateId = userService.getCurrentUserStateId();
                results = rsetiDao.findAllWithCourseCountByState(stateId);
                break;
            case RSETI_ADMIN:
            case RSETI_STAFF:
                UUID rsetiId = userService.getCurrentUserRsetiId();
                results = rsetiDao.findAllWithCourseCountByRseti(rsetiId);
                break;
            default:
                results = rsetiDao.findAllWithCourseCount();
                break;
        }


       
        Map<UUID, RsetiListDto> rsetiMap = new HashMap<>();

        for (Object[] result : results) {
            RSETI rseti = (RSETI) result[0];
            Long courseCount = (Long) result[1];
            RsetiTranslation translation = (RsetiTranslation) result[2];

            RsetiListDto dto = rsetiMap.computeIfAbsent(rseti.getUuid(), uuid -> new RsetiListDto(
                rseti.getUuid(),
                rseti.getExtId(),
                rseti.getEmail(),
                rseti.getContactNo(),
                rseti.getDirectorContactNo(),
                rseti.getStateId(),
                rseti.getBankId(),
                courseCount.intValue(),
                new ArrayList<>()
            ));

            dto.getTranslations().add(rsetiMapper.toTranslationDTO(translation));
        }

        return new ArrayList<>(rsetiMap.values());
    }
    
    @Transactional(readOnly = true)
    public RsetiDto getRsetiByUuid(UUID uuid) {
        Role currentUserRole = getCurrentUserRole();
        RSETI rseti;

        switch (currentUserRole) {
            case PUBLIC:
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
                rseti = rsetiDao.findByUuid(uuid).orElse(null);
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                Integer stateId = userService.getCurrentUserStateId();
                rseti = rsetiDao.findByUuidAndState(uuid, stateId).orElse(null);
                break;
            case RSETI_ADMIN:
            case RSETI_STAFF:
                UUID rsetiId = userService.getCurrentUserRsetiId();
                rseti = rsetiDao.findByUuidAndRseti(uuid, rsetiId).orElse(null);
                break;
            default:
                rseti = rsetiDao.findByUuid(uuid).orElse(null);
                break;
        }

        return rseti != null ? rsetiMapper.toDTO(rseti) : null;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public RsetiDto createRseti(RsetiDto rsetiDto) throws DuplicateExtIdException {
        if (rsetiDao.existsByExtId(rsetiDto.getExtId())) {
            throw new DuplicateExtIdException("RSETI with extId " + rsetiDto.getExtId() + " already exists");
        }

        checkPermission(null, rsetiDto.getStateId());

        RSETI rseti = rsetiMapper.toEntity(rsetiDto);
        rseti.setUuid(UUID.randomUUID()); 
        rseti.setUpdatedAt(ZonedDateTime.now());
        rseti.setCreatedAt(ZonedDateTime.now());
     // Get the current user
        User currentUser = userService.getCurrentUser();
        rseti.setUpdatedBy(currentUser.getUsername());
        rseti.setCreatedBy(currentUser.getUsername());

        if (rsetiDto.getTranslations() != null && !rsetiDto.getTranslations().isEmpty()) {
            createTranslations(rseti, rsetiDto.getTranslations());
        }

        setLanguagesForTranslations(rseti);
        RSETI savedRseti = rsetiDao.save(rseti);
        return rsetiMapper.toDTO(savedRseti);
    }

    private void createTranslations(RSETI rseti, List<RsetiTranslationDto> translationDtos) {
        for (RsetiTranslationDto translationDto : translationDtos) {
            RsetiTranslation translation = new RsetiTranslation();
            translation.setRseti(rseti);
            Language language = languageDao.findByCode(translationDto.getLanguageCode())
                .orElseThrow(() -> new RuntimeException("Language not found: " + translationDto.getLanguageCode()));
            translation.setLanguage(language);
            translation.setDistrict(translationDto.getDistrict());
            if (translationDto.getDistrictId() != null) translation.setDistrictId(translationDto.getDistrictId());
            translation.setName(translationDto.getName());
            translation.setAddress(translationDto.getAddress());
            translation.setDirectorName(translationDto.getDirectorName());
            rseti.addTranslation(translation);
        }
    }


    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public RsetiDto updateRseti(UUID uuid, RsetiDto rsetiDto) {
        RSETI rseti = rsetiDao.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RSETI not found"));
        
        checkPermission(uuid, rsetiDto.getStateId());
        // Update main RSETI fields
        rsetiMapper.updateEntityFromDTO(rsetiDto, rseti);
        
        // Handle translations
        if (rsetiDto.getTranslations() != null) {
            updateRsetiTranslations(rseti, rsetiDto.getTranslations());
        }
        
        setLanguagesForTranslations(rseti);
        
        RSETI updatedRseti = rsetiDao.save(rseti);
       
        // Get the current user
        User currentUser = userService.getCurrentUser();
        updatedRseti.setUpdatedBy(currentUser.getUsername());
        updatedRseti.setUpdatedAt(ZonedDateTime.now());
        return rsetiMapper.toDTO(updatedRseti);
    }

    private void updateRsetiTranslations(RSETI rseti, List<RsetiTranslationDto> translationDtos) {
        Map<String, RsetiTranslation> existingTranslations = rseti.getTranslations().stream()
                .collect(Collectors.toMap(t -> t.getLanguage().getCode(), t -> t));

        for (RsetiTranslationDto translationDto : translationDtos) {
            RsetiTranslation translation = existingTranslations.get(translationDto.getLanguageCode());
            if (translation == null) {
                // Create new translation if it doesn't exist
                translation = rsetiMapper.toTranslationEntity(translationDto);
                translation.setRseti(rseti);
                rseti.getTranslations().add(translation);
            } else {
                // Update existing translation
                rsetiMapper.updateTranslationFromDTO(translationDto, translation);
            }
        }
    }

    private void setLanguagesForTranslations(RSETI rseti) {
        for (RsetiTranslation translation : rseti.getTranslations()) {
            String languageCode = translation.getLanguage().getCode();
            if (languageCode == null || languageCode.isEmpty()) {
                throw new IllegalArgumentException("Language code is missing for a translation");
            }
            Language language = languageDao.findByCode(languageCode)
                .orElseThrow(() -> new RuntimeException("Language not found: " + languageCode));
            translation.setLanguage(language);
        }
    }


    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public void deleteRseti(UUID uuid) {
        RSETI rseti = rsetiDao.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RSETI not found"));
        checkPermission(uuid, rseti.getStateId());
        // Get the current user
        User currentUser = userService.getCurrentUser();
        rseti.setUpdatedBy(currentUser.getUsername());
        rseti.setUpdatedAt(ZonedDateTime.now());
        rsetiDao.deleteByUuid(uuid);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public List<RsetiDto> getRsetisByStateId(UUID stateId) {
        List<RSETI> rsetis = rsetiDao.findByStateId(stateId);
        
        return rsetis.stream().map(rsetiMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public JSONObject bulkUploadRsetis(MultipartFile file) throws IOException {
        JSONObject respObj = new JSONObject();
		List<Map<String, Object>> errorRows = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        String errorMsg = "";
        int count = 0;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Get header row
			Row headerRow = sheet.getRow(2); // Assuming the header is on the 3rd row (index 1)
			
			for (Cell cell : headerRow) {
                CellType ct = cell.getCellType();
                if (ct != CellType.STRING) {
                    throw new RuntimeException("Wrong File Template. Expecting Headers in row 3");
                }
				headers.add(cell.getStringCellValue());
			}
            // Skip header row
            if (rows.hasNext()) {
                rows.next();
            }

             // Fetch all languages and create a map
            //    Map<String, Language> languageMap = languageDao.findAll().stream()
            //       .collect(Collectors.toMap(Language::getCode, language -> language));

            Language defaultLanguage = languageDao.findById(Long.valueOf(DEFAULT_LANGUAGE_CODE))
            .orElseThrow(() -> new RuntimeException("Default language not found"));

            
            // Fetch all states and create a map
            Map<String, State> stateMap = stateDao.findAllByLanguageCode(defaultLanguage.getCode()).stream()
                .collect(Collectors.toMap(State::getName, state -> state));

            // Fetch all banks and create a map
            Map<String, Bank> bankMap = bankDao.findAll().stream()
                .collect(Collectors.toMap(Bank::getName, bank -> bank));



            while (rows.hasNext()) {
                Row row = rows.next();
                if (row.getRowNum() < 3) continue; // Skip header rows

                Map<String, Object> rowData = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    rowData.put(headers.get(i), getStringCellValue(row.getCell(i)));
                }

                try {                    

                    String stateName = getStringCellValue(row.getCell(1)); // State Name
                    if(stateName == null || stateName.isEmpty() || stateName.length()<=0) continue;
                    
                    RSETI rseti = new RSETI();
                    

                    rseti.setUuid(UUID.randomUUID());
                    rseti.setCreatedAt(ZonedDateTime.now());
            		rseti.setUpdatedAt(ZonedDateTime.now());
                    User currentUser = userService.getCurrentUser();
                    rseti.setCreatedBy(currentUser.getUsername());
                    rseti.setUpdatedBy(currentUser.getUsername());
                    rseti.setExtId(getStringCellValue(row.getCell(5))); // Institute ID
                    rseti.setEmail(getStringCellValue(row.getCell(4))); // RSETI E-mail ID
                    rseti.setContactNo(getStringCellValue(row.getCell(8))); // RSETI Contact no
                    rseti.setDirectorContactNo(getStringCellValue(row.getCell(10))); // Director Contact No

                   
                    State state = stateMap.get(stateName);
                    if (state == null) {
                        System.out.println("statename not found for row - " + row.getRowNum() + " state --"+stateName +"-- "+ stateName.length()
                        + "-- email--" + rseti.getEmail()+"-- institude id --"+ rseti.getExtId());

                        rowData.put("Error", "State not found");
                        errorRows.add(rowData);
                        continue;                   
                    }
                    rseti.setStateId(state.getExtId());

                    String bankName = getStringCellValue(row.getCell(6)); // Bank Name
                    Bank bank = bankMap.get(bankName);
                    if (bank == null) {
                        System.out.println("bankName not found for row - " + row.getRowNum() + " state --"+stateName 
                            + "-- email--" + rseti.getEmail()+"-- institude id --"+ rseti.getExtId());
                        rowData.put("Error", "Bank not found");
                        errorRows.add(rowData);
                        continue;                 
                    }
                    rseti.setBankId(bank.getUuid());

                    String districtName = getStringCellValue(row.getCell(2));
                    Integer districtId = 0;
                    District district = state.getDistricts().stream()
                            .filter(d -> d.getName().equals(districtName))
                            .findFirst()
                            .orElse(null);
                    if (district != null) {
                        districtId = district.getExtId();
                    }
                  
                    // Create default translation
                    RsetiTranslation translation = new RsetiTranslation();
                    translation.setLanguage(defaultLanguage);
                    translation.setName(getStringCellValue(row.getCell(3)));
                    translation.setDistrict(districtName); // District Name
                    translation.setDistrictId(districtId);
                    translation.setAddress(getStringCellValue(row.getCell(7))); // Address of RSETI
                    translation.setDirectorName(getStringCellValue(row.getCell(9))); // Director name
                    rseti.addTranslation(translation);

                    rsetiDao.save(rseti);
                    count++;
                } catch (Exception e) {
                    System.out.println("Error processing row: " + e.getMessage());
                    e.printStackTrace();
                    rowData.put("Error", "Exception : " +e.getMessage());
                    errorRows.add(rowData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            count = 0; //If exception occurs all saves are rolled back
            errorMsg += e.getMessage();
           // throw new RuntimeException("Error reading the file", e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            count = 0; //If exception occurs all saves are rolled back
            errorMsg = e.getMessage();
            //throw new RuntimeException("Error processing the file", e);
        } 

        String responsFileUrl = "";
		if (!errorRows.isEmpty()) {
            responsFileUrl = generateAndUploadErrorReport(errorRows, headers);
        }

        errorMsg = (count == 0) ? "Failed -" + errorMsg:"Success";
		respObj.put("errorMsg",errorMsg);
		respObj.put("count", count);			
		respObj.put("errorFileUrl",responsFileUrl );

        return respObj;
    }

    private String generateAndUploadErrorReport(List<Map<String, Object>> errorRows, List<String> headers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Error Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }
            headerRow.createCell(headers.size()).setCellValue("Error");

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
                row.createCell(headers.size()).setCellValue((String) rowData.get("Error"));
            }

            // Auto-size columns
            for (int i = 0; i <= headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            // Generate file name with current date and time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String fileName = "rsetis/rsetibulkupload_errors_" + now.format(formatter) + ".xlsx";

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


    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private void checkPermission(UUID rsetiUuid, Integer stateId) {
        User currentUser = userService.getCurrentUser();
        Role userRole = currentUser.getRole();

        switch (userRole) {
			case PUBLIC:
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
                // These roles have access to all RSETIs
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                if (!stateId.equals(currentUser.getUserProfile().getState().getExtId())) {
                    throw new AccessDeniedException("You don't have permission to access this RSETI Course");
                }
                break;
            case RSETI_ADMIN:
            case RSETI_STAFF:              
            default:
                throw new AccessDeniedException("You don't have permission to access RSETI Course data");
        }
    }
}
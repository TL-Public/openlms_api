package com.tl.reap_admin_api.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tl.reap_admin_api.dao.TraineeCredentialDao;
import com.tl.reap_admin_api.dao.TraineeProfileDao;
import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.TraineeCredentialDto;
import com.tl.reap_admin_api.dto.TraineeProfileDto;
import com.tl.reap_admin_api.exception.UserNotFoundException;
import com.tl.reap_admin_api.mapper.CourseMapper;
import com.tl.reap_admin_api.mapper.TraineeProfileMapper;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.Role;
import com.tl.reap_admin_api.model.RsetiCourse;
import com.tl.reap_admin_api.model.TraineeCredential;
import com.tl.reap_admin_api.model.TraineeProfile;
import com.tl.reap_admin_api.model.TraineeRseti;
import com.tl.reap_admin_api.model.User;
import com.tl.reap_admin_api.security.UserPrincipal;
import com.tl.reap_admin_api.util.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;


@Service
public class TraineeProfileService {

	@Autowired
	private TraineeProfileDao traineeProfileDAO;

	@Autowired
	private TraineeCredentialDao traineeCredentialDao;
	
	 @Autowired
	 private TraineeProfileDao traineeProfileDao;

	@Autowired
	private TraineeProfileMapper traineeProfileMapper;

	@Autowired
	private UserService userService;
	
	@Autowired
	private CourseMapper courseMapper;

	@Autowired
    private TraineeCredentialService traineeService;
	
	private static final String DEFAULT_PASSWORD = "123456";
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	 private static final Logger logger = LoggerFactory.getLogger(TraineeProfileService.class);

	 @Value("${aws.s3.crsimg.bucket-name}")
	 private String bucketName;

    @Autowired
    private AmazonS3 amazonS3Client;
    
    
    private static final List<String> EXPECTED_HEADERS = Arrays.asList(
            "Batch No.", "Candidate name", "Roll no", "Father / Husband name", "Marital Status",
            "Age", "Religion", "Caste", "Education", "Person with Disability", "Sex",
            "Poverty line", "Poverty line number / Ration Card No", "SECC", "SECC No",
            "PAN Number", "Residential", "Date of Birth", "Aadhaar Card No", "Landline STD",
            "Landline number", "Mobile number1", "Mobile number2", "SGSY candidate",
            "Family occupation", "Candidate present occupation", "Nativity area",
            "Candidate address", "Village", "Hobli", "District", "Taluk", "Pincode",
            "Candidate Sponsored by bank", "Sponsored bank name", "Sponsored bank branch",
            "Sponsored bank city", "Sponsor Name/Referral/NGO", "Relevant experience",
            "Name of SHG", "Family Member", "Email", "Mnrega Job Card No"
        );


	
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
	public List<TraineeProfileDto> getAllTraineeProfiles() {
		User currentUser = userService.getCurrentUser();
        List<TraineeProfile> traineeProfiles  = null;

        switch (currentUser.getRole()) {          
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
				traineeProfiles = traineeProfileDAO.findAll();
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                Integer stateId = userService.getCurrentUserStateId();
                traineeProfiles = traineeProfileDAO.findAllByStateId(stateId);                
                break;
            case RSETI_ADMIN:
            case RSETI_STAFF:
                UUID rsetiId = userService.getCurrentUserRsetiId();
                traineeProfiles = traineeProfileDAO.findAllByRsetiId(rsetiId);
                break;
			case PUBLIC:
            default:
                throw new AccessDeniedException("You don't have permission to access User list");               
        }

		
		return traineeProfiles.stream().map(traineeProfileMapper::toDTO).collect(Collectors.toList());
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
	public TraineeProfileDto getTraineeProfileByUuid(UUID uuid) {
		TraineeProfile traineeProfile = traineeProfileDAO.findByUuid(uuid);
		checkPermission(traineeProfile);

		if (traineeProfile == null) {
			throw new EntityNotFoundException("TraineeProfile not found with uuid: " + uuid);
		}
		return traineeProfileMapper.toDTO(traineeProfile);
	}

	@Transactional	
	public TraineeProfileDto getCurrentUserProfile() {
		TraineeCredential  traineeCredential = null;
		UserPrincipal currentUser = getCurrentUser();

		Optional<TraineeCredential> traineeCredentialOpt = traineeCredentialDao.findByUuid(currentUser.getUuid());
		if(traineeCredentialOpt.isEmpty())
		{
			throw new UserNotFoundException("Trainee not found for trainee with UUID: " + currentUser.getUuid());
		}

		traineeCredential = traineeCredentialOpt.get();
		TraineeProfile traineeProfile = traineeProfileDAO.findByTraineeCredential(traineeCredential.getId());
		return traineeProfileMapper.toDTO(traineeProfile);
	}
	
	
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
	public TraineeProfileDto createTraineeProfile(TraineeProfileDto traineeProfileDto) {
		TraineeProfile traineeProfile = traineeProfileMapper.toEntity(traineeProfileDto);
		
		if(traineeProfile == null) {
			throw new UserNotFoundException("Trainee not found for trainee with usernmar: " + traineeProfileDto.getUsername());
		}
		traineeProfile.setUuid(UUID.randomUUID());
		traineeProfile.setCreatedAt(ZonedDateTime.now());
		traineeProfile.setUpdatedAt(ZonedDateTime.now());
	        User currentUser = userService.getCurrentUser();
	        traineeProfile.setCreatedBy(currentUser.getUsername());
	        traineeProfile.setUpdatedBy(currentUser.getUsername());
		checkPermission(traineeProfile);

		TraineeProfile savedTraineeProfile = traineeProfileDAO.save(traineeProfile);
		
		return traineeProfileMapper.toDTO(savedTraineeProfile);
	}	

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
	public boolean deleteTraineeProfile(UUID uuid) {
		TraineeProfile traineeProfile = traineeProfileDAO.findByUuid(uuid);
		 // Get the current user
        User currentUser = userService.getCurrentUser();
        traineeProfile.setUpdatedBy(currentUser.getUsername());
        traineeProfile.setUpdatedAt(ZonedDateTime.now());

		checkPermission(traineeProfile);

		if (traineeProfile != null) {
			traineeProfileDAO.delete(traineeProfile);
			return true;
		}
		return false;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
	public TraineeProfileDto updateTraineeProfile(UUID uuid, TraineeProfileDto traineeProfileDto) {
		// Retrieve the existing trainee profile by UUID
		TraineeProfile existingProfile = traineeProfileDAO.findByUuid(uuid);
		 // Get the current user
        User currentUser = userService.getCurrentUser();
        existingProfile.setUpdatedBy(currentUser.getUsername());
        existingProfile.setUpdatedAt(ZonedDateTime.now());

		checkPermission(existingProfile);

		if (existingProfile == null) {
			throw new EntityNotFoundException("TraineeProfile not found with uuid: " + uuid);
		}

		// Update only the fields that are not null in the DTO
		if (traineeProfileDto.getEnrollId() != null) {
			existingProfile.setEnrollId(traineeProfileDto.getEnrollId());
		}
		if (traineeProfileDto.getStatus() != null) {
			existingProfile.setStatus(traineeProfileDto.getStatus());
		}
		if (traineeProfileDto.getCandidateName() != null) {
			existingProfile.setCandidateName(traineeProfileDto.getCandidateName());
		}
		if (traineeProfileDto.getFatherNameOrHusbandName() != null) {
			existingProfile.setFatherNameOrHusbandName(traineeProfileDto.getFatherNameOrHusbandName());
		}
		if (traineeProfileDto.getMaritalStatus() != null) {
			existingProfile.setMaritalStatus(traineeProfileDto.getMaritalStatus());
		}
		if (traineeProfileDto.getAge() != null) {
			existingProfile.setAge(traineeProfileDto.getAge());
		}
		if (traineeProfileDto.getReligion() != null) {
			existingProfile.setReligion(traineeProfileDto.getReligion());
		}
		if (traineeProfileDto.getCaste() != null) {
			existingProfile.setCaste(traineeProfileDto.getCaste());
		}
		if (traineeProfileDto.getEducation() != null) {
			existingProfile.setEducation(traineeProfileDto.getEducation());
		}
		if (traineeProfileDto.getPersonWithDisability() != null) {
			existingProfile.setPersonWithDisability(traineeProfileDto.getPersonWithDisability());
		}
		if (traineeProfileDto.getSex() != null) {
			existingProfile.setSex(traineeProfileDto.getSex());
		}
		if (traineeProfileDto.getPovertyLine() != null) {
			existingProfile.setPovertyLine(traineeProfileDto.getPovertyLine());
		}
		if (traineeProfileDto.getPovertyLineNumberOrRationCardNumber() != null) {
			existingProfile
					.setPovertyLineNumberOrRationCardNumber(traineeProfileDto.getPovertyLineNumberOrRationCardNumber());
		}
		if (traineeProfileDto.getSecc() != null) {
			existingProfile.setSecc(traineeProfileDto.getSecc());
		}
		if (traineeProfileDto.getSeccNo() != null) {
			existingProfile.setSeccNo(traineeProfileDto.getSeccNo());
		}
		if (traineeProfileDto.getPanNumber() != null) {
			existingProfile.setPanNumber(traineeProfileDto.getPanNumber());
		}
		if (traineeProfileDto.getResidential() != null) {
			existingProfile.setResidential(traineeProfileDto.getResidential());
		}
		if (traineeProfileDto.getDateOfBirth() != null) {
			existingProfile.setDateOfBirth(traineeProfileDto.getDateOfBirth());
		}
		if (traineeProfileDto.getAadharCardNo() != null) {
			existingProfile.setAadharCardNo(traineeProfileDto.getAadharCardNo());
		}
		if (traineeProfileDto.getLandlineStd() != null) {
			existingProfile.setLandlineStd(traineeProfileDto.getLandlineStd());
		}
		if (traineeProfileDto.getLandlineNumber() != null) {
			existingProfile.setLandlineNumber(traineeProfileDto.getLandlineNumber());
		}
		if (traineeProfileDto.getMobileNumber1() != null) {
			existingProfile.setMobileNumber1(traineeProfileDto.getMobileNumber1());
		}
		if (traineeProfileDto.getMobileNumber2() != null) {
			existingProfile.setMobileNumber2(traineeProfileDto.getMobileNumber2());
		}
		if (traineeProfileDto.getSgsyCandidate() != null) {
			existingProfile.setSgsyCandidate(traineeProfileDto.getSgsyCandidate());
		}
		if (traineeProfileDto.getFamilyOccupation() != null) {
			existingProfile.setFamilyOccupation(traineeProfileDto.getFamilyOccupation());
		}
		if (traineeProfileDto.getCandidatePresentOccupation() != null) {
			existingProfile.setCandidatePresentOccupation(traineeProfileDto.getCandidatePresentOccupation());
		}
		if (traineeProfileDto.getNativityArea() != null) {
			existingProfile.setNativityArea(traineeProfileDto.getNativityArea());
		}
		if (traineeProfileDto.getCandidateAddress() != null) {
			existingProfile.setCandidateAddress(traineeProfileDto.getCandidateAddress());
		}
		if (traineeProfileDto.getVillage() != null) {
			existingProfile.setVillage(traineeProfileDto.getVillage());
		}
		if (traineeProfileDto.getHobli() != null) {
			existingProfile.setHobli(traineeProfileDto.getHobli());
		}
		if (traineeProfileDto.getDistrict() != null) {
			existingProfile.setDistrict(traineeProfileDto.getDistrict());
		}
		if (traineeProfileDto.getTaluk() != null) {
			existingProfile.setTaluk(traineeProfileDto.getTaluk());
		}
		if (traineeProfileDto.getPincode() != null) {
			existingProfile.setPincode(traineeProfileDto.getPincode());
		}
		if (traineeProfileDto.getCandidateSponsoredByBank() != null) {
			existingProfile.setCandidateSponsoredByBank(traineeProfileDto.getCandidateSponsoredByBank());
		}
		if (traineeProfileDto.getSponsoredBankName() != null) {
			existingProfile.setSponsoredBankName(traineeProfileDto.getSponsoredBankName());
		}
		if (traineeProfileDto.getSponsoredBankBranch() != null) {
			existingProfile.setSponsoredBankBranch(traineeProfileDto.getSponsoredBankBranch());
		}
		if (traineeProfileDto.getSponsoredBankCity() != null) {
			existingProfile.setSponsoredBankCity(traineeProfileDto.getSponsoredBankCity());
		}
		if (traineeProfileDto.getSponsorName() != null) {
			existingProfile.setSponsorName(traineeProfileDto.getSponsorName());
		}
		if (traineeProfileDto.getRelevantExperience() != null) {
			existingProfile.setRelevantExperience(traineeProfileDto.getRelevantExperience());
		}
		if (traineeProfileDto.getNameOfShg() != null) {
			existingProfile.setNameOfShg(traineeProfileDto.getNameOfShg());
		}
		if (traineeProfileDto.getFamilyMember() != null) {
			existingProfile.setFamilyMember(traineeProfileDto.getFamilyMember());
		}
		if (traineeProfileDto.getMnergaCardNo() != null) {
			existingProfile.setMnergaCardNo(traineeProfileDto.getMnergaCardNo());
		}
		
		if (traineeProfileDto.getEnrolledOn() != null) {
			existingProfile.setEnrolledOn(traineeProfileDto.getEnrolledOn());
		}
		
		 if (traineeProfileDto.getUsername() != null) {
	            TraineeCredential traineeCredential = existingProfile.getTrainee();
	            if (traineeCredential != null) {
	                traineeCredential.setUsername(traineeProfileDto.getUsername());
	            }
	        }
		 
		traineeProfileDAO.save(existingProfile);
		// Convert the updated entity to DTO and return
		return traineeProfileMapper.toDTO(existingProfile);
	}

	
    public UserPrincipal getCurrentUser() {
        UserPrincipal userPrincipal = SecurityUtils.getCurrentUser();
        if (userPrincipal == null) {
            throw new RuntimeException("No authenticated user found");
        }

       return userPrincipal; 	
    }

	@Transactional(rollbackFor = Exception.class)
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public Map<String, Object> bulkUploadTraineeProfiles(MultipartFile file) throws IOException {
		logger.info("Starting bulk upload process");
		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> errorRows = new ArrayList<>();
		List<String> headers = new ArrayList<>();
		String errorMsg = "";
		int count = 0;

		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);

			Row headerRow = findHeaderRow(sheet);
			if (headerRow == null) {
				throw new IllegalArgumentException("Header row not found in the first 3 rows:invalid template");
			}

			headers = getHeaders(headerRow);
			
			 // Validate template
            List<String> missingHeaders = validateTemplate(headers);
            if (!missingHeaders.isEmpty()) {
                throw new IllegalArgumentException("Invalid template. Missing headers: " + String.join(", ", missingHeaders));
            }
			int dataStartRow = headerRow.getRowNum() + 1;
			for (int rowIndex = dataStartRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (isEmptyRow(row))
					continue;

				Map<String, Object> rowData = new HashMap<>();
				try {
					TraineeProfile traineeProfile = createTraineeProfileFromRow(row);
					TraineeCredential traineeCredential = createTraineeCredential(traineeProfile);
					// Check if email already exists
                    Optional<TraineeCredential> existingCredential = traineeCredentialDao.findByEmail(traineeCredential.getEmail());
                    if (existingCredential.isPresent()) {
                        throw new IllegalStateException("Email already exists: " + traineeCredential.getEmail());
                    }
					traineeCredential = traineeCredentialDao.save(traineeCredential);
					traineeProfile.setTrainee(traineeCredential);
					traineeProfileDao.save(traineeProfile);

					count++;
					logger.info("Created new trainee profile and credential for enrollId: {}",
							traineeProfile.getEnrollId());
				} catch (Exception e) {
					logger.error("Error processing row {}: {}", rowIndex, e.getMessage());
					rowData.put("Row", rowIndex + 1);
					rowData.put("Error", e.getMessage());
					for (int i = 0; i < headers.size(); i++) {
						rowData.put(headers.get(i), getStringCellValue(row.getCell(i)));
					}
					errorRows.add(rowData);
				}
			}
			
		} catch (IllegalArgumentException e) {
            logger.error("Template validation error: {}", e.getMessage());
            errorMsg = e.getMessage();
        } catch (Exception e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            errorMsg = "Failed to process file: " + e.getMessage();
        }
		int totalRows = count + errorRows.size();
		response.put("count", count);
		response.put("totalRows", count + errorRows.size());
		response.put("errors", errorRows);
		
		if (errorMsg.startsWith("Invalid template")) {
            response.put("errorMsg", errorMsg);
        } else if (totalRows == 0) {
            response.put("errorMsg", errorMsg.isEmpty() 
                ? "Failed - No rows were processed. The file might be empty or contain only headers." 
                : errorMsg);
        } else if (count == 0) {
            response.put("errorMsg", "Failed - All " + totalRows + " rows contain errors. Common issues: " + getCommonErrors(errorRows));
        } else if (count < totalRows) {
            response.put("errorMsg", "Partial Success - " + count + " out of " + totalRows + " rows processed successfully. " + errorRows.size() + " rows contain errors.");
        } else {
            response.put("errorMsg", "Success - All " + totalRows + " rows processed successfully.");
        }

        if (!errorRows.isEmpty()) {
            String errorReportUrl = generateAndUploadErrorReport(errorRows, headers);
            response.put("errorFileUrl", errorReportUrl);
        } else {
            response.put("errorFileUrl", "");
        }

        return response;
    }
	
	 private List<String> validateTemplate(List<String> headers) {
	        return EXPECTED_HEADERS.stream()
	                .filter(header -> !headers.contains(header))
	                .collect(Collectors.toList());
	    }
	

	private TraineeProfile createTraineeProfileFromRow(Row row) {
		TraineeProfile profile = new TraineeProfile();
		profile.setUuid(UUID.randomUUID());

		profile.setBatchNo(getStringCellValue(row.getCell(0)));
		profile.setCandidateName(getStringCellValue(row.getCell(1)));
		profile.setEnrollId(getStringCellValue(row.getCell(2)));
		profile.setFatherNameOrHusbandName(getStringCellValue(row.getCell(3)));
		profile.setMaritalStatus(getStringCellValue(row.getCell(4)));
		profile.setAge(parseInteger(getStringCellValue(row.getCell(5))));
		profile.setReligion(getStringCellValue(row.getCell(6)));
		profile.setCaste(getStringCellValue(row.getCell(7)));
		profile.setEducation(getStringCellValue(row.getCell(8)));
		profile.setPersonWithDisability(parseBoolean(getStringCellValue(row.getCell(9))));
		profile.setSex(getStringCellValue(row.getCell(10)));
		profile.setPovertyLine(getStringCellValue(row.getCell(11)));
		profile.setPovertyLineNumberOrRationCardNumber(getStringCellValue(row.getCell(12)));
		profile.setSecc(getStringCellValue(row.getCell(13)));
		profile.setSeccNo(getStringCellValue(row.getCell(14)));
		profile.setPanNumber(getStringCellValue(row.getCell(15)));
		profile.setResidential(getStringCellValue(row.getCell(16)));
		profile.setDateOfBirth(parseDate(getStringCellValue(row.getCell(17))));
		profile.setAadharCardNo(getStringCellValue(row.getCell(18)));
		profile.setLandlineStd(getStringCellValue(row.getCell(19)));
		profile.setLandlineNumber(getStringCellValue(row.getCell(20)));
		profile.setMobileNumber1(getStringCellValue(row.getCell(21)));
		profile.setMobileNumber2(getStringCellValue(row.getCell(22)));
		profile.setSgsyCandidate(parseBoolean(getStringCellValue(row.getCell(23))));
		profile.setFamilyOccupation(getStringCellValue(row.getCell(24)));
		profile.setCandidatePresentOccupation(getStringCellValue(row.getCell(25)));
		profile.setNativityArea(getStringCellValue(row.getCell(26)));
		profile.setCandidateAddress(getStringCellValue(row.getCell(27)));
		profile.setVillage(getStringCellValue(row.getCell(28)));
		profile.setHobli(getStringCellValue(row.getCell(29)));
		profile.setDistrict(getStringCellValue(row.getCell(30)));
		profile.setTaluk(getStringCellValue(row.getCell(31)));
		profile.setPincode(getStringCellValue(row.getCell(32)));
		profile.setCandidateSponsoredByBank(parseBoolean(getStringCellValue(row.getCell(33))));
		profile.setSponsoredBankName(getStringCellValue(row.getCell(34)));
		profile.setSponsoredBankBranch(getStringCellValue(row.getCell(35)));
		profile.setSponsoredBankCity(getStringCellValue(row.getCell(36)));
		profile.setSponsorName(getStringCellValue(row.getCell(37)));
		profile.setRelevantExperience(getStringCellValue(row.getCell(38)));
		profile.setNameOfShg(getStringCellValue(row.getCell(39)));
		profile.setFamilyMember(getStringCellValue(row.getCell(40)));
		profile.setEmail(getStringCellValue(row.getCell(41)));
		profile.setMnergaCardNo(getStringCellValue(row.getCell(42)));

		profile.setStatus(1);
		profile.setEnrolledOn(LocalDate.now());

		return profile;
	}

	private TraineeCredential createTraineeCredential(TraineeProfile traineeProfile) {
		TraineeCredential traineeCredential = new TraineeCredential();
		traineeCredential.setUuid(UUID.randomUUID());
		traineeCredential.setUsername(traineeProfile.getEnrollId());
		traineeCredential.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
		traineeCredential.setEmail(traineeProfile.getEmail());
		traineeCredential.setCreatedAt(ZonedDateTime.now());
		traineeCredential.setUpdatedAt(ZonedDateTime.now());
		return traineeCredential;
	}

	private Row findHeaderRow(Sheet sheet) {
		for (int i = 0; i <= 2; i++) {
			Row row = sheet.getRow(i);
			if (row != null && isCellMatch(row.getCell(0), "Batch No.")) {
				return row;
			}
		}
		return null;
	}

	private List<String> getHeaders(Row headerRow) {
		List<String> headers = new ArrayList<>();
		for (Cell cell : headerRow) {
			headers.add(getStringCellValue(cell));
		}
		return headers;
	}

	private boolean isEmptyRow(Row row) {
		if (row == null)
			return true;

		for (int i = 0; i < 3; i++) {
			Cell cell = row.getCell(i);
			if (cell != null && !getStringCellValue(cell).trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private boolean isCellMatch(Cell cell, String expectedValue) {
		if (cell == null)
			return false;
		return expectedValue.equalsIgnoreCase(getStringCellValue(cell).trim());
	}

	private String getStringCellValue(Cell cell) {
		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue().trim();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getLocalDateTimeCellValue().toLocalDate().toString();
			}
			double numericValue = cell.getNumericCellValue();
			if (numericValue == Math.floor(numericValue)) {
				return String.valueOf((long) numericValue);
			}
			return String.valueOf(numericValue);
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			try {
				return cell.getStringCellValue().trim();
			} catch (Exception e) {
				return String.valueOf(cell.getNumericCellValue());
			}
		default:
			return "";
		}
	}

	private LocalDate parseDate(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty() || dateStr.equalsIgnoreCase("bn")) {
			return null;
		}

		String[] dateFormats = { "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy", "dd MMM yyyy",
				"dd MMMM yyyy" };

		for (String format : dateFormats) {
			try {
				return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
			} catch (Exception e) {
				continue;
			}
		}

		throw new IllegalArgumentException("Unable to parse date: " + dateStr);
	}

	private Integer parseInteger(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			double doubleValue = Double.parseDouble(value.trim());
			return (int) Math.round(doubleValue);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid integer value: " + value);
		}
	}

	private Boolean parseBoolean(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		String lowercaseValue = value.toLowerCase().trim();
		if (lowercaseValue.equals("yes") || lowercaseValue.equals("true") || lowercaseValue.equals("1")) {
			return true;
		} else if (lowercaseValue.equals("no") || lowercaseValue.equals("false") || lowercaseValue.equals("0")) {
			return false;
		} else {
			throw new IllegalArgumentException("Invalid boolean value: " + value);
		}
	}
	
	 private String getCommonErrors(List<Map<String, Object>> errorRows) {
	        Map<String, Integer> errorCounts = new HashMap<>();
	        for (Map<String, Object> row : errorRows) {
	            String error = (String) row.get("Error");
	            errorCounts.put(error, errorCounts.getOrDefault(error, 0) + 1);
	        }

	        List<String> commonErrors = errorCounts.entrySet().stream()
	                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
	                .limit(3)
	                .map(e -> e.getKey() + " (" + e.getValue() + " occurrences)")
	                .collect(Collectors.toList());

	        return String.join(", ", commonErrors);
	    }
	 
	 private String generateAndUploadErrorReport(List<Map<String, Object>> errorRows, List<String> headers) throws IOException {
	        try (Workbook workbook = new XSSFWorkbook()) {
	            Sheet sheet = workbook.createSheet("Error Report");

	            // Create header row
	            Row headerRow = sheet.createRow(0);
	            int colIndex = 0;
	            for (String header : headers) {
	                headerRow.createCell(colIndex++).setCellValue(header);
	            }
	            headerRow.createCell(colIndex).setCellValue("Error Message");

	            // Populate error rows
	            int rowIndex = 1;
	            for (Map<String, Object> errorRow : errorRows) {
	                Row row = sheet.createRow(rowIndex++);
	                colIndex = 0;

	                for (String header : headers) {
	                    Object value = errorRow.get(header);
	                    row.createCell(colIndex++).setCellValue(value != null ? value.toString() : "");
	                }

	                Object errorMessage = errorRow.get("Error");
	                row.createCell(colIndex).setCellValue(errorMessage != null ? errorMessage.toString() : "");
	            }

	            // Auto-size columns
	            for (int i = 0; i <= headers.size(); i++) {
	                sheet.autoSizeColumn(i);
	            }

	            // Write to S3
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            workbook.write(outputStream);

	            String fileName = String.format("trainee-profiles/errors_%s.xlsx",
	                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

	            ObjectMetadata metadata = new ObjectMetadata();
	            metadata.setContentLength(outputStream.size());
	            metadata.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName,
	                    new ByteArrayInputStream(outputStream.toByteArray()), metadata));

	            return amazonS3Client.getUrl(bucketName, fileName).toString();
	        }
	    }
	 
	private void checkPermission(TraineeProfile traineeProfile) {
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
				for( TraineeRseti tr : traineeProfile.getTraineeRsetis())
				{
					if (tr.getRseti().getStateId().equals(currentUser.getUserProfile().getState().getExtId())) {
						return;
					} 
				}

                throw new AccessDeniedException("You don't have permission to access Trainee Profile data");
            case RSETI_ADMIN:
            case RSETI_STAFF:
				for( TraineeRseti tr : traineeProfile.getTraineeRsetis())
				{
					if (tr.getRseti().getUuid().equals(currentUser.getUserProfile().getRseti().getUuid())) {
						return;
					} 
				}
				throw new AccessDeniedException("You don't have permission to access Trainee Profile data");                
            default:
                throw new AccessDeniedException("You don't have permission to access RSETI Course data");
        }
    }

	 @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
	    public List<CourseDto> getTraineeCourses(UUID traineeUuid) {
	        TraineeProfile traineeProfile = traineeProfileDAO.findByUuid(traineeUuid);
	        if (traineeProfile == null) {
	            throw new EntityNotFoundException("TraineeProfile not found with uuid: " + traineeUuid);
	        }

	        checkPermission(traineeProfile);

	        List<Course> courses = traineeProfile.getTraineeRsetis().stream()
	            .map(TraineeRseti::getRsetiCourse)
	            .filter(rsetiCourse -> rsetiCourse != null)
	            .map(RsetiCourse::getCourse)
	            .distinct()
	            .collect(Collectors.toList());

	        return courseMapper.toDtoList(courses);
	    }
}
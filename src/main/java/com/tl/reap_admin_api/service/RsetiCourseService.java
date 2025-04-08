package com.tl.reap_admin_api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import com.tl.reap_admin_api.dao.CourseDao;
import com.tl.reap_admin_api.dao.RsetiCourseDao;
import com.tl.reap_admin_api.dao.RsetiDao;
import com.tl.reap_admin_api.dto.RsetiCourseBatchUpdateDto;
import com.tl.reap_admin_api.dto.RsetiCourseDto;
import com.tl.reap_admin_api.exception.CourseNotFoundException;
import com.tl.reap_admin_api.exception.RsetiNotFoundException;
import com.tl.reap_admin_api.mapper.RsetiCourseMapper;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.Role;
import com.tl.reap_admin_api.model.RsetiCourse;
import com.tl.reap_admin_api.model.User;

@Service
public class RsetiCourseService {
	private static final Logger logger = LoggerFactory.getLogger(RsetiCourseService.class);

	
    private AmazonS3 amazonS3Client;

    @Value("${aws.s3.crsimg.bucket-name}")
    private String bucketName;

	private final RsetiDao rsetiDao;
	private final RsetiCourseDao rsetiCourseDao;
	private final RsetiCourseMapper rsetiCourseMapper;
	private final UserService userService; 
	private final CourseDao courseDao;

	@Autowired
	public RsetiCourseService(RsetiDao rsetiDao, RsetiCourseDao rsetiCourseDao, RsetiCourseMapper rsetiCourseMapper,
			UserService userService, CourseDao courseDao, AmazonS3 amazonS3Client) {
		this.rsetiDao = rsetiDao;
		this.rsetiCourseDao = rsetiCourseDao;
		this.rsetiCourseMapper = rsetiCourseMapper;
		this.userService = userService;
		this.courseDao = courseDao;
		this.amazonS3Client = amazonS3Client;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public List<RsetiCourseDto> addCoursesToRseti(UUID rsetiUuid, List<RsetiCourseDto> rsetiCourseDtos) {
		
		List<RsetiCourse> addedCourses = new ArrayList<>();
		RSETI rseti = rsetiDao.findByUuid(rsetiUuid)
			        .orElseThrow(() -> new RsetiNotFoundException("RSETI not found with UUID: " + rsetiUuid));
		rseti.setCreatedAt(ZonedDateTime.now());
		rseti.setUpdatedAt(ZonedDateTime.now());
        User currentUser = userService.getCurrentUser();
        rseti.setCreatedBy(currentUser.getUsername());
        rseti.setUpdatedBy(currentUser.getUsername());

		checkPermission(rseti); 

		for (RsetiCourseDto dto : rsetiCourseDtos) {
			RsetiCourse rsetiCourse = rsetiCourseMapper.toEntity(dto);
			 Course course = courseDao.findByUuid(dto.getCourseUuid())
			            .orElseThrow(() -> new CourseNotFoundException("Course not found with UUID: " + dto.getCourseUuid()));

			rsetiCourse.setUuid(UUID.randomUUID());
			 rsetiCourse.setRseti(rseti);
			 rsetiCourse.setCourse(course);
            rsetiCourse.setCreatedAt(ZonedDateTime.now());
			rsetiCourse.setCreatedBy(userService.getCurrentUser().getUsername());
            rsetiCourse.setUpdatedAt(ZonedDateTime.now());
			rsetiCourse.setCreatedBy(userService.getCurrentUser().getUsername());
			rsetiCourse= rsetiCourseDao.save(rsetiCourse );
			addedCourses.add(rsetiCourse);
			
		}

		return rsetiCourseMapper.toDtoList(addedCourses);
	}

	@Transactional(readOnly = true)
	public List<RsetiCourseDto> getCoursesInRseti(UUID rsetiUuid) {
		RSETI rseti = rsetiDao.findByUuid(rsetiUuid)
				.orElseThrow(() -> new RsetiNotFoundException("RSETI not found with UUID: " + rsetiUuid));
				
		checkPermission(rseti);  // Checks whether the logged in user has read permission to this rseti. Lese throws access denied exceptio
		
		List<RsetiCourse> rsetiCourses = rsetiCourseDao.findByRseti(rseti);
		return rsetiCourseMapper.toDtoList(rsetiCourses);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public List<RsetiCourseDto> updateCoursesInRseti(UUID rsetiUuid, List<RsetiCourseDto> courseDtos) {
		
		List<RsetiCourse> updatedCourses = new ArrayList<>();
		RSETI rseti = rsetiDao.findByUuid(rsetiUuid)
				.orElseThrow(() -> new RsetiNotFoundException("RSETI not found with UUID: " + rsetiUuid));
		 User currentUser = userService.getCurrentUser();
		 rseti.setUpdatedBy(currentUser.getUsername());
		 rseti.setUpdatedAt(ZonedDateTime.now());
				
		checkPermission(rseti);  // Checks whether the logged in user has read permission to this rseti. Lese throws access denied exceptio
		

		for (RsetiCourseDto dto : courseDtos) {
			RsetiCourse rsetiCourse = rsetiCourseDao.findByRsetiUuidAndCourseUuid(rsetiUuid, dto.getCourseUuid()).orElseThrow(
					() -> new CourseNotFoundException("Course not found with UUID: " + dto.getCourseUuid() 
					+ "for rseti with UUID: " + rsetiUuid ));

			if (dto.getStartYear() != 0 && dto.getStartMonth() != 0) {
				rsetiCourse.setStartDate(LocalDate.of(dto.getStartYear(), dto.getStartMonth(), 1));
			}
			if (dto.getEndYear() != 0 && dto.getEndMonth() != 0) {
				rsetiCourse.setEndDate(LocalDate.of(dto.getEndYear(), dto.getEndMonth(), 1));
			}
			updatedCourses.add(rsetiCourseDao.save(rsetiCourse));
		}

		return rsetiCourseMapper.toDtoList(updatedCourses);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public void deleteCourseFromRseti(UUID rsetiUuid, UUID courseUuid) {
		RSETI rseti = rsetiDao.findByUuid(rsetiUuid)
				.orElseThrow(() -> new RsetiNotFoundException("RSETI not found with UUID: " + rsetiUuid));
		 User currentUser = userService.getCurrentUser();
		 rseti.setUpdatedBy(currentUser.getUsername());
		 rseti.setUpdatedAt(ZonedDateTime.now());
				
		checkPermission(rseti);  // Checks whether the logged in user has read permission to this rseti. Lese throws access denied exceptio
		
		RsetiCourse rsetiCourse = rsetiCourseDao.findByRsetiUuidAndCourseUuid(rsetiUuid,courseUuid).orElseThrow(
					() -> new CourseNotFoundException("Course not found with UUID: " +courseUuid 
					+ "for rseti with UUID: " + rsetiUuid ));

		rsetiCourseDao.delete(rsetiCourse);
	}

	@Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public JSONObject  bulkUploadRsetiCourses(MultipartFile file) throws IOException {
		
		JSONObject respObj = new JSONObject();
		List<Map<String, Object>> errorRows = new ArrayList<>();
		List<RsetiCourseBatchUpdateDto> rsetiCourseBatchUpdateDto = new ArrayList<>();
		List<String> headers = new ArrayList<>();
		String errorMsg = "";
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

			// Get header row
			Row headerRow = sheet.getRow(1); // Assuming the header is on the 2nd row (index 1)
			
			for (Cell cell : headerRow) {
				CellType ct = cell.getCellType();
                if (ct != CellType.STRING) {
                    throw new RuntimeException("Wrong File Template. Expecting Headers in row 3");
                }
				headers.add(cell.getStringCellValue());
			}
  
            // Skip header rows
            for (int i = 0; i <= 1; i++) {
                if (rows.hasNext()) rows.next();
            }

			// Pre-fetch all RSETIs and create a map with name as key
			Map<String, Long> rsetiMap = rsetiDao.findAllRsetiIdsByName();

			// Pre-fetch all courses and create a map with code as key
			Map<String, Long> courseMap = courseDao.findAllCourseIdsByCourseCode();

			// Pre-fetch all rseticourses and create a map with code+rsetiname+startmonth+startyear as key
			Map<String, Long> existingRsetiCourseMap = rsetiCourseDao.findAndCreateMapOfAllRsetiCourses();

			Map<String, Row> rsetiCourseMap = new HashMap<String, Row>();			
			
			String currentUser = userService.getCurrentUser().getUsername();
			String rsetiCoureMapKey = "";

            while (rows.hasNext()) {
                Row row = rows.next();
				Map<String, Object> rowData = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    rowData.put(headers.get(i), getStringCellValue(row.getCell(i)));
                }

				
                // Skip if essential data is missing
                String rsetiName = getStringCellValue(row.getCell(3)); // RSETI column
                String courseCode = "NARQ" + getStringCellValue(row.getCell(6)); // Course Code column
                if (isBlank(rsetiName) || isBlank(courseCode)) continue;

                String monthStr = getStringCellValue(row.getCell(4)); // Month column
                String yearStr = getStringCellValue(row.getCell(5)); // Year column
                
                // Parse month and year
                int startMonth = parseMonth(monthStr);
                int startYear = Integer.parseInt(yearStr);
                
				rsetiCoureMapKey = courseCode+rsetiName+monthStr.trim()+yearStr.trim();
				Long rsetiCourseId = existingRsetiCourseMap.get(rsetiCoureMapKey);

				System.out.println("rsetiCoureMapKey -- " + rsetiCoureMapKey);
				if(rsetiCourseId != null) {
					logger.error("RSETI Course already exists {}", rsetiCoureMapKey);
					rowData.put("Error", "RSETI Course already exists");
                    errorRows.add(rowData);
					continue;
				}
                // Get RSETIid from maps
				Long rsetiId = rsetiMap.get(rsetiName);
				if (rsetiId == null) {
					logger.error("RSETI not found for row {}. RSETI: {}, Course: {}", row.getRowNum(), rsetiName);
				 	rowData.put("Error", "RSETI not found");
                    errorRows.add(rowData);
					continue;
				}

				Long courseId = courseMap.get(courseCode);
                
                if (courseId == null) {
                    logger.error("Course not found for row {}.  Course: {}", row.getRowNum(),  courseCode);
					rowData.put("Error", "Course not found");
                    errorRows.add(rowData);
                    continue;
                }

				

                // Check if combination already exists
                LocalDate startDate = LocalDate.of(startYear, startMonth, 1);
				int durationDays = Integer.parseInt(getStringCellValue(row.getCell(11))); // Duration column
                LocalDate endDate = startDate.plusDays(durationDays);

				
				if(rsetiCourseMap.get(rsetiCoureMapKey) != null) {
					logger.error("Duplicate Row");
					rowData.put("Error", "Duplicate Row");
                    errorRows.add(rowData);
					continue;
				}

				rsetiCourseMap.put(rsetiCoureMapKey, row);				

				RsetiCourseBatchUpdateDto dto = new RsetiCourseBatchUpdateDto();
                dto.setRsetiId(rsetiId);
                dto.setCourseId(courseId);
                dto.setStartDate(startDate);
                dto.setEndDate(endDate);
                dto.setCreatedAt(ZonedDateTime.now());
                dto.setUpdatedAt(ZonedDateTime.now());
                dto.setCreatedBy(currentUser);
                dto.setUpdatedBy(currentUser);

                rsetiCourseBatchUpdateDto.add(dto);
            }

			// Batch insert RsetiCourses
            rsetiCourseDao.batchInsert(rsetiCourseBatchUpdateDto);
        } catch (Exception e) {
            logger.error("Error processing bulk upload", e);
			errorMsg +=  e.getMessage();
			respObj.put("errorMsg","Failed - " + errorMsg);
			respObj.put("count", 0);			
			respObj.put("errorFileUrl","" );
			return respObj;
          //  throw new RuntimeException("Error processing bulk upload: " + e.getMessage());
        }

		String responsFileUrl = "";
		if (!errorRows.isEmpty()) {
            responsFileUrl = generateAndUploadErrorReport(errorRows, headers);
        }
		errorMsg = (rsetiCourseBatchUpdateDto.size() == 0) ? "Failed - " + errorMsg:"Success";
		respObj.put("errorMsg",errorMsg);
		respObj.put("count", rsetiCourseBatchUpdateDto.size());			
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
            String fileName = "rseti_courses/rseti_course_errors_" + now.format(formatter) + ".xlsx";

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
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private int parseMonth(String monthStr) {
        return Month.valueOf(monthStr.toUpperCase()).getValue();
    }


	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public void deleteRsetiCourse(UUID rsetiUuid, UUID rsetiCourseUuid) {
		RSETI rseti = rsetiDao.findByUuid(rsetiUuid)
				.orElseThrow(() -> new RsetiNotFoundException("RSETI not found with UUID: " + rsetiUuid));
		rseti.setUpdatedAt(ZonedDateTime.now());
        User currentUser = userService.getCurrentUser();
        rseti.setUpdatedBy(currentUser.getUsername());
				
		checkPermission(rseti);  // Checks whether the logged in user has read permission to this rseti. Lese throws access denied exceptio
		
		RsetiCourse rsetiCourse = rsetiCourseDao.findByRsetiUuidAndUuid(rsetiUuid, rsetiCourseUuid)
				.orElseThrow(() -> new CourseNotFoundException(
						"RSETI Course not found with UUID: " + rsetiCourseUuid + " for RSETI with UUID: " + rsetiUuid));

		rsetiCourseDao.delete(rsetiCourse);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public RsetiCourseDto editRsetiCourse(UUID rsetiUuid, UUID rsetiCourseUuid, RsetiCourseDto rsetiCourseDto) {
		RSETI rseti = rsetiDao.findByUuid(rsetiUuid)
				.orElseThrow(() -> new RsetiNotFoundException("RSETI not found with UUID: " + rsetiUuid));
				
		checkPermission(rseti);  // Checks whether the logged in user has read permission to this rseti. Lese throws access denied exceptio
		
		RsetiCourse rsetiCourse = rsetiCourseDao.findByRsetiUuidAndUuid(rsetiUuid, rsetiCourseUuid)
				.orElseThrow(() -> new CourseNotFoundException(
						"RSETI Course not found with UUID: " + rsetiCourseUuid + " for RSETI with UUID: " + rsetiUuid));

		// Update the fields
		if (rsetiCourseDto.getStartYear() != 0 && rsetiCourseDto.getStartMonth() != 0) {
			rsetiCourse.setStartDate(LocalDate.of(rsetiCourseDto.getStartYear(), rsetiCourseDto.getStartMonth(), 1));
		}
		if (rsetiCourseDto.getEndYear() != 0 && rsetiCourseDto.getEndMonth() != 0) {
			rsetiCourse.setEndDate(LocalDate.of(rsetiCourseDto.getEndYear(), rsetiCourseDto.getEndMonth(), 1));
		}

		rsetiCourse.setUpdatedAt(ZonedDateTime.now());
		rsetiCourse.setUpdatedBy(userService.getCurrentUser().getUsername());

		RsetiCourse updatedRsetiCourse = rsetiCourseDao.save(rsetiCourse);
		return rsetiCourseMapper.toDto(updatedRsetiCourse);
	}

	private void checkPermission(RSETI rseti) {
        User currentUser = userService.getCurrentUser();
        Role userRole = currentUser.getRole();

		System.out.println("User Role - " + userRole.getNumber() + " rseti - " + rseti.getUuid());
        switch (userRole) {
			case PUBLIC:
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
                // These roles have access to all RSETIs
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                if (!rseti.getStateId().equals(currentUser.getUserProfile().getState().getExtId())) {
                    throw new AccessDeniedException("You don't have permission to access this RSETI Course");
                }
                break;
            case RSETI_ADMIN:
            case RSETI_STAFF:
                if (!rseti.getUuid().equals(currentUser.getUserProfile().getRseti().getUuid())) {
                    throw new AccessDeniedException("You don't have permission to access this RSETI Course");
                }
                break;
            default:
                throw new AccessDeniedException("You don't have permission to access RSETI Course data");
        }
    }
}

package com.tl.reap_admin_api.service;


import com.amazonaws.services.s3.AmazonS3;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tl.reap_admin_api.dao.TestimonialDao;
import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.TestimonialDTO;
import com.tl.reap_admin_api.dto.TestimonialTranslationDTO;
import com.tl.reap_admin_api.exception.DuplicatePriorityOrderException;
import com.tl.reap_admin_api.mapper.TestimonialMapper;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.model.Testimonial;
import com.tl.reap_admin_api.model.TestimonialTranslation;
import com.tl.reap_admin_api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TestimonialService {

	private TestimonialDao testimonialDAO;

	private TestimonialMapper mapper;
	
    private UserService userService;

	private final AmazonS3 amazonS3Client;

	
    @Value("${aws.s3.crsimg.testimonial-bucket-name}")
    private String tstmnlImgBucketName;
    
    
    private final KPointService kPointService;
    
    private static final Logger logger = LoggerFactory.getLogger(TestimonialService.class);
  
	@Autowired
    public TestimonialService(TestimonialDao testimonialDAO, TestimonialMapper mapper, UserService userService, AmazonS3 amazonS3Client, KPointService kPointService) {
        this.testimonialDAO = testimonialDAO;
        this.mapper = mapper;
        this.userService = userService;
        this.amazonS3Client = amazonS3Client;
        this.kPointService = kPointService;
    }

	 // Update createTestimonial method to handle video
	@Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public TestimonialDTO createTestimonial(TestimonialDTO dto) throws DuplicatePriorityOrderException {
        if (dto.getOrderNo() != null) {
            Optional<Testimonial> existingTestimonial = testimonialDAO.findByorderNo(dto.getOrderNo());
            if (existingTestimonial.isPresent()) {
                throw new DuplicatePriorityOrderException("A testimonial with priority order " + dto.getOrderNo() + " already exists.");
            }
        }

        Testimonial testimonial = new Testimonial();
        testimonial.setImage(dto.getImage());
        testimonial.setOrderNo(dto.getOrderNo());
        
        // Handle video if provided
        if (dto.getVideoUrl() != null && !dto.getVideoUrl().isEmpty()) {
            JsonNode videoNode = kPointService.checkAndUploadVideo(dto.getVideoUrl(), "Testimonials", "en");
            if (videoNode != null) {
                testimonial.setVideoUrl(dto.getVideoUrl());
                testimonial.setVideoExtId(videoNode.get("id").asText());
            }
        }
        
        User currentUser = userService.getCurrentUser();
        testimonial.setCreatedBy(currentUser.getUsername());
        testimonial.setUpdatedBy(currentUser.getUsername());
        testimonial.setCreatedAt(ZonedDateTime.now());
        testimonial.setUpdatedAt(ZonedDateTime.now());

        updateTranslations(testimonial, dto.getTranslations());
        testimonial = testimonialDAO.save(testimonial);
        return mapper.toDTO(testimonial);
    }

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public TestimonialDTO uploadTestimonialImage(UUID tstmnlUuid, MultipartFile file) throws IOException {
		try {
			Testimonial testimonial = testimonialDAO.findByUuid(tstmnlUuid)
					.orElseThrow(() -> new RuntimeException("Testimonial not found with UUID: " + tstmnlUuid));
			User currentUser = userService.getCurrentUser();
			 testimonial.setUpdatedBy(currentUser.getUsername());
		        testimonial.setUpdatedAt(ZonedDateTime.now());

			String fileExtension = getFileExtension(file.getOriginalFilename());
			String key = "testimonials/" + testimonial.getId() + ".png";

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());

			amazonS3Client.putObject(new PutObjectRequest(tstmnlImgBucketName, key, file.getInputStream(), metadata));

			String imageUrl = amazonS3Client.getUrl(tstmnlImgBucketName, key).toString();
			testimonial.setImage(imageUrl);

			Testimonial updatedTestimonial = testimonialDAO.save(testimonial);
			return mapper.toDTO(updatedTestimonial);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error uploading course image", e);
		}
	}

	private String getFileExtension(String filename) {
		return filename.substring(filename.lastIndexOf(".") + 1);
	}

	@Transactional	
	public TestimonialDTO getTestimonialByUuid(UUID uuid) {
		Testimonial testimonial = testimonialDAO.findByUuid(uuid)
				.orElseThrow(() -> new RuntimeException("Testimonial not found"));
		return mapper.toDTO(testimonial);
	}

	@Transactional
	public List<TestimonialDTO> getAllTestimonials() {
		return testimonialDAO.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
	}

	// Update updateTestimonial method to handle video
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public TestimonialDTO updateTestimonial(UUID uuid, TestimonialDTO dto) {
        Testimonial testimonial = testimonialDAO.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Testimonial not found"));

        testimonial.setImage(dto.getImage());
        
        // Handle video update if provided
        if (dto.getVideoUrl() != null && !dto.getVideoUrl().equals(testimonial.getVideoUrl())) {
            JsonNode videoNode = kPointService.checkAndUploadVideo(dto.getVideoUrl(), "Testimonials", "en");
            if (videoNode != null) {
                testimonial.setVideoUrl(dto.getVideoUrl());
                testimonial.setVideoExtId(videoNode.get("id").asText());
            }
        }
        User currentUser = userService.getCurrentUser();
        testimonial.setUpdatedBy(currentUser.getUsername());
        testimonial.setUpdatedAt(ZonedDateTime.now());
        updateTranslations(testimonial, dto.getTranslations());

        testimonial = testimonialDAO.save(testimonial);
        return mapper.toDTO(testimonial);
    }

	private void updateTranslations(Testimonial testimonial, Set<TestimonialTranslationDTO> translationDTOs) {
		testimonial.clearTranslations();

		if (translationDTOs != null) {
			for (TestimonialTranslationDTO translationDTO : translationDTOs) {
				TestimonialTranslation translation = new TestimonialTranslation();
				translation.setName(translationDTO.getName());
				translation.setDesignation(translationDTO.getDesignation());
				translation.setTestimonialText(translationDTO.getTestimonialText());
				Language language = testimonialDAO.findLanguageByCode(translationDTO.getLanguageCode()).orElseThrow(
						() -> new RuntimeException("Language not found: " + translationDTO.getLanguageCode()));
				translation.setLanguage(language);
				testimonial.addTranslation(translation);
			}
		}
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public void deleteTestimonial(UUID uuid) {
	    Testimonial testimonial = testimonialDAO.findByUuid(uuid)
	        .orElseThrow(() -> new EntityNotFoundException("Testimonial not found for UUID: " + uuid));

	    User currentUser = userService.getCurrentUser();
	    testimonial.setUpdatedBy(currentUser.getUsername());
	    testimonial.setUpdatedAt(ZonedDateTime.now());

	    testimonialDAO.save(testimonial); 
	    testimonialDAO.deleteByUuid(uuid);
	}

	
	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN' )")
	public TestimonialDTO uploadTestimonialVideo(UUID uuid, String videoUrl) {
		try {
			Testimonial testimonial = testimonialDAO.findByUuid(uuid)
					.orElseThrow(() -> new RuntimeException("Testimonial not found with UUID: " + uuid));

			// Upload video to KPoint
			JsonNode videoNode = kPointService.checkAndUploadVideo(videoUrl, "Testimonials", "en");
			if (videoNode == null) {
				throw new RuntimeException("Failed to upload video to KPoint");
			}

			// Update testimonial with video information
			testimonial.setVideoUrl(videoUrl);
			testimonial.setVideoExtId(videoNode.get("id").asText());
			testimonial.setUpdatedAt(ZonedDateTime.now());
			testimonial.setUpdatedBy(userService.getCurrentUser().getUsername());

			Testimonial updatedTestimonial = testimonialDAO.save(testimonial);
			return mapper.toDTO(updatedTestimonial);
		} catch (Exception e) {
			// Log the error
			logger.error("Error uploading testimonial video", e);
			throw new RuntimeException("Error uploading testimonial video", e);
		}
	}
}
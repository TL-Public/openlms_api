package com.tl.reap_admin_api.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.TestimonialDTO;
import com.tl.reap_admin_api.service.TestimonialService;

@RestController
@RequestMapping("/apis/v1/testimonials")
public class TestimonialController {

	@Autowired
	private TestimonialService testimonialService;

	@PostMapping
	public ResponseEntity<TestimonialDTO> createTestimonial(@RequestBody TestimonialDTO testimonialDTO) {
		TestimonialDTO createdTestimonial = testimonialService.createTestimonial(testimonialDTO);
		return new ResponseEntity<>(createdTestimonial, HttpStatus.CREATED);
	}

	@PostMapping("/{uuid}/image")
    public ResponseEntity<TestimonialDTO> uploadTestimonialImage(@PathVariable UUID uuid, @RequestParam("file") MultipartFile file) {
        try {
            TestimonialDTO updatedTestimonial = testimonialService.uploadTestimonialImage(uuid, file);
            return ResponseEntity.ok(updatedTestimonial);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


	@GetMapping("/{uuid}")
	public ResponseEntity<TestimonialDTO> getTestimonial(@PathVariable UUID uuid) {
		TestimonialDTO testimonial = testimonialService.getTestimonialByUuid(uuid);
		return ResponseEntity.ok(testimonial);
	}

	@GetMapping
	public ResponseEntity<List<TestimonialDTO>> getAllTestimonials() {
		List<TestimonialDTO> testimonials = testimonialService.getAllTestimonials();
		return ResponseEntity.ok(testimonials);
	}

	@PutMapping("/{uuid}")
	public ResponseEntity<TestimonialDTO> updateTestimonial(@PathVariable UUID uuid,
			@RequestBody TestimonialDTO testimonialDTO) {
		TestimonialDTO updatedTestimonial = testimonialService.updateTestimonial(uuid, testimonialDTO);
		return ResponseEntity.ok(updatedTestimonial);
	}

	@DeleteMapping("/{uuid}")
	public ResponseEntity<Void> deleteTestimonial(@PathVariable UUID uuid) {
		testimonialService.deleteTestimonial(uuid);
		return ResponseEntity.noContent().build();
	}
	
	// endpoint for video upload
	
	@PostMapping("/{uuid}/video")
    public ResponseEntity<TestimonialDTO> uploadTestimonialVideo(
            @PathVariable UUID uuid,
            @RequestBody Map<String, String> requestBody) {
        try {
            String videoUrl = requestBody.get("videoUrl");
            if (videoUrl == null || videoUrl.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            TestimonialDTO updatedTestimonial = testimonialService.uploadTestimonialVideo(uuid, videoUrl);
            return ResponseEntity.ok(updatedTestimonial);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
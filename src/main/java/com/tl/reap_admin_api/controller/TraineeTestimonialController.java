package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.TraineeTestimonialDto;
import com.tl.reap_admin_api.service.TraineeTestimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/apis/v1/traineetestimonials")
public class TraineeTestimonialController {

	private final TraineeTestimonialService traineeTestimonialService;

	@Autowired
	public TraineeTestimonialController(TraineeTestimonialService traineeTestimonialService) {
		this.traineeTestimonialService = traineeTestimonialService;
	}

	 @GetMapping
	    public ResponseEntity<List<TraineeTestimonialDto>> getTestimonials(@RequestParam(required = false) UUID courseUuid) {
	        List<TraineeTestimonialDto> testimonials = traineeTestimonialService.getTestimonials(courseUuid);
	        return ResponseEntity.ok(testimonials);
	    }

	@GetMapping("/{uuid}")
	public ResponseEntity<TraineeTestimonialDto> getTestimonialByUuid(@PathVariable UUID uuid) {
		TraineeTestimonialDto testimonial = traineeTestimonialService.getTestimonialByUuid(uuid);
		return testimonial != null ? ResponseEntity.ok(testimonial) : ResponseEntity.notFound().build();
	}

	@PostMapping
	public ResponseEntity<TraineeTestimonialDto> createTestimonial(@RequestBody TraineeTestimonialDto testimonialDto) {
		TraineeTestimonialDto createdTestimonial = traineeTestimonialService.createTestimonial(testimonialDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdTestimonial);
	}

	@PutMapping("/{uuid}")
	public ResponseEntity<?> updateTestimonial(@PathVariable UUID uuid,
			@RequestBody TraineeTestimonialDto testimonialDto) {
		try {
			TraineeTestimonialDto updatedTestimonial = traineeTestimonialService.updateTestimonial(uuid,
					testimonialDto);
			return ResponseEntity.ok(updatedTestimonial);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@DeleteMapping("/{uuid}")
	public ResponseEntity<Void> deleteTestimonial(@PathVariable UUID uuid) {
		traineeTestimonialService.deleteTestimonial(uuid);
		return ResponseEntity.noContent().build();
	}
}
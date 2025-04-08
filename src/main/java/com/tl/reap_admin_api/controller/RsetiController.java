package com.tl.reap_admin_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
import org.springframework.web.server.ResponseStatusException;

import com.tl.reap_admin_api.dto.RsetiCourseDto;
import com.tl.reap_admin_api.dto.RsetiDto;
import com.tl.reap_admin_api.dto.RsetiListDto;
import com.tl.reap_admin_api.dto.TraineeRsetiDto;
import com.tl.reap_admin_api.exception.CourseNotFoundException;
import com.tl.reap_admin_api.exception.DuplicateExtIdException;
import com.tl.reap_admin_api.exception.RsetiNotFoundException;
import com.tl.reap_admin_api.exception.TraineeNotFoundException;
import com.tl.reap_admin_api.service.RsetiCourseService;
import com.tl.reap_admin_api.service.RsetiService;
import com.tl.reap_admin_api.service.TraineeRsetiService;

@RestController
@RequestMapping("/apis/v1/rsetis")
public class RsetiController {

	private final RsetiService rsetiService;
	private final RsetiCourseService rsetiCourseService;
	private final TraineeRsetiService traineeRsetiService;

	@Autowired
	public RsetiController(RsetiService rsetiService, RsetiCourseService rsetiCourseService,
			TraineeRsetiService traineeRsetiService) {
		this.rsetiService = rsetiService;
		this.rsetiCourseService = rsetiCourseService;
		this.traineeRsetiService = traineeRsetiService;
	}

	@GetMapping
    public ResponseEntity<List<RsetiListDto>> getAllRsetis() {
        List<RsetiListDto> rsetis = rsetiService.getAllRsetisWithCourseCount();
        return ResponseEntity.ok(rsetis);
    }

	@GetMapping("/{uuid}")
	public ResponseEntity<RsetiDto> getRsetiByUuid(@PathVariable UUID uuid) {
		RsetiDto rseti = rsetiService.getRsetiByUuid(uuid);
		return rseti != null ? ResponseEntity.ok(rseti) : ResponseEntity.notFound().build();
	}

	@PostMapping
    public ResponseEntity<RsetiDto> createRseti(@RequestBody RsetiDto rsetiDto) {
        try {
            RsetiDto createdRseti = rsetiService.createRseti(rsetiDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRseti);
        } catch (DuplicateExtIdException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

	@PutMapping("/{uuid}")
	public ResponseEntity<?> updateRseti(@PathVariable UUID uuid, @RequestBody RsetiDto rsetiDto) {
		try {
			RsetiDto updatedRseti = rsetiService.updateRseti(uuid, rsetiDto);
			return ResponseEntity.ok(updatedRseti);
		} catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

	@DeleteMapping("/{uuid}")
	public ResponseEntity<?> deleteRseti(@PathVariable UUID uuid) {
		try {
			rsetiService.deleteRseti(uuid);
			return ResponseEntity.noContent().build();
		} catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

	@GetMapping("/state/{stateId}")
	public ResponseEntity<List<RsetiDto>> getRsetisByState(@PathVariable UUID stateId) {
		List<RsetiDto> rsetis = rsetiService.getRsetisByStateId(stateId);
		return ResponseEntity.ok(rsetis);
	}

	@PostMapping("/{uuid}/courses")
	public ResponseEntity<List<RsetiCourseDto>> addCoursesToRseti(@PathVariable UUID uuid,
			@RequestBody List<RsetiCourseDto> courseDtos) {
		try {
			List<RsetiCourseDto> addedCourses = rsetiCourseService.addCoursesToRseti(uuid, courseDtos);
			return ResponseEntity.status(HttpStatus.CREATED).body(addedCourses);
		} catch (RsetiNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/{uuid}/courses")
	public ResponseEntity<List<RsetiCourseDto>> getCoursesInRseti(@PathVariable UUID uuid) {
		try {
			List<RsetiCourseDto> courses = rsetiCourseService.getCoursesInRseti(uuid);
			return ResponseEntity.ok(courses);
		} catch (RsetiNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (AccessDeniedException e) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PutMapping("/{uuid}/courses")
	public ResponseEntity<List<RsetiCourseDto>> updateCoursesInRseti(@PathVariable UUID uuid,
			@RequestBody List<RsetiCourseDto> courseDtos) {
		try {
			List<RsetiCourseDto> updatedCourses = rsetiCourseService.updateCoursesInRseti(uuid, courseDtos);
			return ResponseEntity.ok(updatedCourses);
		} catch (RsetiNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{uuid}/courses/{courseUuid}")
	public ResponseEntity<Void> deleteCourseFromRseti(@PathVariable UUID uuid, @PathVariable UUID courseUuid) {
		try {
			rsetiCourseService.deleteCourseFromRseti(uuid, courseUuid);
			return ResponseEntity.noContent().build();
		} catch (RsetiNotFoundException | CourseNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	
	 @GetMapping("/{rsetiUuid}/courses/{courseUuid}/trainees")
	    public ResponseEntity<List<TraineeRsetiDto>> getTraineesByCourse(
	            @PathVariable UUID rsetiUuid,
	            @PathVariable UUID courseUuid) {
	        try {
	            List<TraineeRsetiDto> trainees = traineeRsetiService.getTraineesByCourse(rsetiUuid, courseUuid);
	            return ResponseEntity.ok(trainees);
	        } catch (RsetiNotFoundException | CourseNotFoundException e) {
	            return ResponseEntity.notFound().build();
	        }
	    }

	    @PostMapping("/{rsetiUuid}/courses/{courseUuid}/trainees")
	    public ResponseEntity<TraineeRsetiDto> addTraineeToCourse(
	            @PathVariable UUID rsetiUuid,
	            @PathVariable UUID courseUuid,
	            @RequestBody TraineeRsetiDto traineeRsetiDto) {
	        try {
	            TraineeRsetiDto addedTrainee = traineeRsetiService.addTraineeToCourse(rsetiUuid, courseUuid, traineeRsetiDto);
	            return ResponseEntity.status(HttpStatus.CREATED).body(addedTrainee);
	        } catch (RsetiNotFoundException | CourseNotFoundException | TraineeNotFoundException e) {
	            return ResponseEntity.notFound().build();
	        }
	    }

	    @PutMapping("/{rsetiUuid}/courses/{courseUuid}/trainees/{traineeUuid}")
	    public ResponseEntity<TraineeRsetiDto> updateTraineeInCourse(
	            @PathVariable UUID rsetiUuid,
	            @PathVariable UUID courseUuid,
	            @PathVariable UUID traineeUuid,
	            @RequestBody TraineeRsetiDto traineeRsetiDto) {
	        try {
	            TraineeRsetiDto updatedTrainee = traineeRsetiService.updateTraineeInCourse(rsetiUuid, courseUuid, traineeUuid, traineeRsetiDto);
	            return ResponseEntity.ok(updatedTrainee);
	        } catch (RsetiNotFoundException | CourseNotFoundException | TraineeNotFoundException e) {
	            return ResponseEntity.notFound().build();
	        }
	    }

	    @DeleteMapping("/{rsetiUuid}/courses/{courseUuid}/trainees/{traineeUuid}")
	    public ResponseEntity<Void> deleteTraineeFromCourse(
	            @PathVariable UUID rsetiUuid,
	            @PathVariable UUID courseUuid,
	            @PathVariable UUID traineeUuid) {
	        try {
	            traineeRsetiService.deleteTraineeFromCourse(rsetiUuid, courseUuid, traineeUuid);
	            return ResponseEntity.noContent().build();
	        } catch (RsetiNotFoundException | CourseNotFoundException | TraineeNotFoundException e) {
	            return ResponseEntity.notFound().build();
	        }
	    }
	
		@PostMapping("/batch-update")
		public ResponseEntity<?> bulkUploadRsetis(@RequestParam("file") MultipartFile file) {
			try {
				JSONObject respObj = rsetiService.bulkUploadRsetis(file);
				Map<String, Object> response = new HashMap<>();
				response = respObj.toMap();
				if((int)response.get("count") == 0) {
					return ResponseEntity.badRequest().body(response);
				}
				
				return ResponseEntity.ok(response);
			} catch (Exception e) {
				return ResponseEntity.badRequest().body("Error uploading RSETI data: " + e.getMessage());
			}
		}

		@PostMapping("/courses/batch-update")
		public ResponseEntity<?> bulkUploadRsetiCourses(@RequestParam("file") MultipartFile file) {
			try {
				JSONObject respObj = rsetiCourseService.bulkUploadRsetiCourses(file);
				Map<String, Object> response = new HashMap<>();
				response = respObj.toMap();
				if((int)response.get("count") == 0) {
					return ResponseEntity.badRequest().body(response);
				}
				
				
				return ResponseEntity.ok(response);
			} catch (Exception e) {
				return ResponseEntity.badRequest().body("Error uploading RSETI Courses data: " + e.getMessage());
			}
		}

		
		
		@DeleteMapping("/{uuid}/rseticourses/{rsetiCourseUuid}")
		public ResponseEntity<Void> deleteRsetiCourse(@PathVariable UUID uuid, @PathVariable UUID rsetiCourseUuid) {
			try {
				rsetiCourseService.deleteRsetiCourse(uuid, rsetiCourseUuid);
				return ResponseEntity.noContent().build();
			} catch (RsetiNotFoundException | CourseNotFoundException e) {
				return ResponseEntity.notFound().build();
			}
		}

		@PutMapping("/{rsetiUuid}/rseticourses/{rsetiCourseUuid}")
		public ResponseEntity<RsetiCourseDto> editRsetiCourse(@PathVariable UUID rsetiUuid,
				@PathVariable UUID rsetiCourseUuid, @RequestBody RsetiCourseDto rsetiCourseDto) {
			try {
				RsetiCourseDto updatedCourse = rsetiCourseService.editRsetiCourse(rsetiUuid, rsetiCourseUuid,
						rsetiCourseDto);
				return ResponseEntity.ok(updatedCourse);
			} catch (RsetiNotFoundException | CourseNotFoundException e) {
				return ResponseEntity.notFound().build();
			}
		}
	
}

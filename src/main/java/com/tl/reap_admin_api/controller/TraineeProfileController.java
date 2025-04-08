package com.tl.reap_admin_api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.TraineeProfileDto;
import com.tl.reap_admin_api.exception.UserNotFoundException;
import com.tl.reap_admin_api.service.TraineeProfileService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/apis/v1/trainee-profiles")
public class TraineeProfileController {

    @Autowired
    private TraineeProfileService traineeProfileService;

    @GetMapping
    public ResponseEntity<?> getAllTraineeProfiles() {
        try {
            List<TraineeProfileDto> traineeProfiles = traineeProfileService.getAllTraineeProfiles();
            return ResponseEntity.ok(traineeProfiles);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createTraineeProfile(@RequestBody TraineeProfileDto traineeProfileDto) {
        try {
            TraineeProfileDto createdTraineeProfile = traineeProfileService.createTraineeProfile(traineeProfileDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTraineeProfile);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getTraineeProfileByUuid(@PathVariable UUID uuid) {
        try {
            TraineeProfileDto traineeProfile = traineeProfileService.getTraineeProfileByUuid(uuid);
            if (traineeProfile != null) {
                return ResponseEntity.ok(traineeProfile);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            TraineeProfileDto traineeProfile = traineeProfileService.getCurrentUserProfile();
            if (traineeProfile != null) {
                return ResponseEntity.ok(traineeProfile);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteTraineeProfile(@PathVariable UUID uuid) {
        try {
            boolean isDeleted = traineeProfileService.deleteTraineeProfile(uuid);
            if (isDeleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateTraineeProfile(@PathVariable UUID uuid, @RequestBody TraineeProfileDto traineeProfileDto) {
        try {
            TraineeProfileDto updatedProfile = traineeProfileService.updateTraineeProfile(uuid, traineeProfileDto);
            return ResponseEntity.ok(updatedProfile);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/batch-update")
    public ResponseEntity<?> bulkUploadTraineeProfiles(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> response = traineeProfileService.bulkUploadTraineeProfiles(file);
            if ((int) response.get("count") == 0) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("errorMsg", "Error uploading trainee profile data: " + e.getMessage());
            errorResponse.put("count", 0);
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/{uuid}/courses")
    public ResponseEntity<?> getTraineeCourses(@PathVariable UUID uuid) {
        try {
            List<CourseDto> courses = traineeProfileService.getTraineeCourses(uuid);
            return ResponseEntity.ok(courses);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
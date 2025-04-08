package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.TraineeCredentialDto;
import com.tl.reap_admin_api.dto.ChapterDto;
import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.CourseRsetisDto;
import com.tl.reap_admin_api.dto.TraineeCourseDto;
import com.tl.reap_admin_api.dto.VideoDto;
import com.tl.reap_admin_api.dto.VideoResponse;
import com.tl.reap_admin_api.exception.ChapterNotFoundException;
import com.tl.reap_admin_api.exception.CourseNotFoundException;
import com.tl.reap_admin_api.exception.KPAddPlayListToChannelException;
import com.tl.reap_admin_api.exception.KPChannleNotFoundException;
import com.tl.reap_admin_api.exception.KPPlaylistCreationException;
import com.tl.reap_admin_api.exception.KPVideoUploadException;
import com.tl.reap_admin_api.exception.RsetiCourseNotFoundException;
import com.tl.reap_admin_api.exception.TraineeAlreadyEnrolledException;
import com.tl.reap_admin_api.exception.TraineeNotFoundException;
import com.tl.reap_admin_api.exception.VideoNotFoundException;
import com.tl.reap_admin_api.model.CourseTranslation;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.service.CourseService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.tl.reap_admin_api.service.TraineeCredentialService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/trainees")
public class TraineeController {

    @Autowired
    private TraineeCredentialService traineeService;
    
    private final Map<String, String> exceptionCodeMap;

    public TraineeController() {
        this.exceptionCodeMap = new HashMap<>();
        exceptionCodeMap.put("RsetiCourseNotFoundException", "RSETI_COURSE_NOT_FOUND");
        exceptionCodeMap.put("TraineeNotFoundException", "TRAINEE_NOT_FOUND");
    }

    @PostMapping
    public ResponseEntity<TraineeCredentialDto> createTrainee(@Valid @RequestBody TraineeCredentialDto dto) {
        TraineeCredentialDto traineeCredentialDto = traineeService.createTrainee(dto, "12345678");
        return new ResponseEntity<>(traineeCredentialDto, HttpStatus.CREATED);
    }
    
 //traineecourse...
    
    
    @PostMapping("/{rsetiCourseUuid}")
    public ResponseEntity<?> addTraineeToRsetiCourse(@PathVariable UUID rsetiCourseUuid, @RequestBody TraineeCourseDto traineeCourseDto) {
        try {
            TraineeCourseDto addedTrainee = traineeService.addTraineeToRsetiCourse(rsetiCourseUuid, traineeCourseDto);
            return new ResponseEntity<>(addedTrainee, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (RsetiCourseNotFoundException | TraineeNotFoundException e) {
            String className = e.getClass().getSimpleName();
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (TraineeAlreadyEnrolledException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", "TRAINEE_ALREADY_ENROLLED");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/{rsetiCourseUuid}/{traineeUuid}")
    public ResponseEntity<?> updateTraineeInRsetiCourse(
            @PathVariable UUID rsetiCourseUuid,
            @PathVariable UUID traineeUuid,
            @RequestBody TraineeCourseDto traineeCourseDto) {
        try {
            TraineeCourseDto updatedTrainee = traineeService.updateTraineeInRsetiCourse(rsetiCourseUuid, traineeUuid, traineeCourseDto);
            return new ResponseEntity<>(updatedTrainee, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (RsetiCourseNotFoundException | TraineeNotFoundException e) {
            String className = e.getClass().getSimpleName();
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{rsetiCourseUuid}/{traineeUuid}")
    public ResponseEntity<?> removeTraineeFromRsetiCourse(
            @PathVariable UUID rsetiCourseUuid,
            @PathVariable UUID traineeUuid) {
        try {
            traineeService.removeTraineeFromRsetiCourse(rsetiCourseUuid, traineeUuid);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (RsetiCourseNotFoundException | TraineeNotFoundException e) {
            String className = e.getClass().getSimpleName();
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get(className));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{rsetiCourseUuid}")
    public ResponseEntity<?> getTraineesInRsetiCourse(@PathVariable UUID rsetiCourseUuid) {
        try {
            List<TraineeCourseDto> trainees = traineeService.getTraineesInRsetiCourse(rsetiCourseUuid);
            return new ResponseEntity<>(trainees, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (RsetiCourseNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get("RsetiCourseNotFoundException"));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    //trainee credential
    
    @GetMapping("/usernames/{username}")
    public ResponseEntity<?> getTraineeByUsername(@PathVariable String username) {
        try {
            TraineeCredentialDto trainee = traineeService.getTraineeCredentialByUsername(username);
            return new ResponseEntity<>(trainee, HttpStatus.OK);
        } catch (TraineeNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get("TraineeNotFoundException"));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/emails/{email}")
    public ResponseEntity<?> getTraineeByEmail(@PathVariable String email) {
        try {
            TraineeCredentialDto trainee = traineeService.getTraineeCredentialByEmail(email);
            return new ResponseEntity<>(trainee, HttpStatus.OK);
        } catch (TraineeNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get("TraineeNotFoundException"));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteTrainee(@PathVariable UUID uuid) {
        try {
            traineeService.deleteTrainee(uuid);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (TraineeNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get("TraineeNotFoundException"));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @PutMapping("/{uuid}/reset-password")
    public ResponseEntity<?> resetTraineePassword(@PathVariable UUID uuid, @RequestBody Map<String, String> passwordMap) {
        try {
            String newPassword = passwordMap.get("newPassword");
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body("New password is required");
            }
            traineeService.resetTraineePasswordByProfileUuid(uuid, newPassword);
            return ResponseEntity.ok().body("Password reset successfully");
        } catch (TraineeNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("errorCode", exceptionCodeMap.get("TraineeNotFoundException"));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
    
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody String passwordJson) {
        try {
            JSONObject jsonObject = new JSONObject(passwordJson);
            traineeService.resetPassword(jsonObject);
            return ResponseEntity.ok().body("Password reset successfully");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (TraineeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainee not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

}


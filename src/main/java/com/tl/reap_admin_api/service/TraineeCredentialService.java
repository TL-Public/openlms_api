package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.CourseDao;
import com.tl.reap_admin_api.dao.RsetiCourseDao;
import com.tl.reap_admin_api.dao.TraineeCourseDao;
import com.tl.reap_admin_api.dao.TraineeCredentialDao;
import com.tl.reap_admin_api.dao.TraineeProfileDao;
import com.tl.reap_admin_api.dao.TraineeRsetiDao;
import com.tl.reap_admin_api.dto.TraineeCourseDto;
import com.tl.reap_admin_api.dto.TraineeCredentialDto;
import com.tl.reap_admin_api.dto.TraineeProfileDto;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.Role;
import com.tl.reap_admin_api.model.RsetiCourse;
import com.tl.reap_admin_api.model.TraineeCourse;
import com.tl.reap_admin_api.model.TraineeCredential;
import com.tl.reap_admin_api.model.TraineeProfile;
import com.tl.reap_admin_api.model.TraineeRseti;
import com.tl.reap_admin_api.model.User;
import com.tl.reap_admin_api.security.UserPrincipal;
import com.tl.reap_admin_api.util.SecurityUtils;
import com.tl.reap_admin_api.exception.CourseNotFoundException;
import com.tl.reap_admin_api.exception.RsetiCourseNotFoundException;
import com.tl.reap_admin_api.exception.TraineeAlreadyEnrolledException;
import com.tl.reap_admin_api.exception.TraineeNotFoundException;
import com.tl.reap_admin_api.exception.UserNotFoundException;
import com.tl.reap_admin_api.mapper.TraineeCredentialMapper;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TraineeCredentialService {
	

    private final TraineeCredentialDao traineeCredentialDao;
    private final TraineeProfileDao traineeProfileDao;
    private final TraineeCredentialMapper traineeCredentialMapper;
    private final PasswordEncoder passwordEncoder;
    private final TraineeCourseDao traineeCourseDao;
    private final CourseDao courseDao;
    private final RsetiCourseDao rsetiCourseDao;
    private final TraineeRsetiDao traineeRsetiDao;
    private final UserService userService; 
    private final SecurityUtils securityUtils;
    
    @Autowired
    public TraineeCredentialService(TraineeCredentialDao traineeCredentialDao, TraineeProfileDao traineeProfileDao, TraineeCredentialMapper traineeCredentialMapper, PasswordEncoder passwordEncoder,TraineeCourseDao traineeCourseDao,CourseDao courseDao,RsetiCourseDao rsetiCourseDao,TraineeRsetiDao traineeRsetiDao, UserService userService,SecurityUtils securityUtils) {
        this.traineeCredentialDao = traineeCredentialDao;
        this.traineeProfileDao = traineeProfileDao;
        this.traineeCredentialMapper = traineeCredentialMapper;
        this.passwordEncoder = passwordEncoder;
        this.traineeCourseDao = traineeCourseDao;
        this.courseDao = courseDao;
        this.rsetiCourseDao = rsetiCourseDao;
        this.traineeRsetiDao =traineeRsetiDao;
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF','STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN','RSETI_STAFF', 'TRAINER')")
    public TraineeCredentialDto createTrainee(TraineeCredentialDto traineeCredentialDto, String password) {
        TraineeCredential traineeCredential = traineeCredentialMapper.toEntity(traineeCredentialDto);
        traineeCredential.setUuid(UUID.randomUUID());
        traineeCredential.setPassword(passwordEncoder.encode(password));
        traineeCredential.setCreatedAt(ZonedDateTime.now());
        traineeCredential.setUpdatedAt(ZonedDateTime.now());
     // Get the current user
        User currentUser = userService.getCurrentUser();
        traineeCredential.setUpdatedBy(currentUser.getUsername());
        traineeCredential.setCreatedBy(currentUser.getUsername());
      

        TraineeCredential savedTraineeCredential = traineeCredentialDao.save(traineeCredential);

        if (traineeCredentialDto.getTraineeProfileDto() != null) {
            TraineeProfile traineeProfile = createTraineeProfile(savedTraineeCredential, traineeCredentialDto.getTraineeProfileDto());
            savedTraineeCredential.setTraineeProfile(traineeProfile);
            savedTraineeCredential = traineeCredentialDao.save(savedTraineeCredential);
            TraineeCourseDto tcd = insertToTraineeRseti(traineeProfile);
        }        

        return traineeCredentialMapper.toDto(savedTraineeCredential);
    }

    TraineeCourseDto  insertToTraineeRseti(TraineeProfile traineeProfile)
    {
        if((userService.getCurrentUser().getRole() != Role.RSETI_ADMIN) &&
            (userService.getCurrentUser().getRole() != Role.RSETI_STAFF)) {
            return null;
        }

        TraineeRseti traineeRseti = new TraineeRseti();
        
        traineeRseti.setRsetiCourse(null);
        traineeRseti.setTraineeProfile(traineeProfile);
        traineeRseti.setRseti(userService.getCurrentUser().getUserProfile().getRseti());
        traineeRseti.setStatus(1);
        traineeRseti.setEnrolledOn(traineeProfile.getEnrolledOn());
        
        TraineeRseti savedTraineeRseti = traineeRsetiDao.save(traineeRseti);
        return convertToDto(savedTraineeRseti);
    }

    @Transactional
    private TraineeProfile createTraineeProfile(TraineeCredential savedTraineeCredential, TraineeProfileDto traineeProfileDto) {       
        TraineeProfile traineeProfile = new TraineeProfile();
        traineeProfile.setTrainee(savedTraineeCredential);
        traineeProfile.setUuid(UUID.randomUUID());
        traineeProfile.setCreatedAt(ZonedDateTime.now());
        traineeProfile.setUpdatedAt(ZonedDateTime.now());
     // Get the current user
        User currentUser = userService.getCurrentUser();
        traineeProfile.setUpdatedBy(currentUser.getUsername());
        traineeProfile.setCreatedBy(currentUser.getUsername());
      
        
        // Set all fields from traineeProfileDto
        traineeProfile.setCandidateName(traineeProfileDto.getCandidateName());
        traineeProfile.setFatherNameOrHusbandName(traineeProfileDto.getFatherNameOrHusbandName());
        traineeProfile.setMaritalStatus(traineeProfileDto.getMaritalStatus());
        traineeProfile.setAge(traineeProfileDto.getAge());
        traineeProfile.setReligion(traineeProfileDto.getReligion());
        traineeProfile.setCaste(traineeProfileDto.getCaste());
        traineeProfile.setEducation(traineeProfileDto.getEducation());
        traineeProfile.setPersonWithDisability(traineeProfileDto.getPersonWithDisability());
        traineeProfile.setSex(traineeProfileDto.getSex());
        traineeProfile.setDateOfBirth(traineeProfileDto.getDateOfBirth());
        traineeProfile.setAadharCardNo(traineeProfileDto.getAadharCardNo());
        traineeProfile.setMobileNumber1(traineeProfileDto.getMobileNumber1());
        traineeProfile.setCandidateAddress(traineeProfileDto.getCandidateAddress());
        traineeProfile.setDistrict(traineeProfileDto.getDistrict());
        traineeProfile.setPincode(traineeProfileDto.getPincode());
        traineeProfile.setEmail(traineeProfileDto.getEmail());

        return traineeProfileDao.save(traineeProfile);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN', 'RSETI_ADMIN', 'TRAINER','NAR_STAFF','STATE_STAFF','RSETI_STAFF','TRAINEE')")
    public TraineeCredentialDto getTraineeCredentialByUuid(UUID uuid) {
        TraineeCredential traineeCredential = traineeCredentialDao.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found with uuid: " + uuid));
        return traineeCredentialMapper.toDto(traineeCredential);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN', 'RSETI_ADMIN', 'TRAINER','NAR_STAFF','STATE_STAFF','RSETI_STAFF')")
    public List<TraineeCredentialDto> getAllTrainees() {
        return traineeCredentialDao.findAll().stream()
                .map(traineeCredentialMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN', 'RSETI_ADMIN', 'TRAINER','NAR_STAFF','STATE_STAFF','RSETI_STAFF')")
    public TraineeCredentialDto updateTrainee(UUID uuid, TraineeCredentialDto traineeCredentialDto) {
        TraineeCredential existingTraineeCredential = traineeCredentialDao.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with uuid: " + uuid));

        existingTraineeCredential.setUsername(traineeCredentialDto.getUsername());
        existingTraineeCredential.setEmail(traineeCredentialDto.getEmail());
        existingTraineeCredential.setUpdatedAt(ZonedDateTime.now());
        // Get the current user
        User currentUser = userService.getCurrentUser();
        existingTraineeCredential.setUpdatedBy(currentUser.getUsername());
        TraineeCredential updatedTraineeCredential = traineeCredentialDao.save(existingTraineeCredential);
        return traineeCredentialMapper.toDto(updatedTraineeCredential);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public void deleteTrainee(UUID uuid) {
        TraineeCredential trainee = traineeCredentialDao.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with uuid: " + uuid));
     // Get the current user
        User currentUser = userService.getCurrentUser();
        trainee.setUpdatedBy(currentUser.getUsername());
        trainee.setUpdatedAt(ZonedDateTime.now());
        traineeCredentialDao.delete(trainee);
    }
    
 //traineecourses...
    
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public TraineeCourseDto addTraineeToRsetiCourse(UUID rsetiCourseUuid, TraineeCourseDto traineeCourseDto) {
        RsetiCourse rsetiCourse = rsetiCourseDao.findByUuid(rsetiCourseUuid)
                .orElseThrow(() -> new RsetiCourseNotFoundException("RSETI Course not found with UUID: " + rsetiCourseUuid));
        
        checkPermission(rsetiCourse.getRseti().getUuid(), rsetiCourse.getRseti().getStateId());
        
        TraineeProfile trainee = traineeProfileDao.findByUuid(traineeCourseDto.getTraineeUuid());
        if (trainee == null) {
            throw new TraineeNotFoundException("Trainee not found with UUID: " + traineeCourseDto.getTraineeUuid());
        }

        // Check if the trainee is already enrolled in this course
        //TraineeRseti existingEnrollment = traineeRsetiDao.findByRsetiCourseUuidAndTraineeProfileUuid(rsetiCourseUuid, traineeCourseDto.getTraineeUuid());
        
        // Check if the trainee is already enrolled in this course
        TraineeRseti existingEnrollment = traineeRsetiDao.findByRsetiCourseUuidAndTraineeProfileUuid(rsetiCourseUuid, traineeCourseDto.getTraineeUuid());
        if (existingEnrollment != null) {
            throw new TraineeAlreadyEnrolledException("Trainee is already enrolled in this course");
        }

        //When trainee is added an entry with null rseticourseid will be added to the table
        TraineeRseti traineeRseti = null;
        existingEnrollment = traineeRsetiDao.findByTraineeProfileUuidInRseti(rsetiCourse.getRseti().getUuid(), traineeCourseDto.getTraineeUuid());
               
        if (existingEnrollment == null) {
            traineeRseti = new TraineeRseti();
        } 

        traineeRseti.setRsetiCourse(rsetiCourse);
        traineeRseti.setTraineeProfile(trainee);
        traineeRseti.setEnrolledOn(traineeCourseDto.getEnrollmentDate());
        traineeRseti.setRseti(rsetiCourse.getRseti());
        TraineeRseti savedTraineeRseti = traineeRsetiDao.save(traineeRseti);
        return convertToDto(savedTraineeRseti);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public TraineeCourseDto updateTraineeInRsetiCourse(UUID rsetiCourseUuid, UUID traineeUuid, TraineeCourseDto traineeCourseDto) {
        TraineeRseti traineeRseti = traineeRsetiDao.findByRsetiCourseUuidAndTraineeProfileUuid(rsetiCourseUuid, traineeUuid);
        
        if (traineeRseti == null) {
            throw new TraineeNotFoundException("Trainee not found in the RSETI course");
        }

        checkPermission(traineeRseti.getRseti().getUuid(), traineeRseti.getRseti().getStateId());

        traineeRseti.setEnrolledOn(traineeCourseDto.getEnrollmentDate());
        TraineeRseti updatedTraineeRseti = traineeRsetiDao.save(traineeRseti);
        return convertToDto(updatedTraineeRseti);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public void removeTraineeFromRsetiCourse(UUID rsetiCourseUuid, UUID traineeUuid) {
        TraineeRseti traineeRseti = traineeRsetiDao.findByRsetiCourseUuidAndTraineeProfileUuid(rsetiCourseUuid, traineeUuid);
        if (traineeRseti == null) {
            throw new TraineeNotFoundException("Trainee not found in the RSETI course");
        }
        checkPermission(traineeRseti.getRseti().getUuid(), traineeRseti.getRseti().getStateId());
        traineeRsetiDao.delete(traineeRseti);
    }

    @Transactional(readOnly = true)
    public List<TraineeCourseDto> getTraineesInRsetiCourse(UUID rsetiCourseUuid) {
        RsetiCourse rsetiCourse = rsetiCourseDao.findByUuid(rsetiCourseUuid)
                .orElseThrow(() -> new RsetiCourseNotFoundException("RSETI Course not found with UUID: " + rsetiCourseUuid));

        List<TraineeRseti> traineeRsetis = traineeRsetiDao.findByRsetiCourseUuid(rsetiCourseUuid);
        return traineeRsetis.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TraineeCourseDto convertToDto(TraineeRseti traineeRseti) {
        TraineeCourseDto dto = new TraineeCourseDto();
        dto.setTraineeUuid(traineeRseti.getTraineeProfile().getUuid());
        dto.setEnrollmentDate(traineeRseti.getEnrolledOn());
        return dto;
    }
    
    //traineecredential
    
    
    @Transactional(readOnly = true)
   // @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN', 'RSETI_ADMIN', 'TRAINER', 'NAR_STAFF', 'STATE_STAFF', 'RSETI_STAFF')")
    public TraineeCredentialDto getTraineeCredentialByUsername(String username) {
        TraineeCredential traineeCredential = traineeCredentialDao.findByUsername(username)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found with username: " + username));
        return traineeCredentialMapper.toDto(traineeCredential);
    }

    @Transactional(readOnly = true)
    //@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN', 'RSETI_ADMIN', 'TRAINER', 'NAR_STAFF', 'STATE_STAFF', 'RSETI_STAFF')")
    public TraineeCredentialDto getTraineeCredentialByEmail(String email) {
        TraineeCredential traineeCredential = traineeCredentialDao.findByEmail(email)
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found with email: " + email));
        return traineeCredentialMapper.toDto(traineeCredential);
    }
    
    private void checkPermission(UUID rsetUuid, Integer stateId) {
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
                if (!rsetUuid.equals(currentUser.getUserProfile().getRseti().getUuid())) {
                    throw new AccessDeniedException("You don't have permission to access this RSETI Course");
                }
                break;
            default:
                throw new AccessDeniedException("You don't have permission to access RSETI Course data");
        }
    }

    @Transactional
    public void resetTraineePasswordByProfileUuid(UUID profileUuid, String newPassword) {
        TraineeProfile traineeProfile = traineeProfileDao.findByUuid(profileUuid);
        if (traineeProfile == null) {
            throw new TraineeNotFoundException("Trainee profile not found with uuid: " + profileUuid);
        }

        TraineeCredential trainee = traineeProfile.getTrainee();
        if (trainee == null) {
            throw new TraineeNotFoundException("Trainee credentials not found for profile with uuid: " + profileUuid);
        }

        User currentUser = userService.getCurrentUser();
        
        if (currentUser.getRole() == Role.TRAINEE) {
            if (!currentUser.getUuid().equals(trainee.getUuid())) {
                throw new AccessDeniedException("As a trainee, you can only reset your own password");
            }
        } else if (currentUser.getRole() == Role.RSETI_ADMIN || currentUser.getRole() == Role.RSETI_STAFF) {
            boolean hasAccess = traineeProfile.getTraineeRsetis().stream()
                    .anyMatch(traineeRseti -> traineeRseti.getRseti().getUuid().equals(currentUser.getUserProfile().getRseti().getUuid()));
            if (!hasAccess) {
                throw new AccessDeniedException("You can only reset passwords for trainees in your RSETI");
            }
        } else if (currentUser.getRole() == Role.STATE_ADMIN || currentUser.getRole() == Role.STATE_STAFF) {
            boolean hasAccess = traineeProfile.getTraineeRsetis().stream()
                    .anyMatch(traineeRseti -> traineeRseti.getRseti().getStateId().equals(currentUser.getUserProfile().getState().getExtId()));
            if (!hasAccess) {
                throw new AccessDeniedException("You can only reset passwords for trainees in your state");
            }
        } else if (!isHigherAuthority(currentUser.getRole())) {
            throw new AccessDeniedException("You do not have the required permissions to reset passwords");
        }

        trainee.setPassword(passwordEncoder.encode(newPassword));
        trainee.setUpdatedAt(ZonedDateTime.now());
        trainee.setUpdatedBy(currentUser.getUsername());
        traineeCredentialDao.save(trainee);
    }
    private boolean isHigherAuthority(Role role) {
        return role == Role.SUPER_ADMIN || role == Role.NAR_ADMIN || role == Role.NAR_STAFF
                || role == Role.STATE_ADMIN || role == Role.STATE_STAFF
                || role == Role.RSETI_ADMIN || role == Role.RSETI_STAFF;
    }

    @Transactional
    @PreAuthorize("hasRole('TRAINEE')")
    public void resetPassword(JSONObject jsonObject) {
        if (!jsonObject.has("oldPassword") || !jsonObject.has("newPassword")) {
            throw new IllegalArgumentException("Missing oldPassword or newPassword in the request");
        }

        String oldPassword = jsonObject.getString("oldPassword");
        String newPassword = jsonObject.getString("newPassword");

        TraineeCredential trainee = getCurrentUser();
        if (trainee == null) {
            UserPrincipal userPrincipal = SecurityUtils.getCurrentUser();
            throw new AccessDeniedException("User " + userPrincipal.getUsername() + 
                                          " is not a trainee or doesn't have trainee permissions");
        }

        if (!passwordEncoder.matches(oldPassword, trainee.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from the old password");
        }

        trainee.setPassword(passwordEncoder.encode(newPassword));
        
        // Instead of using userService.getCurrentUser(), use the username from the trainee object
        trainee.setUpdatedAt(ZonedDateTime.now());
        trainee.setUpdatedBy(trainee.getUsername()); // Use the trainee's username instead
        
        traineeCredentialDao.save(trainee);
    }
    
    @Transactional(readOnly = true)
    public TraineeCredential getCurrentUser() {
        UserPrincipal userPrincipal = SecurityUtils.getCurrentUser();
        
        // Check if this UUID exists in the database
        boolean uuidExists = traineeCredentialDao.findByUuid(userPrincipal.getUuid()).isPresent();
        
        // Check if this username exists in the database
        boolean usernameExists = traineeCredentialDao.findByUsername(userPrincipal.getUsername()).isPresent();
        
        if (userPrincipal.getRole() == Role.TRAINEE) {
            Optional<TraineeCredential> optUser = traineeCredentialDao.findByUuid(userPrincipal.getUuid());
            if (optUser.isEmpty()) {
                throw new UserNotFoundException("User not found with UUID: " + userPrincipal.getUuid() + 
                                               ", username: " + userPrincipal.getUsername());
            }
            return optUser.get();
        }

        return null;
    }

}
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
import com.tl.reap_admin_api.model.RsetiCourse;
import com.tl.reap_admin_api.model.TraineeCourse;
import com.tl.reap_admin_api.model.TraineeCredential;
import com.tl.reap_admin_api.model.TraineeProfile;
import com.tl.reap_admin_api.model.TraineeRseti;
import com.tl.reap_admin_api.exception.CourseNotFoundException;
import com.tl.reap_admin_api.exception.RsetiCourseNotFoundException;
import com.tl.reap_admin_api.exception.TraineeAlreadyEnrolledException;
import com.tl.reap_admin_api.exception.TraineeNotFoundException;
import com.tl.reap_admin_api.exception.UserNotFoundException;
import com.tl.reap_admin_api.mapper.TraineeCredentialMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
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
    @Autowired
    public TraineeCredentialService(TraineeCredentialDao traineeCredentialDao, TraineeProfileDao traineeProfileDao, TraineeCredentialMapper traineeCredentialMapper, PasswordEncoder passwordEncoder,TraineeCourseDao traineeCourseDao,CourseDao courseDao,RsetiCourseDao rsetiCourseDao,TraineeRsetiDao traineeRsetiDao) {
        this.traineeCredentialDao = traineeCredentialDao;
        this.traineeProfileDao = traineeProfileDao;
        this.traineeCredentialMapper = traineeCredentialMapper;
        this.passwordEncoder = passwordEncoder;
        this.traineeCourseDao = traineeCourseDao;
        this.courseDao = courseDao;
        this.rsetiCourseDao = rsetiCourseDao;
        this.traineeRsetiDao =traineeRsetiDao;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN', 'RSETI_ADMIN', 'TRAINER','NAR_STAFF','STATE_STAFF','RSETI_STAFF')")
    public TraineeCredentialDto createTrainee(TraineeCredentialDto traineeCredentialDto, String password) {
        TraineeCredential traineeCredential = traineeCredentialMapper.toEntity(traineeCredentialDto);
        traineeCredential.setUuid(UUID.randomUUID());
        traineeCredential.setPassword(passwordEncoder.encode(password));
        traineeCredential.setCreatedAt(ZonedDateTime.now());
        traineeCredential.setUpdatedAt(ZonedDateTime.now());

        TraineeCredential savedTraineeCredential = traineeCredentialDao.save(traineeCredential);

        if (traineeCredentialDto.getTraineeProfileDto() != null) {
            TraineeProfile traineeProfile = createTraineeProfile(savedTraineeCredential, traineeCredentialDto.getTraineeProfileDto());
            savedTraineeCredential.setTraineeProfile(traineeProfile);
            savedTraineeCredential = traineeCredentialDao.save(savedTraineeCredential);
        }

        return traineeCredentialMapper.toDto(savedTraineeCredential);
    }

    @Transactional
    private TraineeProfile createTraineeProfile(TraineeCredential savedTraineeCredential, TraineeProfileDto traineeProfileDto) {       
        TraineeProfile traineeProfile = new TraineeProfile();
        traineeProfile.setTrainee(savedTraineeCredential);
        traineeProfile.setUuid(UUID.randomUUID());
        
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

        TraineeCredential updatedTraineeCredential = traineeCredentialDao.save(existingTraineeCredential);
        return traineeCredentialMapper.toDto(updatedTraineeCredential);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN', 'RSETI_ADMIN')")
    public void deleteTrainee(UUID uuid) {
        TraineeCredential trainee = traineeCredentialDao.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with uuid: " + uuid));
        traineeCredentialDao.delete(trainee);
    }
    
 //traineecourses...
    
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public TraineeCourseDto addTraineeToRsetiCourse(UUID rsetiCourseUuid, TraineeCourseDto traineeCourseDto) {
        RsetiCourse rsetiCourse = rsetiCourseDao.findByUuid(rsetiCourseUuid)
                .orElseThrow(() -> new RsetiCourseNotFoundException("RSETI Course not found with UUID: " + rsetiCourseUuid));
        
        TraineeProfile trainee = traineeProfileDao.findByUuid(traineeCourseDto.getTraineeUuid());
        if (trainee == null) {
            throw new TraineeNotFoundException("Trainee not found with UUID: " + traineeCourseDto.getTraineeUuid());
        }

        // Check if the trainee is already enrolled in this course
        TraineeRseti existingEnrollment = traineeRsetiDao.findByRsetiCourseUuidAndTraineeProfileUuid(rsetiCourseUuid, traineeCourseDto.getTraineeUuid());
        if (existingEnrollment != null) {
            throw new TraineeAlreadyEnrolledException("Trainee is already enrolled in this course");
        }

        TraineeRseti traineeRseti = new TraineeRseti();
        traineeRseti.setRsetiCourse(rsetiCourse);
        traineeRseti.setTraineeProfile(trainee);
        traineeRseti.setEnrolledOn(traineeCourseDto.getEnrollmentDate());
        traineeRseti.setRseti(rsetiCourse.getRseti());
        TraineeRseti savedTraineeRseti = traineeRsetiDao.save(traineeRseti);
        return convertToDto(savedTraineeRseti);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public TraineeCourseDto updateTraineeInRsetiCourse(UUID rsetiCourseUuid, UUID traineeUuid, TraineeCourseDto traineeCourseDto) {
        TraineeRseti traineeRseti = traineeRsetiDao.findByRsetiCourseUuidAndTraineeProfileUuid(rsetiCourseUuid, traineeUuid);
        if (traineeRseti == null) {
            throw new TraineeNotFoundException("Trainee not found in the RSETI course");
        }

        traineeRseti.setEnrolledOn(traineeCourseDto.getEnrollmentDate());
        TraineeRseti updatedTraineeRseti = traineeRsetiDao.save(traineeRseti);
        return convertToDto(updatedTraineeRseti);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void removeTraineeFromRsetiCourse(UUID rsetiCourseUuid, UUID traineeUuid) {
        TraineeRseti traineeRseti = traineeRsetiDao.findByRsetiCourseUuidAndTraineeProfileUuid(rsetiCourseUuid, traineeUuid);
        if (traineeRseti == null) {
            throw new TraineeNotFoundException("Trainee not found in the RSETI course");
        }

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
    
    
    
    

}
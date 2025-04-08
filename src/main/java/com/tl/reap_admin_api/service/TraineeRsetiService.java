package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.TraineeRsetiDao;
import com.tl.reap_admin_api.dto.TraineeRsetiDto;
import com.tl.reap_admin_api.exception.CourseNotFoundException;
import com.tl.reap_admin_api.exception.RsetiNotFoundException;
import com.tl.reap_admin_api.exception.TraineeNotFoundException;
import com.tl.reap_admin_api.mapper.TraineeRsetiMapper;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.TraineeProfile;
import com.tl.reap_admin_api.model.TraineeRseti;


import com.tl.reap_admin_api.model.User;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TraineeRsetiService {

    private final TraineeRsetiDao traineeRsetiDao;
    private final TraineeRsetiMapper traineeRsetiMapper;
    private final UserService userService;

    @Autowired
    public TraineeRsetiService(TraineeRsetiDao traineeRsetiDao, TraineeRsetiMapper traineeRsetiMapper,UserService userService) {
        this.traineeRsetiDao = traineeRsetiDao;
        this.traineeRsetiMapper = traineeRsetiMapper;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public List<TraineeRsetiDto> getTraineesByCourse(UUID rsetiUuid, UUID courseUuid) throws RsetiNotFoundException, CourseNotFoundException {
        List<TraineeRseti> traineeRsetis = traineeRsetiDao.findByRsetiUuidAndCourseUuid(rsetiUuid, courseUuid);
        return traineeRsetis.stream()
                .map(traineeRsetiMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public TraineeRsetiDto addTraineeToCourse(UUID rsetiUuid, UUID courseUuid, TraineeRsetiDto traineeRsetiDto) throws RsetiNotFoundException, CourseNotFoundException, TraineeNotFoundException {
        RSETI rseti = traineeRsetiDao.findRsetiByUuid(rsetiUuid)
                .orElseThrow(() -> new RsetiNotFoundException("RSETI not found with UUID: " + rsetiUuid));

        Course course = traineeRsetiDao.findCourseByUuid(courseUuid)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with UUID: " + courseUuid));

        TraineeProfile traineeProfile = traineeRsetiDao.findTraineeProfileByUuid(traineeRsetiDto.getTraineeProfileUuid())
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found with UUID: " + traineeRsetiDto.getTraineeProfileUuid()));

        TraineeRseti traineeRseti = traineeRsetiMapper.toEntity(traineeRsetiDto);
        traineeRseti.setUuid(UUID.randomUUID());
        traineeRseti.setCreatedAt(ZonedDateTime.now());
        traineeRseti.setUpdatedAt(ZonedDateTime.now());
	        User currentUser = userService.getCurrentUser();
	        traineeRseti.setCreatedBy(currentUser.getUsername());
	        traineeRseti.setUpdatedBy(currentUser.getUsername());
        traineeRseti.setRseti(rseti);
        traineeRseti.setTraineeProfile(traineeProfile);

        TraineeRseti savedTraineeRseti = traineeRsetiDao.save(traineeRseti);
        return traineeRsetiMapper.toDto(savedTraineeRseti);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public TraineeRsetiDto updateTraineeInCourse(UUID rsetiUuid, UUID courseUuid, UUID traineeUuid, TraineeRsetiDto traineeRsetiDto) throws RsetiNotFoundException, CourseNotFoundException, TraineeNotFoundException {
        TraineeRseti existingTraineeRseti = traineeRsetiDao.findByRsetiUuidAndCourseUuidAndTraineeUuid(rsetiUuid, courseUuid, traineeUuid)
                .orElseThrow(() -> new TraineeNotFoundException("TraineeRseti not found for RSETI UUID: " + rsetiUuid + ", Course UUID: " + courseUuid + " and Trainee UUID: " + traineeUuid));
        // Get the current user
        User currentUser = userService.getCurrentUser();
        existingTraineeRseti.setUpdatedBy(currentUser.getUsername());
        existingTraineeRseti.setUpdatedAt(ZonedDateTime.now());
        traineeRsetiMapper.updateEntityFromDto(traineeRsetiDto, existingTraineeRseti);

        TraineeRseti updatedTraineeRseti = traineeRsetiDao.save(existingTraineeRseti);
        return traineeRsetiMapper.toDto(updatedTraineeRseti);
    }

    

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public void deleteTraineeFromCourse(UUID rsetiUuid, UUID courseUuid, UUID traineeUuid) throws RsetiNotFoundException, CourseNotFoundException, TraineeNotFoundException {
        TraineeRseti traineeRseti = traineeRsetiDao.findByRsetiUuidAndCourseUuidAndTraineeUuid(rsetiUuid, courseUuid, traineeUuid)
                .orElseThrow(() -> new TraineeNotFoundException("TraineeRseti not found for RSETI UUID: " + rsetiUuid + ", Course UUID: " + courseUuid + " and Trainee UUID: " + traineeUuid));
     // Get the current user
        User currentUser = userService.getCurrentUser();
        traineeRseti.setUpdatedBy(currentUser.getUsername());
        traineeRseti.setUpdatedAt(ZonedDateTime.now());


        traineeRsetiDao.delete(traineeRseti);
    }
}
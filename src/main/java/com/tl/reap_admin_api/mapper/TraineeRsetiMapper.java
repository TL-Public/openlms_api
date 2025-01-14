package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.TraineeRsetiDto;
import com.tl.reap_admin_api.model.TraineeRseti;
import org.springframework.stereotype.Component;

@Component
public class TraineeRsetiMapper {

    public TraineeRsetiDto toDto(TraineeRseti traineeRseti) {
        TraineeRsetiDto dto = new TraineeRsetiDto();
        dto.setUuid(traineeRseti.getUuid());
        dto.setEnrollId(traineeRseti.getEnrollId());
        dto.setEnrolledOn(traineeRseti.getEnrolledOn());
        dto.setRsetiUuid(traineeRseti.getRseti().getUuid());
        dto.setTraineeProfileUuid(traineeRseti.getTraineeProfile().getUuid());
        dto.setCourseUuid(traineeRseti.getRsetiCourse().getCourse().getUuid());  // Set courseUuid
        dto.setRsetiCourseUuid(traineeRseti.getRsetiCourse().getUuid());  // Set rsetiCourseUuid
     
        dto.setStatus(traineeRseti.getStatus());
        return dto;
    }



    public TraineeRseti toEntity(TraineeRsetiDto dto) {
        TraineeRseti traineeRseti = new TraineeRseti();
        traineeRseti.setUuid(dto.getUuid());
        traineeRseti.setEnrollId(dto.getEnrollId());
        traineeRseti.setEnrolledOn(dto.getEnrolledOn());
        
        traineeRseti.setStatus(dto.getStatus());
        return traineeRseti;
    }

    public void updateEntityFromDto(TraineeRsetiDto dto, TraineeRseti traineeRseti) {
        traineeRseti.setEnrollId(dto.getEnrollId());
        traineeRseti.setEnrolledOn(dto.getEnrolledOn());
        traineeRseti.setStatus(dto.getStatus());
    }
}
package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.TraineeCredentialDto;
import com.tl.reap_admin_api.model.TraineeCredential;
import com.tl.reap_admin_api.mapper.TraineeProfileMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TraineeCredentialMapper {
    @Autowired
    TraineeProfileMapper traineeProfileMapper;

    public TraineeCredentialDto toDto(TraineeCredential trainee) {
        TraineeCredentialDto dto = new TraineeCredentialDto();
        dto.setUuid(trainee.getUuid());
        dto.setUsername(trainee.getUsername());
        dto.setEmail(trainee.getEmail());
        dto.setTraineeProfileDto(traineeProfileMapper.toDTO(trainee.getTraineeProfile()));
        return dto;
    }

    public TraineeCredential toEntity(TraineeCredentialDto dto) {
        TraineeCredential trainee = new TraineeCredential();
        trainee.setUuid(dto.getUuid());
        trainee.setUsername(dto.getUsername());
        trainee.setEmail(dto.getEmail());
        return trainee;
    }
}
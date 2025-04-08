package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dao.TraineeCredentialDao;
import com.tl.reap_admin_api.dto.TraineeProfileDto;
import com.tl.reap_admin_api.dto.TraineeRsetiDto;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.RsetiCourse;
import com.tl.reap_admin_api.model.TraineeCredential;
import com.tl.reap_admin_api.model.TraineeProfile;
import com.tl.reap_admin_api.model.TraineeRseti;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TraineeProfileMapper {
    @Autowired
    TraineeCredentialDao traineeCredentialDao;

    public TraineeProfileDto toDTO(TraineeProfile traineeProfile) {
        if (traineeProfile == null) {
            return null;
        }

         List<TraineeRsetiDto> traineeRsetiDtos = traineeProfile.getTraineeRsetis() != null ? traineeProfile.getTraineeRsetis().stream()
                .map(this::mapTraineeRsetiToDto)
                .collect(Collectors.toList()) : List.of();


        TraineeProfileDto dto = new TraineeProfileDto(
            traineeProfile.getUuid(),
            traineeProfile.getEnrollId(),
            traineeProfile.getEnrolledOn(),
            traineeProfile.getStatus(),
            traineeProfile.getBatchNo(),
            traineeProfile.getCandidateName(),
            traineeProfile.getFatherNameOrHusbandName(),
            traineeProfile.getMaritalStatus(),
            traineeProfile.getAge(),
            traineeProfile.getReligion(),
            traineeProfile.getCaste(),
            traineeProfile.getEducation(),
            traineeProfile.getPersonWithDisability(),
            traineeProfile.getSex(),
            traineeProfile.getPovertyLine(),
            traineeProfile.getPovertyLineNumberOrRationCardNumber(),
            traineeProfile.getSecc(),
            traineeProfile.getSeccNo(),
            traineeProfile.getPanNumber(),
            traineeProfile.getResidential(),
            traineeProfile.getDateOfBirth(),
            traineeProfile.getAadharCardNo(),
            traineeProfile.getLandlineStd(),
            traineeProfile.getLandlineNumber(),
            traineeProfile.getMobileNumber1(),
            traineeProfile.getMobileNumber2(),
            traineeProfile.getSgsyCandidate(),
            traineeProfile.getFamilyOccupation(),
            traineeProfile.getCandidatePresentOccupation(),
            traineeProfile.getNativityArea(),
            traineeProfile.getCandidateAddress(),
            traineeProfile.getVillage(),
            traineeProfile.getHobli(),
            traineeProfile.getDistrict(),
            traineeProfile.getTaluk(),
            traineeProfile.getPincode(),
            traineeProfile.getCandidateSponsoredByBank(),
            traineeProfile.getSponsoredBankName(),
            traineeProfile.getSponsoredBankBranch(),
            traineeProfile.getSponsoredBankCity(),
            traineeProfile.getSponsorName(),
            traineeProfile.getRelevantExperience(),
            traineeProfile.getNameOfShg(),
            traineeProfile.getFamilyMember(),
            traineeProfile.getEmail(),
            traineeProfile.getMnergaCardNo(),
            traineeProfile.getTrainee().getUsername() ,
            traineeRsetiDtos
        );
        return dto;
    }

    private TraineeRsetiDto mapTraineeRsetiToDto(TraineeRseti traineeRseti) {
        UUID rsetiCourseUUID = null;
        UUID courseUUID = null;
        if(traineeRseti.getRsetiCourse() != null) {
            rsetiCourseUUID = traineeRseti.getRsetiCourse().getUuid();
            courseUUID = traineeRseti.getRsetiCourse().getCourse().getUuid();
        }
        return new TraineeRsetiDto(
            traineeRseti.getUuid(),
            traineeRseti.getEnrollId(),
            traineeRseti.getEnrolledOn(),
            traineeRseti.getRseti().getUuid(),
            traineeRseti.getTraineeProfile().getUuid(),
            rsetiCourseUUID,
            courseUUID,    
            traineeRseti.getStatus()
        );
    }


    public TraineeProfile toEntity(TraineeProfileDto dto) {
        if (dto == null) {
            return null;
        }
        TraineeProfile traineeProfile = new TraineeProfile();
        int ret = updateEntityFromDTO(dto, traineeProfile);
        
        return (ret == 1) ? traineeProfile : null;
    }

    public int updateEntityFromDTO(TraineeProfileDto dto, TraineeProfile traineeProfile) {
        if (dto == null || traineeProfile == null) {
            return 0;
        }

        Optional<TraineeCredential> tcOpt = traineeCredentialDao.findByUsername(dto.getUsername());
        if(tcOpt.isPresent()) {
            traineeProfile.setTrainee(tcOpt.get());
        } else {
            return 0;
        }
        

        traineeProfile.setUuid(dto.getUuid());
        traineeProfile.setEnrollId(dto.getEnrollId());
        traineeProfile.setEnrolledOn(dto.getEnrolledOn());
        traineeProfile.setStatus(dto.getStatus());
        traineeProfile.setBatchNo(dto.getBatchNo());
        traineeProfile.setCandidateName(dto.getCandidateName());
        traineeProfile.setFatherNameOrHusbandName(dto.getFatherNameOrHusbandName());
        traineeProfile.setMaritalStatus(dto.getMaritalStatus());
        traineeProfile.setAge(dto.getAge());
        traineeProfile.setReligion(dto.getReligion());
        traineeProfile.setCaste(dto.getCaste());
        traineeProfile.setEducation(dto.getEducation());
        traineeProfile.setPersonWithDisability(dto.getPersonWithDisability());
        traineeProfile.setSex(dto.getSex());
        traineeProfile.setPovertyLine(dto.getPovertyLine());
        traineeProfile.setPovertyLineNumberOrRationCardNumber(dto.getPovertyLineNumberOrRationCardNumber());
        traineeProfile.setSecc(dto.getSecc());
        traineeProfile.setSeccNo(dto.getSeccNo());
        traineeProfile.setPanNumber(dto.getPanNumber());
        traineeProfile.setResidential(dto.getResidential());
        traineeProfile.setDateOfBirth(dto.getDateOfBirth());
        traineeProfile.setAadharCardNo(dto.getAadharCardNo());
        traineeProfile.setLandlineStd(dto.getLandlineStd());
        traineeProfile.setLandlineNumber(dto.getLandlineNumber());
        traineeProfile.setMobileNumber1(dto.getMobileNumber1());
        traineeProfile.setMobileNumber2(dto.getMobileNumber2());
        traineeProfile.setSgsyCandidate(dto.getSgsyCandidate());
        traineeProfile.setFamilyOccupation(dto.getFamilyOccupation());
        traineeProfile.setCandidatePresentOccupation(dto.getCandidatePresentOccupation());
        traineeProfile.setNativityArea(dto.getNativityArea());
        traineeProfile.setCandidateAddress(dto.getCandidateAddress());
        traineeProfile.setVillage(dto.getVillage());
        traineeProfile.setHobli(dto.getHobli());
        traineeProfile.setDistrict(dto.getDistrict());
        traineeProfile.setTaluk(dto.getTaluk());
        traineeProfile.setPincode(dto.getPincode());
        traineeProfile.setCandidateSponsoredByBank(dto.getCandidateSponsoredByBank());
        traineeProfile.setSponsoredBankName(dto.getSponsoredBankName());
        traineeProfile.setSponsoredBankBranch(dto.getSponsoredBankBranch());
        traineeProfile.setSponsoredBankCity(dto.getSponsoredBankCity());
        traineeProfile.setSponsorName(dto.getSponsorName());
        traineeProfile.setRelevantExperience(dto.getRelevantExperience());
        traineeProfile.setNameOfShg(dto.getNameOfShg());
        traineeProfile.setFamilyMember(dto.getFamilyMember());
        traineeProfile.setEmail(dto.getEmail());
        traineeProfile.setMnergaCardNo(dto.getMnergaCardNo());

       

        if (dto.getTraineeRsetis() != null) {
            traineeProfile.setTraineeRsetis(dto.getTraineeRsetis().stream()
                .map(rsetiDto -> mapDtoToTraineeRseti(rsetiDto, traineeProfile))
                .collect(Collectors.toList()));
        }

        return 1;
    }

    private TraineeRseti mapDtoToTraineeRseti(TraineeRsetiDto dto, TraineeProfile traineeProfile) {
        TraineeRseti traineeRseti = new TraineeRseti();
        traineeRseti.setUuid(dto.getUuid());
        traineeRseti.setEnrollId(dto.getEnrollId());
        traineeRseti.setEnrolledOn(dto.getEnrolledOn());
        traineeRseti.setStatus(dto.getStatus());
        traineeRseti.setTraineeProfile(traineeProfile);

        RSETI rseti = new RSETI();
        rseti.setUuid(dto.getRsetiUuid());
        traineeRseti.setRseti(rseti);

        RsetiCourse rsetiCourse = new RsetiCourse();
        rsetiCourse.setUuid(dto.getRsetiUuid());
        traineeRseti.setRsetiCourse(rsetiCourse);

        return traineeRseti;
    }
}
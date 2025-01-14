package com.tl.reap_admin_api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.tl.reap_admin_api.dao.TraineeProfileDao;
import com.tl.reap_admin_api.dao.TraineeCredentialDao;
import com.tl.reap_admin_api.dto.TraineeProfileDto;
import com.tl.reap_admin_api.exception.UserNotFoundException;
import com.tl.reap_admin_api.mapper.TraineeProfileMapper;
import com.tl.reap_admin_api.model.TraineeProfile;
import com.tl.reap_admin_api.model.TraineeCredential;
import com.tl.reap_admin_api.security.UserPrincipal;
import com.tl.reap_admin_api.util.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TraineeProfileService {

	@Autowired
	private TraineeProfileDao traineeProfileDAO;

	@Autowired
	private TraineeCredentialDao traineeCredentialDao;

	@Autowired
	private TraineeProfileMapper traineeProfileMapper;


	
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public List<TraineeProfileDto> getAllTraineeProfiles() {
		List<TraineeProfile> traineeProfiles = traineeProfileDAO.findAll();
		return traineeProfiles.stream().map(traineeProfileMapper::toDTO).collect(Collectors.toList());
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public TraineeProfileDto getTraineeProfileByUuid(UUID uuid) {
		TraineeProfile traineeProfile = traineeProfileDAO.findByUuid(uuid);
		if (traineeProfile == null) {
			throw new EntityNotFoundException("TraineeProfile not found with uuid: " + uuid);
		}
		return traineeProfileMapper.toDTO(traineeProfile);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF','TRAINEE')")
	public TraineeProfileDto getCurrentUserProfile() {
		TraineeCredential  traineeCredential = null;
		UserPrincipal currentUser = getCurrentUser();

		Optional<TraineeCredential> traineeCredentialOpt = traineeCredentialDao.findByUuid(currentUser.getUuid());
		if(traineeCredentialOpt.isEmpty())
		{
			throw new UserNotFoundException("Trainee not found for trainee with UUID: " + currentUser.getUuid());
		}

		traineeCredential = traineeCredentialOpt.get();
		TraineeProfile traineeProfile = traineeProfileDAO.findByTraineeCredential(traineeCredential.getId());
		return traineeProfileMapper.toDTO(traineeProfile);
	}
	
	
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public TraineeProfileDto createTraineeProfile(TraineeProfileDto traineeProfileDto) {
		TraineeProfile traineeProfile = traineeProfileMapper.toEntity(traineeProfileDto);
		traineeProfile.setUuid(UUID.randomUUID());
		TraineeProfile savedTraineeProfile = traineeProfileDAO.save(traineeProfile);
		return traineeProfileMapper.toDTO(savedTraineeProfile);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public boolean deleteTraineeProfile(UUID uuid) {
		TraineeProfile traineeProfile = traineeProfileDAO.findByUuid(uuid);
		if (traineeProfile != null) {
			traineeProfileDAO.delete(traineeProfile);
			return true;
		}
		return false;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
	public TraineeProfileDto updateTraineeProfile(UUID uuid, TraineeProfileDto traineeProfileDto) {
		// Retrieve the existing trainee profile by UUID
		TraineeProfile existingProfile = traineeProfileDAO.findByUuid(uuid);
		if (existingProfile == null) {
			throw new EntityNotFoundException("TraineeProfile not found with uuid: " + uuid);
		}

		// Update only the fields that are not null in the DTO
		if (traineeProfileDto.getEnrollId() != null) {
			existingProfile.setEnrollId(traineeProfileDto.getEnrollId());
		}
		if (traineeProfileDto.getStatus() != null) {
			existingProfile.setStatus(traineeProfileDto.getStatus());
		}
		if (traineeProfileDto.getCandidateName() != null) {
			existingProfile.setCandidateName(traineeProfileDto.getCandidateName());
		}
		if (traineeProfileDto.getFatherNameOrHusbandName() != null) {
			existingProfile.setFatherNameOrHusbandName(traineeProfileDto.getFatherNameOrHusbandName());
		}
		if (traineeProfileDto.getMaritalStatus() != null) {
			existingProfile.setMaritalStatus(traineeProfileDto.getMaritalStatus());
		}
		if (traineeProfileDto.getAge() != null) {
			existingProfile.setAge(traineeProfileDto.getAge());
		}
		if (traineeProfileDto.getReligion() != null) {
			existingProfile.setReligion(traineeProfileDto.getReligion());
		}
		if (traineeProfileDto.getCaste() != null) {
			existingProfile.setCaste(traineeProfileDto.getCaste());
		}
		if (traineeProfileDto.getEducation() != null) {
			existingProfile.setEducation(traineeProfileDto.getEducation());
		}
		if (traineeProfileDto.getPersonWithDisability() != null) {
			existingProfile.setPersonWithDisability(traineeProfileDto.getPersonWithDisability());
		}
		if (traineeProfileDto.getSex() != null) {
			existingProfile.setSex(traineeProfileDto.getSex());
		}
		if (traineeProfileDto.getPovertyLine() != null) {
			existingProfile.setPovertyLine(traineeProfileDto.getPovertyLine());
		}
		if (traineeProfileDto.getPovertyLineNumberOrRationCardNumber() != null) {
			existingProfile
					.setPovertyLineNumberOrRationCardNumber(traineeProfileDto.getPovertyLineNumberOrRationCardNumber());
		}
		if (traineeProfileDto.getSecc() != null) {
			existingProfile.setSecc(traineeProfileDto.getSecc());
		}
		if (traineeProfileDto.getSeccNo() != null) {
			existingProfile.setSeccNo(traineeProfileDto.getSeccNo());
		}
		if (traineeProfileDto.getPanNumber() != null) {
			existingProfile.setPanNumber(traineeProfileDto.getPanNumber());
		}
		if (traineeProfileDto.getResidential() != null) {
			existingProfile.setResidential(traineeProfileDto.getResidential());
		}
		if (traineeProfileDto.getDateOfBirth() != null) {
			existingProfile.setDateOfBirth(traineeProfileDto.getDateOfBirth());
		}
		if (traineeProfileDto.getAadharCardNo() != null) {
			existingProfile.setAadharCardNo(traineeProfileDto.getAadharCardNo());
		}
		if (traineeProfileDto.getLandlineStd() != null) {
			existingProfile.setLandlineStd(traineeProfileDto.getLandlineStd());
		}
		if (traineeProfileDto.getLandlineNumber() != null) {
			existingProfile.setLandlineNumber(traineeProfileDto.getLandlineNumber());
		}
		if (traineeProfileDto.getMobileNumber1() != null) {
			existingProfile.setMobileNumber1(traineeProfileDto.getMobileNumber1());
		}
		if (traineeProfileDto.getMobileNumber2() != null) {
			existingProfile.setMobileNumber2(traineeProfileDto.getMobileNumber2());
		}
		if (traineeProfileDto.getSgsyCandidate() != null) {
			existingProfile.setSgsyCandidate(traineeProfileDto.getSgsyCandidate());
		}
		if (traineeProfileDto.getFamilyOccupation() != null) {
			existingProfile.setFamilyOccupation(traineeProfileDto.getFamilyOccupation());
		}
		if (traineeProfileDto.getCandidatePresentOccupation() != null) {
			existingProfile.setCandidatePresentOccupation(traineeProfileDto.getCandidatePresentOccupation());
		}
		if (traineeProfileDto.getNativityArea() != null) {
			existingProfile.setNativityArea(traineeProfileDto.getNativityArea());
		}
		if (traineeProfileDto.getCandidateAddress() != null) {
			existingProfile.setCandidateAddress(traineeProfileDto.getCandidateAddress());
		}
		if (traineeProfileDto.getVillage() != null) {
			existingProfile.setVillage(traineeProfileDto.getVillage());
		}
		if (traineeProfileDto.getHobli() != null) {
			existingProfile.setHobli(traineeProfileDto.getHobli());
		}
		if (traineeProfileDto.getDistrict() != null) {
			existingProfile.setDistrict(traineeProfileDto.getDistrict());
		}
		if (traineeProfileDto.getTaluk() != null) {
			existingProfile.setTaluk(traineeProfileDto.getTaluk());
		}
		if (traineeProfileDto.getPincode() != null) {
			existingProfile.setPincode(traineeProfileDto.getPincode());
		}
		if (traineeProfileDto.getCandidateSponsoredByBank() != null) {
			existingProfile.setCandidateSponsoredByBank(traineeProfileDto.getCandidateSponsoredByBank());
		}
		if (traineeProfileDto.getSponsoredBankName() != null) {
			existingProfile.setSponsoredBankName(traineeProfileDto.getSponsoredBankName());
		}
		if (traineeProfileDto.getSponsoredBankBranch() != null) {
			existingProfile.setSponsoredBankBranch(traineeProfileDto.getSponsoredBankBranch());
		}
		if (traineeProfileDto.getSponsoredBankCity() != null) {
			existingProfile.setSponsoredBankCity(traineeProfileDto.getSponsoredBankCity());
		}
		if (traineeProfileDto.getSponsorName() != null) {
			existingProfile.setSponsorName(traineeProfileDto.getSponsorName());
		}
		if (traineeProfileDto.getRelevantExperience() != null) {
			existingProfile.setRelevantExperience(traineeProfileDto.getRelevantExperience());
		}
		if (traineeProfileDto.getNameOfShg() != null) {
			existingProfile.setNameOfShg(traineeProfileDto.getNameOfShg());
		}
		if (traineeProfileDto.getFamilyMember() != null) {
			existingProfile.setFamilyMember(traineeProfileDto.getFamilyMember());
		}
		if (traineeProfileDto.getMnergaCardNo() != null) {
			existingProfile.setMnergaCardNo(traineeProfileDto.getMnergaCardNo());
		}
		
		if (traineeProfileDto.getEnrolledOn() != null) {
			existingProfile.setEnrolledOn(traineeProfileDto.getEnrolledOn());
		}
		
		 if (traineeProfileDto.getUsername() != null) {
	            TraineeCredential traineeCredential = existingProfile.getTrainee();
	            if (traineeCredential != null) {
	                traineeCredential.setUsername(traineeProfileDto.getUsername());
	            }
	        }
		traineeProfileDAO.save(existingProfile);
		// Convert the updated entity to DTO and return
		return traineeProfileMapper.toDTO(existingProfile);
	}

	
    public UserPrincipal getCurrentUser() {
        UserPrincipal userPrincipal = SecurityUtils.getCurrentUser();
        if (userPrincipal == null) {
            throw new RuntimeException("No authenticated user found");
        }

       return userPrincipal; 	
    }

}
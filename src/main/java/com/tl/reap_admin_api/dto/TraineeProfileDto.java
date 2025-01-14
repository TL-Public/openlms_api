package com.tl.reap_admin_api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TraineeProfileDto {

    private UUID uuid;
    private String enrollId;
    private LocalDate enrolledOn;
    private Integer status;
    private String batchNo;
    private String candidateName;
    private String fatherNameOrHusbandName;
    private String maritalStatus;
    private Integer age;
    private String religion;
    private String caste;
    private String education;
    private Boolean personWithDisability;
    private String sex;
    private String povertyLine;
    private String povertyLineNumberOrRationCardNumber;
    private String secc;
    private String seccNo;
    private String panNumber;
    private String residential;
    private LocalDate dateOfBirth;
    private String aadharCardNo;
    private String landlineStd;
    private String landlineNumber;
    private String mobileNumber1;
    private String mobileNumber2;
    private Boolean sgsyCandidate;
    private String familyOccupation;
    private String candidatePresentOccupation;
    private String nativityArea;
    private String candidateAddress;
    private String village;
    private String hobli;
    private String district;
    private String taluk;
    private String pincode;
    private Boolean candidateSponsoredByBank;
    private String sponsoredBankName;
    private String sponsoredBankBranch;
    private String sponsoredBankCity;
    private String sponsorName;
    private String relevantExperience;
    private String nameOfShg;
    private String familyMember;
    private String email;
    private String mnergaCardNo;
    private String username;
    private List<TraineeRsetiDto> traineeRsetis;

    // Constructors
    public TraineeProfileDto() {
    }

    public TraineeProfileDto(UUID uuid, String enrollId, LocalDate enrolledOn, Integer status, String batchNo,
                             String candidateName, String fatherNameOrHusbandName, String maritalStatus, Integer age,
                             String religion, String caste, String education, Boolean personWithDisability, String sex,
                             String povertyLine, String povertyLineNumberOrRationCardNumber, String secc, String seccNo,
                             String panNumber, String residential, LocalDate dateOfBirth, String aadharCardNo,
                             String landlineStd, String landlineNumber, String mobileNumber1, String mobileNumber2,
                             Boolean sgsyCandidate, String familyOccupation, String candidatePresentOccupation,
                             String nativityArea, String candidateAddress, String village, String hobli, String district,
                             String taluk, String pincode, Boolean candidateSponsoredByBank, String sponsoredBankName,
                             String sponsoredBankBranch, String sponsoredBankCity, String sponsorName,
                             String relevantExperience, String nameOfShg, String familyMember, String email,
                             String mnergaCardNo, String username, List<TraineeRsetiDto> traineeRsetis) {
        this.uuid = uuid;
        this.enrollId = enrollId;
        this.enrolledOn = enrolledOn;
        this.status = status;
        this.batchNo = batchNo;
        this.candidateName = candidateName;
        this.fatherNameOrHusbandName = fatherNameOrHusbandName;
        this.maritalStatus = maritalStatus;
        this.age = age;
        this.religion = religion;
        this.caste = caste;
        this.education = education;
        this.personWithDisability = personWithDisability;
        this.sex = sex;
        this.povertyLine = povertyLine;
        this.povertyLineNumberOrRationCardNumber = povertyLineNumberOrRationCardNumber;
        this.secc = secc;
        this.seccNo = seccNo;
        this.panNumber = panNumber;
        this.residential = residential;
        this.dateOfBirth = dateOfBirth;
        this.aadharCardNo = aadharCardNo;
        this.landlineStd = landlineStd;
        this.landlineNumber = landlineNumber;
        this.mobileNumber1 = mobileNumber1;
        this.mobileNumber2 = mobileNumber2;
        this.sgsyCandidate = sgsyCandidate;
        this.familyOccupation = familyOccupation;
        this.candidatePresentOccupation = candidatePresentOccupation;
        this.nativityArea = nativityArea;
        this.candidateAddress = candidateAddress;
        this.village = village;
        this.hobli = hobli;
        this.district = district;
        this.taluk = taluk;
        this.pincode = pincode;
        this.candidateSponsoredByBank = candidateSponsoredByBank;
        this.sponsoredBankName = sponsoredBankName;
        this.sponsoredBankBranch = sponsoredBankBranch;
        this.sponsoredBankCity = sponsoredBankCity;
        this.sponsorName = sponsorName;
        this.relevantExperience = relevantExperience;
        this.nameOfShg = nameOfShg;
        this.familyMember = familyMember;
        this.email = email;
        this.mnergaCardNo = mnergaCardNo;
        this.username = username;
        this.traineeRsetis = traineeRsetis;
    }

    // Getters and setters for all fields
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getEnrollId() {
        return enrollId;
    }

    public void setEnrollId(String enrollId) {
        this.enrollId = enrollId;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public void setEnrolledOn(LocalDate enrolledOn) {
        this.enrolledOn = enrolledOn;
    }

    public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getFatherNameOrHusbandName() {
        return fatherNameOrHusbandName;
    }

    public void setFatherNameOrHusbandName(String fatherNameOrHusbandName) {
        this.fatherNameOrHusbandName = fatherNameOrHusbandName;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getCaste() {
        return caste;
    }

    public void setCaste(String caste) {
        this.caste = caste;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public Boolean getPersonWithDisability() {
        return personWithDisability;
    }

    public void setPersonWithDisability(Boolean personWithDisability) {
        this.personWithDisability = personWithDisability;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPovertyLine() {
        return povertyLine;
    }

    public void setPovertyLine(String povertyLine) {
        this.povertyLine = povertyLine;
    }

    public String getPovertyLineNumberOrRationCardNumber() {
        return povertyLineNumberOrRationCardNumber;
    }

    public void setPovertyLineNumberOrRationCardNumber(String povertyLineNumberOrRationCardNumber) {
        this.povertyLineNumberOrRationCardNumber = povertyLineNumberOrRationCardNumber;
    }

    public String getSecc() {
        return secc;
    }

    public void setSecc(String secc) {
        this.secc = secc;
    }

    public String getSeccNo() {
        return seccNo;
    }

    public void setSeccNo(String seccNo) {
        this.seccNo = seccNo;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getResidential() {
        return residential;
    }

    public void setResidential(String residential) {
        this.residential = residential;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAadharCardNo() {
        return aadharCardNo;
    }

    public void setAadharCardNo(String aadharCardNo) {
        this.aadharCardNo = aadharCardNo;
    }

    public String getLandlineStd() {
        return landlineStd;
    }

    public void setLandlineStd(String landlineStd) {
        this.landlineStd = landlineStd;
    }

    public String getLandlineNumber() {
        return landlineNumber;
    }

    public void setLandlineNumber(String landlineNumber) {
        this.landlineNumber = landlineNumber;
    }

    public String getMobileNumber1() {
        return mobileNumber1;
    }

    public void setMobileNumber1(String mobileNumber1) {
        this.mobileNumber1 = mobileNumber1;
    }

    public String getMobileNumber2() {
        return mobileNumber2;
    }

    public void setMobileNumber2(String mobileNumber2) {
        this.mobileNumber2 = mobileNumber2;
    }

    public Boolean getSgsyCandidate() {
        return sgsyCandidate;
    }

    public void setSgsyCandidate(Boolean sgsyCandidate) {
        this.sgsyCandidate = sgsyCandidate;
    }

    public String getFamilyOccupation() {
        return familyOccupation;
    }

    public void setFamilyOccupation(String familyOccupation) {
        this.familyOccupation = familyOccupation;
    }

    public String getCandidatePresentOccupation() {
        return candidatePresentOccupation;
    }

    public void setCandidatePresentOccupation(String candidatePresentOccupation) {
        this.candidatePresentOccupation = candidatePresentOccupation;
    }

    public String getNativityArea() {
        return nativityArea;
    }

    public void setNativityArea(String nativityArea) {
        this.nativityArea = nativityArea;
    }

    public String getCandidateAddress() {
        return candidateAddress;
    }

    public void setCandidateAddress(String candidateAddress) {
        this.candidateAddress = candidateAddress;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getHobli() {
        return hobli;
    }

    public void setHobli(String hobli) {
        this.hobli = hobli;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTaluk() {
        return taluk;
    }

    public void setTaluk(String taluk) {
        this.taluk = taluk;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Boolean getCandidateSponsoredByBank() {
        return candidateSponsoredByBank;
    }

    public void setCandidateSponsoredByBank(Boolean candidateSponsoredByBank) {
        this.candidateSponsoredByBank = candidateSponsoredByBank;
    }

    public String getSponsoredBankName() {
        return sponsoredBankName;
    }

    public void setSponsoredBankName(String sponsoredBankName) {
        this.sponsoredBankName = sponsoredBankName;
    }

    public String getSponsoredBankBranch() {
        return sponsoredBankBranch;
    }

    public void setSponsoredBankBranch(String sponsoredBankBranch) {
        this.sponsoredBankBranch = sponsoredBankBranch;
    }

    public String getSponsoredBankCity() {
        return sponsoredBankCity;
    }

    public void setSponsoredBankCity(String sponsoredBankCity) {
        this.sponsoredBankCity = sponsoredBankCity;
    }

    public String getSponsorName() {
        return sponsorName;
    }

    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }

    public String getRelevantExperience() {
        return relevantExperience;
    }

    public void setRelevantExperience(String relevantExperience) {
        this.relevantExperience = relevantExperience;
    }

    public String getNameOfShg() {
        return nameOfShg;
    }

    public void setNameOfShg(String nameOfShg) {
        this.nameOfShg = nameOfShg;
    }

    public String getFamilyMember() {
        return familyMember;
    }

    public void setFamilyMember(String familyMember) {
        this.familyMember = familyMember;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMnergaCardNo() {
        return mnergaCardNo;
    }

    public void setMnergaCardNo(String mnergaCardNo) {
        this.mnergaCardNo = mnergaCardNo;
    }
    
    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
    
    public List<TraineeRsetiDto> getTraineeRsetis() {
        return traineeRsetis;
    }

    public void setTraineeRsetis(List<TraineeRsetiDto> traineeRsetis) {
        this.traineeRsetis = traineeRsetis;
    }
}
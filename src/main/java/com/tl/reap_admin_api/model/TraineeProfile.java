package com.tl.reap_admin_api.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "trainee_profiles")
public class TraineeProfile {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "uuid", updatable = false, nullable = false)
	private UUID uuid;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private TraineeCredential trainee;

	@OneToMany(mappedBy = "traineeProfile")
	private List<TraineeRseti> traineeRsetis;


	    @Column(name = "enroll_id")
	    private String enrollId;

	    @Column(name = "enrolled_on")
	    private LocalDate enrolledOn;

	    @Column(name = "status", nullable = false, columnDefinition = "integer default 1")
	    private Integer status = 1;

	    @Column(name = "batch_no")
	    private String batchNo;

	    @Column(name = "candidate_name")
	    private String candidateName;

	    @Column(name = "father_or_husband_name")
	    private String fatherNameOrHusbandName;

	    @Column(name = "marital_status")
	    private String maritalStatus;

	    private Integer age;

	    private String religion;

	    private String caste;

	    private String education;

	    @Column(name = "person_with_disability")
	    private Boolean personWithDisability;

	    private String sex;

	    @Column(name = "poverty_line")
	    private String povertyLine;

	    @Column(name = "poverty_line_number_or_ration_card_number")
	    private String povertyLineNumberOrRationCardNumber;

	    private String secc;

	    @Column(name = "secc_no")
	    private String seccNo;

	    @Column(name = "pan_number")
	    private String panNumber;

	    private String residential;

	    @Column(name = "date_of_birth")
	    private LocalDate dateOfBirth;

	    @Column(name = "aadhar_card_no")
	    private String aadharCardNo;

	    @Column(name = "landline_std")
	    private String landlineStd;

	    @Column(name = "landline_number")
	    private String landlineNumber;

	    @Column(name = "mobile_number_1")
	    private String mobileNumber1;

	    @Column(name = "mobile_number_2")
	    private String mobileNumber2;

	    @Column(name = "sgsy_candidate")
	    private Boolean sgsyCandidate;

	    @Column(name = "family_occupation")
	    private String familyOccupation;

	    @Column(name = "candidate_present_occupation")
	    private String candidatePresentOccupation;

	    @Column(name = "nativity_area")
	    private String nativityArea;

	    @Column(name = "candidate_address")
	    private String candidateAddress;

	    private String village;

	    private String hobli;

	    private String district;

	    private String taluk;

	    private String pincode;

	    @Column(name = "candidate_sponsored_by_bank")
	    private Boolean candidateSponsoredByBank;

	    @Column(name = "sponsored_bank_name")
	    private String sponsoredBankName;

	    @Column(name = "sponsored_bank_branch")
	    private String sponsoredBankBranch;

	    @Column(name = "sponsored_bank_city")
	    private String sponsoredBankCity;

	    @Column(name = "sponsor_name")
	    private String sponsorName;

	    @Column(name = "relevant_experience")
	    private String relevantExperience;

	    @Column(name = "name_of_shg")
	    private String nameOfShg;

	    @Column(name = "family_member")
	    private String familyMember;

	    private String email;

	    @Column(name = "mnrega_card_no")
	    private String mnergaCardNo;

	    // Getters and setters

	  

	    public String getEnrollId() {
	        return enrollId;
	    }

	    public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public UUID getUuid() {
			return uuid;
		}

		public void setUuid(UUID uuid) {
			this.uuid = uuid;
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

    public TraineeCredential getTrainee() {
        return trainee;
    }

    public void setTrainee(TraineeCredential trainee) {
        this.trainee = trainee;
    }

	public List<TraineeRseti> getTraineeRsetis() {
        return traineeRsetis;
    }

    public void setTraineeRsetis(List<TraineeRseti> traineeRsetis) {
        this.traineeRsetis = traineeRsetis;
    }
}

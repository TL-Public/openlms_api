package com.tl.reap_admin_api.dto;

import java.util.UUID;
import java.util.List;

public class RsetiListDto {
	private UUID uuid;
    private String extId;
    private String email;
    private String contactNo;
    private String directorContactNo;
    private Integer stateId;
    private UUID bankId;
    private int courseCount;
    private List<RsetiTranslationDto> translations;

    // Constructor
    public RsetiListDto(UUID uuid, String extId, String email, String contactNo, 
                        String directorContactNo, Integer stateId, UUID bankId, 
                        int courseCount, List<RsetiTranslationDto> translations) {
        this.uuid = uuid;
        this.extId = extId;
        this.email = email;
        this.contactNo = contactNo;
        this.directorContactNo = directorContactNo;
        this.stateId = stateId;
        this.bankId = bankId;
        this.courseCount = courseCount;
        this.translations = translations;
    }

    // Getters and setters

    public List<RsetiTranslationDto> getTranslations() {
        return translations;
    }

    public void setTranslations(List<RsetiTranslationDto> translations) {
        this.translations = translations;
    }

    

    public int getCourseCount() {
        return courseCount;
    }

    public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getDirectorContactNo() {
		return directorContactNo;
	}

	public void setDirectorContactNo(String directorContactNo) {
		this.directorContactNo = directorContactNo;
	}

	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}

	public UUID getBankId() {
		return bankId;
	}

	public void setBankId(UUID bankId) {
		this.bankId = bankId;
	}

	public void setCourseCount(int courseCount) {
        this.courseCount = courseCount;
    }
}
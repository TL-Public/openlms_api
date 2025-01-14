package com.tl.reap_admin_api.dto;

import java.util.List;
import java.util.UUID;

public class RsetiDto {
    private UUID uuid;
    private UUID bankId;
    private Integer stateId;
    private String extId;
    private String email;
    private String contactNo;
    private String directorContactNo;
    private Integer status;
    private List<RsetiTranslationDto> translations;

    // Constructors, getters, and setters

    public RsetiDto() {
    }

    public RsetiDto(UUID uuid, UUID bankId, Integer stateId, String extId, String email,
                    String contactNo, String directorContactNo, Integer status) {
       
        this.uuid = uuid;
        this.bankId = bankId;
        this.stateId = stateId;
        this.extId = extId;
        this.email = email;
        this.contactNo = contactNo;
        this.directorContactNo = directorContactNo;
        this.status = status;
    }

    // Getters and setters for all fields

  

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getBankId() {
        return bankId;
    }

    public void setBankId(UUID bankId) {
        this.bankId = bankId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
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

    public List<RsetiTranslationDto> getTranslations() {
        return translations;
    }

    public void setTranslations(List<RsetiTranslationDto> translations) {
        this.translations = translations;
    }

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
    
    
}
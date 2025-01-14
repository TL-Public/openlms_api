package com.tl.reap_admin_api.dto;

public class DistrictDto {
    private Integer extId;
    private String name;
    private String languageCode;

    // Default constructor
    public DistrictDto() {}

    // Constructor with all fields
    public DistrictDto(Integer extId, String name, String languageCode) {
        this.extId = extId;
        this.name = name;
        this.languageCode = languageCode;
    }

    // Getters and setters
    public Integer getExtId() {
        return extId;
    }

    public void setExtId(Integer extId) {
        this.extId = extId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
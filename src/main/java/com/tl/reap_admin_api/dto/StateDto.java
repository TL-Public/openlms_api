package com.tl.reap_admin_api.dto;

import java.util.List;
import java.util.ArrayList;

public class StateDto {
    private Integer extId;
    private String name;
    private String isoCode;
    private String languageCode;
    private List<DistrictDto> districts;

    // Default constructor
    public StateDto() {
        this.districts = new ArrayList<>();
    }

    // Constructor with all fields
    public StateDto(Integer extId, String name, String isoCode, String languageCode) {
        this.extId = extId;
        this.name = name;
        this.isoCode = isoCode;
        this.languageCode = languageCode;
        this.districts = new ArrayList<>();
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

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public List<DistrictDto> getDistricts() {
        return districts;
    }

    public void setDistricts(List<DistrictDto> districts) {
        this.districts = districts;
    }

    // Method to add a single district
    public void addDistrict(DistrictDto district) {
        if (this.districts == null) {
            this.districts = new ArrayList<>();
        }
        this.districts.add(district);
    }
}
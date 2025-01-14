package com.tl.reap_admin_api.dto;

public class RsetiTranslationDto {
    private Long id;
    private String languageCode;
    private String district;
    private Integer districtId;
    private String name;
    private String address;
    private String directorName;

    // Constructors, getters, and setters

    public RsetiTranslationDto() {
    }

    public RsetiTranslationDto(Long id, String languageCode, String district, Integer districtId, String name,
                               String address, String directorName) {
        this.id = id;
        this.languageCode = languageCode;
        this.district = district;
        this.districtId = districtId;
        this.name = name;
        this.address = address;
        this.directorName = directorName;
    }

    // Getters and setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDirectorName() {
        return directorName;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }
}
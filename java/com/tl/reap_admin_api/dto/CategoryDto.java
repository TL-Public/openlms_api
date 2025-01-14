package com.tl.reap_admin_api.dto;

public class CategoryDto {

    private Integer extId;
    private String name;
    private String languageCode;

    public CategoryDto(Integer extId, String name, String languageCode) {
        this.extId = extId;
        this.name = name;
        this.languageCode = languageCode;
    }

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


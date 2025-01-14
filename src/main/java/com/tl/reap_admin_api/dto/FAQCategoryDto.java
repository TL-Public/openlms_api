package com.tl.reap_admin_api.dto;

public class FAQCategoryDto {
    private Integer extId;
    private String category;
    private String languageCode;

    public FAQCategoryDto() {}

    public FAQCategoryDto(Integer extId, String category, String languageCode) {
        this.extId = extId;
        this.category = category;
        this.languageCode = languageCode;
    }

    public Integer getExtId() {
        return extId;
    }

    public void setExtId(Integer extId) {
        this.extId = extId;
    }

  

    public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
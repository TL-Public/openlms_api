package com.tl.reap_admin_api.dto;

public class LanguageCountDto {
    private String languageCode;
    private Long count;

    public LanguageCountDto(String languageCode, Long count) {
        this.languageCode = languageCode;
        this.count = count;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
package com.tl.reap_admin_api.dto;

public class AppConfigDTO {
    private String key;
    private String value;

    // Default constructor
    public AppConfigDTO() {}

    // Constructor with parameters
    public AppConfigDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
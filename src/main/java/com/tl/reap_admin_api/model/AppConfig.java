package com.tl.reap_admin_api.model;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_config")
public class AppConfig {

    @Id
    @Column(name = "key")
    private String key;
    
    @Column(name = "value")
    private String value;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    // Default constructor
    public AppConfig() {}

    // Constructor with parameters
    public AppConfig(String key, String value, String createdBy) {
        this.key = key;
        this.value = value;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
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

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Override toString() method
    @Override
    public String toString() {
        return "AppConfig{" +
               "key='" + key + '\'' +
               ", value='" + value + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ", createdBy='" + createdBy + '\'' +
               ", updatedBy='" + updatedBy + '\'' +
               '}';
    }
}
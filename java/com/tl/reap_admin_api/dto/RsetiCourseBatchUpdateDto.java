package com.tl.reap_admin_api.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public class RsetiCourseBatchUpdateDto {
    private UUID uuid;
    private Long rsetiId;
    private Long courseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ZonedDateTime createdAt;
    private String createdBy;
    private ZonedDateTime updatedAt;
    private String updatedBy;

    // Default constructor
    public RsetiCourseBatchUpdateDto() {
    }

    // Constructor with all fields
    public RsetiCourseBatchUpdateDto(UUID uuid, Long rsetiId, Long courseId, LocalDate startDate, LocalDate endDate, ZonedDateTime updatedAt, String updatedBy) {
        this.uuid = uuid;
        this.rsetiId = rsetiId;
        this.courseId = courseId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    // Getters and setters
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Long getRsetiId() {
        return rsetiId;
    }

    public void setRsetiId(Long rsetiId) {
        this.rsetiId = rsetiId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "RsetiCourseBatchUpdateDto{" +
                "uuid=" + uuid +
                ", rsetiId=" + rsetiId +
                ", courseId=" + courseId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
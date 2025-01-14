package com.tl.reap_admin_api.dto;

import java.time.LocalDate;
import java.util.UUID;

public class TraineeRsetiDto {
    private UUID uuid;
    private String enrollId;
    private LocalDate enrolledOn;
    private UUID rsetiUuid;
    private UUID traineeProfileUuid;
    private UUID rsetiCourseUuid;
    private UUID courseUuid;
    private Integer status;

    // Constructors
    public TraineeRsetiDto() {}

    public TraineeRsetiDto(UUID uuid, String enrollId, LocalDate enrolledOn, UUID rsetiUuid, UUID traineeProfileUuid,UUID rsetiCourseUuid, UUID courseUuid, Integer status) {
        this.uuid = uuid;
        this.enrollId = enrollId;
        this.enrolledOn = enrolledOn;
        this.rsetiUuid = rsetiUuid;
        this.traineeProfileUuid = traineeProfileUuid;
        this.rsetiCourseUuid = rsetiCourseUuid; 
        this.courseUuid = courseUuid;
        this.status = status;
    }

    // Getters and setters for all fields

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getEnrollId() {
        return enrollId;
    }

    public void setEnrollId(String enrollId) {
        this.enrollId = enrollId;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public void setEnrolledOn(LocalDate enrolledOn) {
        this.enrolledOn = enrolledOn;
    }

    public UUID getRsetiUuid() {
        return rsetiUuid;
    }

    public void setRsetiUuid(UUID rsetiUuid) {
        this.rsetiUuid = rsetiUuid;
    }

    public UUID getTraineeProfileUuid() {
        return traineeProfileUuid;
    }

    public void setTraineeProfileUuid(UUID traineeProfileUuid) {
        this.traineeProfileUuid = traineeProfileUuid;
    }

    public UUID getRsetiCourseUuid() {
        return rsetiCourseUuid;
    }

    public void setRsetiCourseUuid(UUID rsetiCourseUuid) {
        this.rsetiCourseUuid = rsetiCourseUuid;
    }

    public UUID getCourseUuid() {
        return courseUuid;
    }

    public void setCourseUuid(UUID courseUuid) {
        this.courseUuid = courseUuid;
    }

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
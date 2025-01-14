package com.tl.reap_admin_api.dto;

import java.time.LocalDate;
import java.util.UUID;

public class TraineeCourseDto {
    private UUID traineeUuid;
    private LocalDate enrollmentDate;

    // Getters and setters

    public UUID getTraineeUuid() {
        return traineeUuid;
    }

    public void setTraineeUuid(UUID traineeUuid) {
        this.traineeUuid = traineeUuid;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}


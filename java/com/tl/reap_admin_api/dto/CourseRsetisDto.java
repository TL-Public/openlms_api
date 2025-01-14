package com.tl.reap_admin_api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CourseRsetisDto {
    private List<RsetiCourseInfo> rsetiCourses;

    public CourseRsetisDto() {}

    public CourseRsetisDto(List<RsetiCourseInfo> rsetiCourses) {
        this.rsetiCourses = rsetiCourses;
    }

    public List<RsetiCourseInfo> getRsetiCourses() {
        return rsetiCourses;
    }

    public void setRsetiCourses(List<RsetiCourseInfo> rsetiCourses) {
        this.rsetiCourses = rsetiCourses;
    }

    public static class RsetiCourseInfo {
        private UUID rsetiUuid;
        private LocalDate startDate;

        public RsetiCourseInfo(UUID rsetiUuid, LocalDate startDate) {
            this.rsetiUuid = rsetiUuid;
            this.startDate = startDate;
        }

        public UUID getRsetiUuid() {
            return rsetiUuid;
        }

        public void setRsetiUuid(UUID rsetiUuid) {
            this.rsetiUuid = rsetiUuid;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }
    }
}
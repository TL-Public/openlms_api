package com.tl.reap_admin_api.dto;

public class CourseStatisticsDto {
    private long totalChapters;
    private long totalVideoDuration;
    private long courseDuration;
    private long numberOfStudents;
    private long totalRsetis;

    // Constructors
    public CourseStatisticsDto() {}

    public CourseStatisticsDto(long totalChapters, long totalVideoDuration, long courseDuration, long numberOfStudents, long totalRsetis) {
        this.totalChapters = totalChapters;
        this.totalVideoDuration = totalVideoDuration;
        this.courseDuration = courseDuration;
        this.numberOfStudents = numberOfStudents;
        this.totalRsetis = totalRsetis;
    }

    // Getters and setters
    public long getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(long totalChapters) {
        this.totalChapters = totalChapters;
    }

    public long getTotalVideoDuration() {
        return totalVideoDuration;
    }

    public void setTotalVideoDuration(long totalVideoDuration) {
        this.totalVideoDuration = totalVideoDuration;
    }

    public long getCourseDuration() {
        return courseDuration;
    }

    public void setCourseDuration(long courseDuration) {
        this.courseDuration = courseDuration;
    }

    public long getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(long numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public long getTotalRsetis() {
        return totalRsetis;
    }

    public void setTotalRsetis(long totalRsetis) {
        this.totalRsetis = totalRsetis;
    }
}
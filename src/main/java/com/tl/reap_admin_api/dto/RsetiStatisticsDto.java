package com.tl.reap_admin_api.dto;

public class RsetiStatisticsDto {
    private long totalCourses;
    private long totalTrainees;

    // Getters and setters
    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getTotalTrainees() {
        return totalTrainees;
    }

    public void setTotalTrainees(long totalTrainees) {
        this.totalTrainees = totalTrainees;
    }
}
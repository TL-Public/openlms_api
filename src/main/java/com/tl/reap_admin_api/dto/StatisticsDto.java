package com.tl.reap_admin_api.dto;

public class StatisticsDto {
    private long totalCourses;
    private long totalTrainingCenterCourses;
    private long totalTrainingCenters;
    private long totalTraineesEnrolledThisYear;
    private long totalCategories;
    private long totalCourseDuration;
    private long totalVideoDuration;
    private long totalVideos;
    private long totalTrainees;
    private long totalStates;
    private double avgTraineePerState;
    private double avgTraineePerRSETI;
    private double avgRSETIPerState;

    // Getters
    public long getTotalCourses() {
        return totalCourses;
    }

    public long getTotalTrainingCenters() {
        return totalTrainingCenters;
    }

    public long getTotalTrainingCenterCourses() {
        return totalTrainingCenterCourses;
    }

    public long getTotalTraineesEnrolledThisYear() {
        return totalTraineesEnrolledThisYear;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public long getTotalCourseDuration() {
        return totalCourseDuration;
    }

    public long getTotalVideoDuration() {
        return totalVideoDuration;
    }

    public long getTotalVideos() {
        return totalVideos;
    }

    public long getTotalTrainees() {
        return totalTrainees;
    }

    public long getTotalStates() {
        return totalStates;
    }

    public double getAvgTraineePerState() {
        return avgTraineePerState;
    }

    public double getAvgTraineePerRSETI() {
        return avgTraineePerRSETI;
    }

    public double getAvgRSETIPerState() {
        return avgRSETIPerState;
    }

    // Setters
    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public void setTotalTrainingCenters(long totalTrainingCenters) {
        this.totalTrainingCenters = totalTrainingCenters;
    }

    public void setTotalTrainingCenterCourses(long totalTrainingCenterCourses) {
        this.totalTrainingCenterCourses = totalTrainingCenterCourses;
    }
    
    public void setTotalTraineesEnrolledThisYear(long totalTraineesEnrolledThisYear) {
        this.totalTraineesEnrolledThisYear = totalTraineesEnrolledThisYear;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public void setTotalCourseDuration(long totalCourseDuration) {
        this.totalCourseDuration = totalCourseDuration;
    }

    public void setTotalVideoDuration(long totalVideoDuration) {
        this.totalVideoDuration = totalVideoDuration;
    }

    public void setTotalVideos(long totalVideos) {
        this.totalVideos = totalVideos;
    }

    public void setTotalTrainees(long totalTrainees) {
        this.totalTrainees = totalTrainees;
    }

    public void setTotalStates(long totalStates) {
        this.totalStates = totalStates;
    }

    public void setAvgTraineePerState(double avgTraineePerState) {
        this.avgTraineePerState = avgTraineePerState;
    }

    public void setAvgTraineePerRSETI(double avgTraineePerRSETI) {
        this.avgTraineePerRSETI = avgTraineePerRSETI;
    }

    public void setAvgRSETIPerState(double avgRSETIPerState) {
        this.avgRSETIPerState = avgRSETIPerState;
    }
}
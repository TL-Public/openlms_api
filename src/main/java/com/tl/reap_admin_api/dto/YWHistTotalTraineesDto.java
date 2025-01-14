package com.tl.reap_admin_api.dto;

public class YWHistTotalTraineesDto {
    private String id;
    private String year;
    private Long trainedCount;
    private Long settledCount;

    public YWHistTotalTraineesDto(Long trainedCount, Long settledCount) {
        this.trainedCount = trainedCount;
        this.settledCount = settledCount;
    }

    public YWHistTotalTraineesDto(Long id, String year, Long trainedCount, Long settledCount) {
        this.id = String.valueOf(id);
        this.year = year;
        this.trainedCount = trainedCount;
        this.settledCount = settledCount;
    }

    public YWHistTotalTraineesDto(String id, String year, Long trainedCount, Long settledCount) {
        this.id = id;
        this.year = year;
        this.trainedCount = trainedCount;
        this.settledCount = settledCount;
    }
    
    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
    public Long getTrainedCount() {
        return trainedCount;
    }

    public void setTrainedCount(Long trainedCount) {
        this.trainedCount = trainedCount;
    }

    public Long getSettledCount() {
        return settledCount;
    }

    public void setSettledCount(Long settledCount) {
        this.settledCount = settledCount;
    }
}
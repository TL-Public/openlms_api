package com.tl.reap_admin_api.dto;

import java.util.Arrays;

public class HistoricDataMonthWiseDto {
    private String stateName;
    private Long stateId;
    private String epds;
    private Long categoryId;
    private String courseCode;
    private String courseName;
    private MonthlyData[] monthlyData;

    public HistoricDataMonthWiseDto() {
        this.monthlyData = new MonthlyData[12];
        Arrays.fill(this.monthlyData, new MonthlyData(0L, 0L));
    }

    // Getters and setters for all fields

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public String getEpds() {
        return epds;
    }

    public void setEpds(String epds) {
        this.epds = epds;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public MonthlyData[] getMonthlyData() {
        return monthlyData;
    }

    public void setMonthlyData(MonthlyData[] monthlyData) {
        this.monthlyData = monthlyData;
    }

    public static class MonthlyData {
        private Long programmesConducted;
        private Long candidatesTrained;

        public MonthlyData(Long programmesConducted, Long candidatesTrained) {
            this.programmesConducted = programmesConducted;
            this.candidatesTrained = candidatesTrained;
        }

        // Getters and setters

        public Long getProgrammesConducted() {
            return programmesConducted;
        }

        public void setProgrammesConducted(Long programmesConducted) {
            this.programmesConducted = programmesConducted;
        }

        public Long getCandidatesTrained() {
            return candidatesTrained;
        }

        public void setCandidatesTrained(Long candidatesTrained) {
            this.candidatesTrained = candidatesTrained;
        }
    }
}
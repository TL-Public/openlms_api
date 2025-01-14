package com.tl.reap_admin_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "historicdata")
public class HistoricDataMonthWise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "state_id")
    private Long stateId;

    @Column(name = "state_name")
    private String stateName;

    @Column(name = "course_code")
    private String courseCode;

    @Column(name = "mnth")
    private Long month;

    @Column(name = "crs_cnt")
    private Long courseCount;

    @Column(name = "trainee_cnt")
    private Long traineeCount;

    // Getters and setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Long getMonth() {
        return month;
    }

    public void setMonth(Long month) {
        this.month = month;
    }

    public Long getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(Long courseCount) {
        this.courseCount = courseCount;
    }

    public Long getTraineeCount() {
        return traineeCount;
    }

    public void setTraineeCount(Long traineeCount) {
        this.traineeCount = traineeCount;
    }
}
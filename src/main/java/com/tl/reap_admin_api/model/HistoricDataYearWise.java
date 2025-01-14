package com.tl.reap_admin_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "historic_data_yearwise")
public class HistoricDataYearWise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rseti_uuid", nullable = false)
    private String rsetiUuid;

    @Column(name = "state_id", nullable = false)
    private Integer stateId;

    @Column(name = "fiscal_year", nullable = false)
    private String fiscalYear;

    @Column(name = "trained_count", nullable = false)
    private Integer trainedCount;

    @Column(name = "settled_count", nullable = false)
    private Integer settledCount;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRsetiUuid() {
        return rsetiUuid;
    }

    public void setRsetiUuid(String rsetiUuid) {
        this.rsetiUuid = rsetiUuid;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public String getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(String fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public Integer getTrainedCount() {
        return trainedCount;
    }

    public void setTrainedCount(Integer trainedCount) {
        this.trainedCount = trainedCount;
    }

    public Integer getSettledCount() {
        return settledCount;
    }

    public void setSettledCount(Integer settledCount) {
        this.settledCount = settledCount;
    }
}
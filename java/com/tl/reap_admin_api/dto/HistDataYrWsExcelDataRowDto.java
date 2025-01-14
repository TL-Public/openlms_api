package com.tl.reap_admin_api.dto;

import java.util.Map;
import java.util.HashMap;

public class HistDataYrWsExcelDataRowDto {
    private String state;
    private String district;
    private String rsetiName;
    private Map<String, YearlyData> yearlyData = new HashMap<>();

    public static class YearlyData {
        private Integer trained;
        private Integer settled;

        public YearlyData(Integer trained, Integer settled) {
            this.trained = trained;
            this.settled = settled;
        }

        public Integer getTrained() {
            return trained;
        }

        public Integer getSettled() {
            return settled;
        }
    }

    // Getters and setters
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getRsetiName() {
        return rsetiName;
    }

    public void setRsetiName(String rsetiName) {
        this.rsetiName = rsetiName;
    }

    public Map<String, YearlyData> getYearlyData() {
        return yearlyData;
    }

    public void addYearlyData(String fiscalYear, Integer trained, Integer settled) {
        this.yearlyData.put(fiscalYear, new YearlyData(trained, settled));
    }
}
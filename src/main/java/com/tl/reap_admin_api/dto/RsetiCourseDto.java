package com.tl.reap_admin_api.dto;

import java.util.UUID;

public class RsetiCourseDto {
	private UUID	uuid;
	private UUID courseUuid;
	private UUID rsetiUuid;
	private int startYear;
	private int startMonth;
	private int endYear;
	private int endMonth;
	private Integer status;


	// Default constructor
	public RsetiCourseDto() {
	}

	// Constructor with all fields
	public RsetiCourseDto( UUID uuid, UUID courseUuid, UUID rsetiUuid,int startYear, int startMonth, int endYear, int endMonth, Integer status) {
		this.uuid = uuid;
		this.courseUuid = courseUuid;
		this.rsetiUuid = rsetiUuid;
		this.startYear = startYear;
		this.startMonth = startMonth;
		this.endYear = endYear;
		this.endMonth = endMonth;	
		this.status = status;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getCourseUuid() {
		return courseUuid;
	}

	public void setCourseUuid(UUID courseUuid) {
		this.courseUuid = courseUuid;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public int getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	public int getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(int endMonth) {
		this.endMonth = endMonth;
	}

	public UUID getRsetiUuid() {
		return rsetiUuid;
	}

	public void setRsetiUuid(UUID rsetiUuid) {
		this.rsetiUuid = rsetiUuid;
	}
	

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "RsetiCourseDto [uuid=" + uuid + ", courseUuid=" + courseUuid + ", rsetiUuid=" + rsetiUuid
				+ ", startYear=" + startYear + ", startMonth=" + startMonth + ", endYear=" + endYear + ", endMonth="
				+ endMonth + ", status=" + status + "]";
	}

	
}

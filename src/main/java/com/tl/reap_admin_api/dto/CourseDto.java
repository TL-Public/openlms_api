package com.tl.reap_admin_api.dto;

import java.util.UUID;
import java.util.List;
public class CourseDto {
    private UUID uuid;
    private String courseCode;
    private Integer duration;
    private Integer category;
    private Integer status;
    private int numberOfChapters;
    private int numberOfVideos;
    private String displayCourseCode;
    
    private String imageUrl;
    private List<CourseTranslationDto> translations;
    private List<ChapterDto> chapters;

    // Constructors, getters, and setters

    public CourseDto() {
    }

    public CourseDto(UUID uuid, String courseCode, Integer duration, Integer category, Integer status, int numberOfChapters, int numberOfVideos,String displayCourseCode, String imageUrl, List<CourseTranslationDto> translations) {
        this.uuid = uuid;
        this.courseCode = courseCode;
        this.duration = duration;        
        this.numberOfChapters = numberOfChapters;
        this.numberOfVideos = numberOfVideos;
        this.displayCourseCode= displayCourseCode;
        this.imageUrl = imageUrl;
        this.translations = translations;
        this.category = category;
        this.status = status;
    }

    // Getters and setters for all fields

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public int getNumberOfChapters() {
        return numberOfChapters;
    }

    public void setNumberOfChapters(int numberOfChapters) {
        this.numberOfChapters = numberOfChapters;
    }

    public int getNumberOfVideos() {
        return numberOfVideos;
    }

    public void setNumberOfVideos(int numberOfVideos) {
        this.numberOfVideos = numberOfVideos;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<CourseTranslationDto> getTranslations() {
        return translations;
    }

    public void setTranslations(List<CourseTranslationDto> translations) {
        this.translations = translations;
    }

    public List<ChapterDto> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterDto> chapters) {
        this.chapters = chapters;
    }
    

    public String getDisplayCourseCode() {
		return displayCourseCode;
	}

	public void setDisplayCourseCode(String displayCourseCode) {
		this.displayCourseCode = displayCourseCode;
	}

	@Override  
    public String toString() {
        return "CourseDto{" +
                "uuid=" + uuid +
                ", courseCode='" + courseCode + '\'' +
                ", duration=" + duration +
                ", numberOfChapters=" + numberOfChapters +
                ", numberOfVideos=" + numberOfVideos +
                ", displayCourseCode+displayCourseCode"+
                ", imageUrl='" + imageUrl + '\'' +
                ", translations=" + translations +
                ", chapters=" + chapters +
                '}';
    }
}

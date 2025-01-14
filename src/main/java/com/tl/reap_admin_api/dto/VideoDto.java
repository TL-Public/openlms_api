package com.tl.reap_admin_api.dto;

import java.util.UUID;
import java.util.List;

public class VideoDto {
    private UUID uuid;
    private String name;
    private String description;
    private String thumbnail;
    private Integer duration;
    private Integer orderNumber;
    private String extId;
    private String url;
    private String languageCode;
    private List<ChapterDto> chapters;
    private List<CourseDto> courses;
    private List<ChapterVideoDto> chapterVideos;
    private Integer status;

   
    // Constructors, getters, and setters

    public VideoDto() {
    }

    // Getters and setters for all fields

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public List<ChapterDto> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterDto> chapters) {
        this.chapters = chapters;
    }

    public List<CourseDto> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseDto> courses) {
        this.courses = courses;
    }

    public List<ChapterVideoDto> getChapterVideos() {
        return chapterVideos;
    }

    public void setChapterVideos(List<ChapterVideoDto> chapterVideos) {
        this.chapterVideos = chapterVideos;
    }

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
package com.tl.reap_admin_api.dto;

import java.util.UUID;
import java.util.List;
import java.util.Set;


public class ChapterDto {
    private UUID uuid;
    private UUID courseId;
    private Integer orderNumber;
    private Integer numberOfVideos;
    private Integer status;
    private List<ChapterTranslationDto> translations;
    private Set<ChapterVideoDto> chapterVideos;
    private Set<VideoDto> videos;
    // Constructors, getters, and setters

    public ChapterDto() {
    }

  
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getCourseUuid() {
        return courseId;
    }

    public void setCourseUuid(UUID courseUuid) {
        this.courseId = courseUuid;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
     
    public List<ChapterTranslationDto> getTranslations() {
        return translations;
    }

    public void setTranslations(List<ChapterTranslationDto> translations) {
        this.translations = translations;
    }    

    public Set<ChapterVideoDto> getChapterVideos() {
        return chapterVideos;
    }

    public void setChapterVideos(Set<ChapterVideoDto> chapterVideos) {
        this.chapterVideos = chapterVideos;
    }

    public Set<VideoDto> getVideos() {
        return videos;
    }

    public void setVideos(Set<VideoDto> videos) {
        this.videos = videos;
    }

    public int setNumberOfVideos(int numberOfVideos) {
        return this.numberOfVideos = numberOfVideos;
    }

    public int getNumberOfVideos() {
       
        if(videos != null) {
            return videos.size();
        } else if (chapterVideos != null) {
            return chapterVideos.size();
        }       
        return this.numberOfVideos;
    }


	public UUID getCourseId() {
		return courseId;
	}


	public void setCourseId(UUID courseId) {
		this.courseId = courseId;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public void setNumberOfVideos(Integer numberOfVideos) {
		this.numberOfVideos = numberOfVideos;
	}
    
}
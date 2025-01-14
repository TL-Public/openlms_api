package com.tl.reap_admin_api.dto;

import org.springframework.http.HttpStatus;

public class VideoResponse {
    private HttpStatus status;
    private VideoDto video;
    private String message;

    public VideoResponse(HttpStatus status, VideoDto video, String message) {
        this.status = status;
        this.video = video;
        this.message = message;
    }

    // Getters and setters

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public VideoDto getVideo() {
        return video;
    }

    public void setVideo(VideoDto video) {
        this.video = video;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
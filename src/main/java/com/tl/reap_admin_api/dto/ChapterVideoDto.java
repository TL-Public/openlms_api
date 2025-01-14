package com.tl.reap_admin_api.dto;

import java.util.UUID;

public class ChapterVideoDto {
    private UUID chapterUuid;
    private UUID videoUuid;
    private VideoDto video;
    private Integer orderNumber;

    // Constructors
    public ChapterVideoDto() {}

    public ChapterVideoDto(UUID chapterUuid, VideoDto video, Integer orderNumber) {
        this.chapterUuid = chapterUuid;
        this.video = video;
        this.orderNumber = orderNumber;
    }

    // Getters and Setters
    public UUID getChapterUuid() {
        return chapterUuid;
    }

    public void setChapterUuid(UUID chapterUuid) {
        this.chapterUuid = chapterUuid;
    }

    // Getters and Setters
    public UUID getVideoUuid() {
        return videoUuid;
    }

    public void setVideoUuid(UUID videoUuid) {
        this.videoUuid = videoUuid;
    }

    public VideoDto getVideo() {
        return video;
    }

    public void setVideo(VideoDto video) {
        this.video = video;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
}
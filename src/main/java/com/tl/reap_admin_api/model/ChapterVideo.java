package com.tl.reap_admin_api.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;


//Needed this entity to add order number to the relationship between chapters and videos
@Entity
@Table(name = "chapter_videos")
public class ChapterVideo {

    @EmbeddedId
    private ChapterVideoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chapterId")
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("videoId")
    @JoinColumn(name = "video_id")
    private Video video;

    @Column(name = "order_number")
    private Integer orderNumber;

    // Constructors, getters, and setters

    public ChapterVideo() {}

    public ChapterVideo(Chapter chapter, Video video, Integer orderNumber) {
        this.chapter = chapter;
        this.video = video;
        this.orderNumber = orderNumber;
        this.id = new ChapterVideoId(chapter.getId(), video.getId());
    }

    public ChapterVideoId getId() {
        return id;
    }

    public void setId(ChapterVideoId id) {
        this.id = id;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
        if (this.id == null) {
            this.id = new ChapterVideoId();
        }
        if(chapter != null)
        {
            this.id.setChapterId(chapter.getId());
        }
        
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
        if (this.id == null) {
            this.id = new ChapterVideoId();
        }
        if(video != null) {
            this.id.setVideoId(video.getId());
        } 
        
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
    // Getters and setters

    @Embeddable
    public static class ChapterVideoId implements Serializable {
        @Column(name = "chapter_id")
        private Long chapterId;

        @Column(name = "video_id")
        private Long videoId;

        public ChapterVideoId() {}

        public ChapterVideoId(Long chapterId, Long videoId) {
            this.chapterId = chapterId;
            this.videoId = videoId;
        }

        public Long getChapterId() {
            return chapterId;
        }

        public void setChapterId(Long chapterId) {
            this.chapterId = chapterId;
        }

        public Long getVideoId() {
            return videoId;
        }

        public void setVideoId(Long videoId) {
            this.videoId = videoId;
        }
       
         // equals and hashCode methods
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChapterVideoId that = (ChapterVideoId) o;
            return Objects.equals(chapterId, that.chapterId) &&
                   Objects.equals(videoId, that.videoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chapterId, videoId);
        }
    }

    // equals and hashCode methods
}
package com.tl.reap_admin_api.model;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "chapters")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChapterTranslation> translations = new HashSet<>();

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNumber ASC")
    private Set<ChapterVideo> chapterVideos = new HashSet<>();
    
    @Column(name = "status", nullable = false, columnDefinition = "integer default 1")
    private Integer status = 1;

    // Constructors, getters, and setters

    public Chapter() {
        this.uuid = UUID.randomUUID();
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    // Getters and setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Set<ChapterTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<ChapterTranslation> translations) {
        this.translations = translations;
    }

    public Set<ChapterVideo> getChapterVideos() {
        return chapterVideos;
    }

    public void setChapterVideos(Set<ChapterVideo> chapterVideos) {
        this.chapterVideos = chapterVideos;
    }

     // Helper methods

     public void addChapterVideo(ChapterVideo chapterVideo) {
        chapterVideos.add(chapterVideo);
        chapterVideo.setChapter(this);
    }

     public void addVideo(Video video, Integer orderNumber) {
        ChapterVideo chapterVideo = new ChapterVideo(this, video, orderNumber);
        chapterVideos.add(chapterVideo);
        video.getChapterVideos().add(chapterVideo);
    }

    public void removeVideo(Video video) {
        for (ChapterVideo chapterVideo : new HashSet<>(chapterVideos)) {
            if (chapterVideo.getVideo().equals(video)) {
                chapterVideos.remove(chapterVideo);
                video.getChapterVideos().remove(chapterVideo);
                chapterVideo.setChapter(null);
                chapterVideo.setVideo(null);
            }
        }
    }
    public void addTranslation(ChapterTranslation translation) {
        translations.add(translation);
        translation.setChapter(this);
    }

    public void removeTranslation(ChapterTranslation translation) {
        translations.remove(translation);
        translation.setChapter( null);
    }

    public int getNumberOfVideos2() {
        return 2;//chapterVideos.size();
    }

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
    
    
}       
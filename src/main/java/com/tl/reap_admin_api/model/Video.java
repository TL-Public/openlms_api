package com.tl.reap_admin_api.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "ext_id")
    private String extId;

    @Column(name = "url")
    private String url;
   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(name = "status", nullable = false, columnDefinition = "integer default 1")
    private Integer status = 1;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNumber ASC")
    private Set<ChapterVideo> chapterVideos = new HashSet<>();

    @ManyToMany(mappedBy = "videos")
    private Set<Playlist> playlists = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    // Constructors, getters, and setters

    public Video() {
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

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

  

    public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Set<ChapterVideo> getChapterVideos() {
        return chapterVideos;
    }

    public void setChapterVideos(Set<ChapterVideo> chapterVideos) {
        this.chapterVideos = chapterVideos;
    }

    public Set<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Set<Playlist> playlists) {
        this.playlists = playlists;
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

    // Helper methods

    public void addChapter(Chapter chapter) {
        ChapterVideo chapterVideo = new ChapterVideo(chapter, this, 0); // Assuming order number is not relevant here
        chapterVideos.add(chapterVideo);
        chapter.getChapterVideos().add(chapterVideo);
    }

    public void removeChapter(Chapter chapter) {
        for (ChapterVideo chapterVideo : new HashSet<>(chapterVideos)) {
            if (chapterVideo.getChapter().equals(chapter)) {
                chapterVideos.remove(chapterVideo);
                chapter.getChapterVideos().remove(chapterVideo);
                chapterVideo.setChapter(null);
                chapterVideo.setVideo(null);
            }
        }
    }

    public void addChapterVideo(ChapterVideo chapterVideo) {
        chapterVideos.add(chapterVideo);
        chapterVideo.setVideo(this);
    }

    public void removeChapterVideo(ChapterVideo chapterVideo) {
        chapterVideos.remove(chapterVideo);
        chapterVideo.setVideo(null);
    }
    
    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
        playlist.getVideos().add(this);
    }

    public void removePlaylist(Playlist playlist) {
        playlists.remove(playlist);
        playlist.getVideos().remove(this);
    }
}
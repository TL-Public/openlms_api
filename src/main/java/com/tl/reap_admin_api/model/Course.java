package com.tl.reap_admin_api.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Entity
@Table(name = "courses")
@Inheritance(strategy = InheritanceType.JOINED)
public class Course {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(name = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "course_code", unique = true, nullable = false)
    private String courseCode;
    
    @Column(name = "display_course_code")
    private String displayCourseCode;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "category_id", nullable = false, columnDefinition = "integer default 0")
    private Integer category = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseTranslation> translations = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNumber ASC")
    private Set<Chapter> chapters = new HashSet<>();

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL)
    private Channel channel;

    @Column(name = "status", nullable = false, columnDefinition = "integer default 1")
    private Integer status = 1;
    // Constructors, getters, and setters

    public Course() {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

	public Set<CourseTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<CourseTranslation> translations) {
        this.translations = translations;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        if (channel != null) {
            channel.setCourse(this);
        }
    }
    

    public String getDisplayCourseCode() {
		return displayCourseCode;
	}

	public void setDisplayCourseCode(String displayCourseCode) {
		this.displayCourseCode = displayCourseCode;
	}

	public void addTranslation(CourseTranslation translation) {
        translations.add(translation);
        translation.setCourse(this);
    }

    public void removeTranslation(CourseTranslation translation) {
        translations.remove(translation);
        translation.setCourse(null);
    }

    public Set<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(Set<Chapter> chapters) {
        this.chapters = chapters;
    }    

    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
        chapter.setCourse(this);
    }

    public void removeChapter(Chapter chapter) {
        chapters.remove(chapter);
        chapter.setCourse(null);
    }

    @Override
    public String toString() {
        return "Course{" +
                "uuid=" + uuid +
                ", courseCode='" + courseCode + '\'' +
                ", displayCourseCode='" + displayCourseCode + '\'' +
                ", duration=" + duration +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", translations=" + translations.stream()
                        .map(t -> "Translation{ title='" + t.getTitle() + "'}")
                        .collect(Collectors.joining(", ", "[", "]")) +
                ", chapters=" + chapters.size() +
                '}';
    }
}



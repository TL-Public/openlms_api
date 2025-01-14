package com.tl.reap_admin_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "course_translations")
public class CourseTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_code")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "about_video_url")
    private String aboutVideoUrl;

    @Column(name = "about_video_extid")
    private String aboutVideoExtid;

    // Constructors, getters, and setters

    public CourseTranslation() {
    }

    public CourseTranslation(String title, String description, String aboutVideoUrl) {
        this.title = title;
        this.description = description;
        this.aboutVideoUrl = aboutVideoUrl;
    }

    // Getters and setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAboutVideoUrl() {
        return aboutVideoUrl;
    }

    public void setAboutVideoUrl(String aboutVideoUrl) {
        this.aboutVideoUrl = aboutVideoUrl;
    }

    public String getAboutVideoExtid() {
        return aboutVideoExtid;
    }

    public void setAboutVideoExtid(String aboutVideoExtid) {
        this.aboutVideoExtid = aboutVideoExtid;
    }
    public String getLanguageCode() {
        return language.getCode();
    }
}

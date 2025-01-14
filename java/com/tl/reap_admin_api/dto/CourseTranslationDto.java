package com.tl.reap_admin_api.dto;

public class CourseTranslationDto implements Cloneable<CourseTranslationDto>{
    private Long id;
    private String title;
    private String description;
    private String aboutVideoUrl;
    private String aboutVideoExtid;
    private String languageCode;

    // Constructors, getters, and setters

    public CourseTranslationDto() {
    }

    public CourseTranslationDto(Long id, String title, String description, String aboutVideoUrl, String languageCode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.aboutVideoUrl = aboutVideoUrl;
        this.languageCode = languageCode;
    }

    @Override
    public CourseTranslationDto deepClone() {
        CourseTranslationDto clone = new CourseTranslationDto();
        clone.setId(this.id);
        clone.setTitle(this.title);
        clone.setDescription(this.description);
        clone.setAboutVideoUrl(this.aboutVideoUrl);
        clone.setAboutVideoExtid(this.aboutVideoExtid);
        clone.setLanguageCode(this.languageCode);
        return clone;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
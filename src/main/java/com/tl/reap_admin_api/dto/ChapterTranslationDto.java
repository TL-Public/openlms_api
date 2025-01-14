package com.tl.reap_admin_api.dto;

public class ChapterTranslationDto  implements Cloneable<ChapterTranslationDto> {
    private Long id;
    private String title;
    private String description;
    private String languageCode;

    // Constructors, getters, and setters

    public ChapterTranslationDto() {
    }

    public ChapterTranslationDto(Long id, String title, String description, String languageCode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.languageCode = languageCode;
    }

    @Override
    public ChapterTranslationDto deepClone() {
        ChapterTranslationDto clone = new ChapterTranslationDto();
        clone.setLanguageCode(this.languageCode);
        clone.setTitle(this.title);
        clone.setDescription(this.description);
        return clone;
    }
    
    // Getters and setters for all fields

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

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
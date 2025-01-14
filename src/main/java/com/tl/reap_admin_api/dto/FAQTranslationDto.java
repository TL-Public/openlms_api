package com.tl.reap_admin_api.dto;

public class FAQTranslationDto {
    private Long id;
    private String languageCode;
    private String question;
    private String answer;

    // Constructors, getters, and setters

    public FAQTranslationDto() {
    }

    public FAQTranslationDto(Long id, String languageCode, String question, String answer) {
        this.id = id;
        this.languageCode = languageCode;
        this.question = question;
        this.answer = answer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
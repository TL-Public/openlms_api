package com.tl.reap_admin_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "faq_categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ext_id", "language_code"})
})
public class FAQCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ext_id", nullable = false)
    private Integer extId;

    @Column(nullable = false)
    private String category;

    @Column(name = "language_code", nullable = false)
    private String languageCode;
    
    public FAQCategory() {}

    public FAQCategory(String category, Integer extId, String languageCode) {
        this.category = category;
        this.extId = extId;
        this.languageCode = languageCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getExtId() {
        return extId;
    }

    public void setExtId(Integer extId) {
        this.extId = extId;
    }

    

    public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
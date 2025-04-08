package com.tl.reap_admin_api.model;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rsetis")
public class RSETI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "bank_id", nullable = false)
    private UUID bankId;

    @Column(name = "state_id", nullable = false)
    private Integer stateId;

    @Column(name = "ext_id", nullable = false)
    private String extId;

    @Column(nullable = false)
    private String email;

    @Column(name = "contact_no", nullable = false)
    private String contactNo;

    @Column(name = "director_contact_no", nullable = false)
    private String directorContactNo;

    @OneToMany(mappedBy = "rseti", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RsetiTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "rseti", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RsetiCourse> rsetiCourses = new ArrayList<>();
    
    @Column(name = "status", nullable = false, columnDefinition = "integer default 1")
    private Integer status = 1;
    
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_by",  updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    
    public RSETI() {
    	 this.createdAt = ZonedDateTime.now();
         this.updatedAt = ZonedDateTime.now();
    }
    // Getters and setters

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

    public UUID getBankId() {
        return bankId;
    }

    public void setBankId(UUID bankId) {
        this.bankId = bankId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getDirectorContactNo() {
        return directorContactNo;
    }

    public void setDirectorContactNo(String directorContactNo) {
        this.directorContactNo = directorContactNo;
    }

    public List<RsetiTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<RsetiTranslation> translations) {
        this.translations = translations;
    }

    public void addTranslation(RsetiTranslation translation) {
        translations.add(translation);
        translation.setRseti(this);
    }

    public void removeTranslation(RsetiTranslation translation) {
        translations.remove(translation);
        translation.setRseti(null);
    }

    public List<RsetiCourse> getRsetiCourses() {
        return rsetiCourses;
    }
    public void setRsetiCourses(List<RsetiCourse> rsetiCourses) {
        this.rsetiCourses = rsetiCourses;
    }
    public void addRsetiCourse(RsetiCourse rsetiCourse) {
        rsetiCourses.add(rsetiCourse);
        rsetiCourse.setRseti(this);
    }
    public void removeRsetiCourse(RsetiCourse rsetiCourse) {
        rsetiCourses.remove(rsetiCourse);
        rsetiCourse.setRseti(null);
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
    
	
    
}
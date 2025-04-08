package com.tl.reap_admin_api.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "trainee_rseti")
public class TraineeRseti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "enroll_id")
    private String enrollId;

    @Column(name = "enrolled_on")
    private LocalDate enrolledOn;

    @ManyToOne
    @JoinColumn(name = "rseti_id", nullable = false)
    private RSETI rseti;

    @ManyToOne
    @JoinColumn(name = "trainee_profile_id", nullable = false)
    private TraineeProfile traineeProfile;

    @ManyToOne
    @JoinColumn(name = "rseti_course_id")
    private RsetiCourse rsetiCourse;

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

    // Constructors, getters, and setters

    public TraineeRseti() {
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

    public String getEnrollId() {
        return enrollId;
    }

    public void setEnrollId(String enrollId) {
        this.enrollId = enrollId;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public void setEnrolledOn(LocalDate enrolledOn) {
        this.enrolledOn = enrolledOn;
    }

    public RSETI getRseti() {
        return rseti;
    }

    public void setRseti(RSETI rseti) {
        this.rseti = rseti;
    }

    public TraineeProfile getTraineeProfile() {
        return traineeProfile;
    }

    public void setTraineeProfile(TraineeProfile traineeProfile) {
        this.traineeProfile = traineeProfile;
    }

   

	public RsetiCourse getRsetiCourse() {
		return rsetiCourse;
	}

	public void setRsetiCourse(RsetiCourse rsetiCourse) {
		this.rsetiCourse = rsetiCourse;
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
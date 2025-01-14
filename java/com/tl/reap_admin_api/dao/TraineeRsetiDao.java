package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.TraineeProfile;
import com.tl.reap_admin_api.model.TraineeRseti;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TraineeRsetiDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<TraineeRseti> findByRsetiUuidAndCourseUuid(UUID rsetiUuid, UUID courseUuid) {
        TypedQuery<TraineeRseti> query = entityManager.createQuery(
                "SELECT tr FROM TraineeRseti tr WHERE tr.rseti.uuid = :rsetiUuid AND tr.course.uuid = :courseUuid", TraineeRseti.class);
        query.setParameter("rsetiUuid", rsetiUuid);
        query.setParameter("courseUuid", courseUuid);
        return query.getResultList();
    }

    public Optional<RSETI> findRsetiByUuid(UUID rsetiUuid) {
        TypedQuery<RSETI> query = entityManager.createQuery(
                "SELECT r FROM RSETI r WHERE r.uuid = :rsetiUuid", RSETI.class);
        query.setParameter("rsetiUuid", rsetiUuid);
        List<RSETI> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Course> findCourseByUuid(UUID courseUuid) {
        TypedQuery<Course> query = entityManager.createQuery(
                "SELECT c FROM Course c WHERE c.uuid = :courseUuid", Course.class);
        query.setParameter("courseUuid", courseUuid);
        List<Course> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<TraineeProfile> findTraineeProfileByUuid(UUID traineeProfileUuid) {
        TypedQuery<TraineeProfile> query = entityManager.createQuery(
                "SELECT tp FROM TraineeProfile tp WHERE tp.uuid = :traineeProfileUuid", TraineeProfile.class);
        query.setParameter("traineeProfileUuid", traineeProfileUuid);
        List<TraineeProfile> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public TraineeRseti save(TraineeRseti traineeRseti) {
        if (traineeRseti.getId() == null) {
            entityManager.persist(traineeRseti);
        } else {
            traineeRseti = entityManager.merge(traineeRseti);
        }
        return traineeRseti;
    }

    public Optional<TraineeRseti> findByRsetiUuidAndCourseUuidAndTraineeUuid(UUID rsetiUuid, UUID courseUuid, UUID traineeUuid) {
        TypedQuery<TraineeRseti> query = entityManager.createQuery(
                "SELECT tr FROM TraineeRseti tr WHERE tr.rseti.uuid = :rsetiUuid AND tr.course.uuid = :courseUuid AND tr.traineeProfile.uuid = :traineeUuid", TraineeRseti.class);
        query.setParameter("rsetiUuid", rsetiUuid);
        query.setParameter("courseUuid", courseUuid);
        query.setParameter("traineeUuid", traineeUuid);
        List<TraineeRseti> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public void delete(TraineeRseti traineeRseti) {
        entityManager.remove(traineeRseti);
    }

    public TraineeRseti findByRsetiCourseUuidAndTraineeProfileUuid(UUID rsetiCourseUuid, UUID traineeUuid) {
        try {
            return entityManager.createQuery(
                "SELECT tr FROM TraineeRseti tr WHERE tr.rsetiCourse.uuid = :rsetiCourseUuid AND tr.traineeProfile.uuid = :traineeUuid", TraineeRseti.class)
                .setParameter("rsetiCourseUuid", rsetiCourseUuid)
                .setParameter("traineeUuid", traineeUuid)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TraineeRseti> findByRsetiCourseUuid(UUID rsetiCourseUuid) {
        return entityManager.createQuery(
            "SELECT tr FROM TraineeRseti tr WHERE tr.rsetiCourse.uuid = :rsetiCourseUuid", TraineeRseti.class)
            .setParameter("rsetiCourseUuid", rsetiCourseUuid)
            .getResultList();
    }
}
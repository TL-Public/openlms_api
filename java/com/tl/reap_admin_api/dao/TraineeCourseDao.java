package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.TraineeCourse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class TraineeCourseDao {

    @PersistenceContext
    private EntityManager entityManager;

    public TraineeCourse save(TraineeCourse traineeCourse) {
        if (traineeCourse.getId() == null) {
            entityManager.persist(traineeCourse);
        } else {
            traineeCourse = entityManager.merge(traineeCourse);
        }
        return traineeCourse;
    }

    public TraineeCourse findByCourseUuidAndTraineeUuid(UUID courseUuid, UUID traineeUuid) {
        TypedQuery<TraineeCourse> query = entityManager.createQuery(
            "SELECT tc FROM TraineeCourse tc WHERE tc.course.uuid = :courseUuid AND tc.trainee.uuid = :traineeUuid", 
            TraineeCourse.class);
        query.setParameter("courseUuid", courseUuid);
        query.setParameter("traineeUuid", traineeUuid);
        List<TraineeCourse> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<TraineeCourse> findByCourseUuid(UUID courseUuid) {
        TypedQuery<TraineeCourse> query = entityManager.createQuery(
            "SELECT tc FROM TraineeCourse tc WHERE tc.course.uuid = :courseUuid", 
            TraineeCourse.class);
        query.setParameter("courseUuid", courseUuid);
        return query.getResultList();
    }

    public void delete(TraineeCourse traineeCourse) {
        entityManager.remove(entityManager.contains(traineeCourse) ? traineeCourse : entityManager.merge(traineeCourse));
    }
}


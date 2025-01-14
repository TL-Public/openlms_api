package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.Chapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ChapterDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Chapter> findAll() {
        TypedQuery<Chapter> query = entityManager.createQuery("SELECT c FROM Chapter c", Chapter.class);
        return query.getResultList();
    }

    public Optional<Chapter> findById(Long id) {
        TypedQuery<Chapter> query = entityManager.createQuery(
            "SELECT c FROM Chapter c WHERE c.id = :id",
            Chapter.class
        );
        query.setParameter("id", id);
        List<Chapter> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Chapter> findByUuid(UUID uuid) {
        TypedQuery<Chapter> query = entityManager.createQuery(
            "SELECT c FROM Chapter c WHERE c.uuid = :uuid",
            Chapter.class
        );
        query.setParameter("uuid", uuid);
        List<Chapter> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    public List<Chapter> findByCourseId(UUID courseId) {
        TypedQuery<Chapter> query = entityManager.createQuery(
            "SELECT c FROM Chapter c WHERE c.course.id = :courseId ORDER BY c.orderNumber",
            Chapter.class
        );
        query.setParameter("courseId", courseId);
        return query.getResultList();
    }

    public Chapter save(Chapter chapter) {
        if (chapter.getId() == null) {
            entityManager.persist(chapter);
        } else {
            chapter = entityManager.merge(chapter);
        }
        return chapter;
    }

    public void delete(Chapter chapter) {
        entityManager.remove(entityManager.contains(chapter) ? chapter : entityManager.merge(chapter));
    }
}
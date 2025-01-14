package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.Video;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import jakarta.persistence.criteria.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VideoDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Video save(Video video) {
        if (video.getId() == null) {
            entityManager.persist(video);
        } else {           
            video = entityManager.merge(video);
        }
        return video;
    }

    public Optional<Video> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Video.class, id));
    }

    public Optional<Video> findByUuid(UUID uuid) {
        TypedQuery<Video> query = entityManager.createQuery(
            "SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.chapterVideos cv " +
            "LEFT JOIN FETCH cv.chapter c LEFT JOIN FETCH c.course WHERE v.uuid = :uuid", Video.class);
        query.setParameter("uuid", uuid);
        List<Video> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Video> findByUrl(String url) {
        TypedQuery<Video> query = entityManager.createQuery(
           "SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.chapterVideos cv " +
            "LEFT JOIN FETCH cv.chapter c LEFT JOIN FETCH c.course WHERE v.url = :url", Video.class);
        query.setParameter("url", url);
        List<Video> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    public List<Video> findAll() {
        TypedQuery<Video> query = entityManager.createQuery(
            "SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.chapterVideos cv " +
            "LEFT JOIN FETCH cv.chapter c LEFT JOIN FETCH c.course", Video.class);
        return query.getResultList();
    }

     public Page<Video> findAllPaged(Pageable pageable) {
        TypedQuery<Video> query = entityManager.createQuery(
            "SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.chapterVideos cv " +
            "LEFT JOIN FETCH cv.chapter c LEFT JOIN FETCH c.course", Video.class);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Video> videos = query.getResultList();

        TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(DISTINCT v) FROM Video v", Long.class);
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(videos, pageable, total);
    }

    public List<Video> findAllFiltered(String courseName, String courseCode, String videoTitle, UUID courseUuid, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Video> query = cb.createQuery(Video.class);
        Root<Video> video = query.from(Video.class);

        Predicate orPredicate = createPredicates(cb, query, video, courseName, courseCode, videoTitle, courseUuid);

        query.where(orPredicate);
        query.select(video).distinct(true);

        TypedQuery<Video> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        return typedQuery.getResultList();
    }

    public Long countAllFiltered(String courseName, String courseCode, String videoTitle, UUID courseUuid) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Video> countRoot = countQuery.from(Video.class);

        Predicate orPredicate = createPredicates(cb, countQuery, countRoot, courseName, courseCode, videoTitle, courseUuid);

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(orPredicate);

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Predicate createPredicates(CriteriaBuilder cb, CriteriaQuery<?> query, Root<Video> video, 
                                       String courseName, String courseCode, String videoTitle, UUID courseUuid) {
        List<Predicate> orPredicates = new ArrayList<>();
        List<Predicate> andPredicates = new ArrayList<>();

        if (courseName != null && !courseName.isEmpty()) {
            Subquery<Long> courseNameSubquery = query.subquery(Long.class);
            Root<Video> subVideo = courseNameSubquery.from(Video.class);
            Join<Object, Object> chapterVideos = subVideo.join("chapterVideos");
            Join<Object, Object> chapter = chapterVideos.join("chapter");
            Join<Object, Object> course = chapter.join("course");
            Join<Object, Object> courseTranslations = course.join("translations");
            
            courseNameSubquery.select(subVideo.get("id"))
                .where(cb.like(cb.lower(courseTranslations.get("title")), "%" + courseName.toLowerCase() + "%"));
                
            orPredicates.add(cb.in(video.get("id")).value(courseNameSubquery));
        }

        if (courseCode != null && !courseCode.isEmpty()) {
            Subquery<Long> courseCodeSubquery = query.subquery(Long.class);
            Root<Video> subVideo = courseCodeSubquery.from(Video.class);
            courseCodeSubquery.select(subVideo.get("id"))
                .where(cb.equal(subVideo.join("chapterVideos").join("chapter").join("course").get("courseCode"), courseCode));
            orPredicates.add(cb.in(video.get("id")).value(courseCodeSubquery));
        }

        if (videoTitle != null && !videoTitle.isEmpty()) {
            orPredicates.add(cb.like(cb.lower(video.get("name")), "%" + videoTitle.toLowerCase() + "%"));
        }

        // If no predicates, return a predicate that always evaluates to true
        if (orPredicates.isEmpty()) {
            return cb.isNotNull(video.get("id"));
        }
    
        // Add courseUUID predicate
        if (courseUuid != null) {
            Subquery<Long> courseUuidSubquery = query.subquery(Long.class);
            Root<Video> subVideo = courseUuidSubquery.from(Video.class);
            courseUuidSubquery.select(subVideo.get("id"))
                .where(cb.equal(subVideo.join("chapterVideos").join("chapter").join("course").get("uuid"), courseUuid));
            andPredicates.add(cb.in(video.get("id")).value(courseUuidSubquery));
        }

         // Combine OR predicates
         Predicate orPredicate = cb.or(orPredicates.toArray(new Predicate[0]));

        // If there are OR predicates, add them to AND predicates
        if (!orPredicates.isEmpty()) {
            andPredicates.add(orPredicate);
        }

        // If no predicates at all, return a predicate that always evaluates to true
        if (andPredicates.isEmpty()) {
            return cb.isNotNull(video.get("id"));
        }

        // Combine all AND predicates
        return cb.and(andPredicates.toArray(new Predicate[0]));
    }

    public void delete(Video video) {
        entityManager.remove(entityManager.contains(video) ? video : entityManager.merge(video));
    }
}

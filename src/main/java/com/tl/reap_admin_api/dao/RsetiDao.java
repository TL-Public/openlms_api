package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.RsetiTranslation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RsetiDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<RSETI> findAll() {
        TypedQuery<RSETI> query = entityManager.createQuery("SELECT r FROM RSETI r LEFT JOIN FETCH r.translations", RSETI.class);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public List<RSETI> findAllByLanguageCode(Long languageId) {
        TypedQuery<RSETI> query = entityManager.createQuery(
            "SELECT DISTINCT r FROM RSETI r LEFT JOIN r.translations rt where rt.language.id = :languageId" , 
            RSETI.class);
            query.setParameter("languageId", languageId);
        return query.getResultList();
    
    }

    @Transactional(readOnly = true)
    public Optional<RSETI> findByUuid(UUID uuid) {
        TypedQuery<RSETI> query = entityManager.createQuery(
            "SELECT r FROM RSETI r LEFT JOIN FETCH r.translations WHERE r.uuid = :uuid", RSETI.class);
        query.setParameter("uuid", uuid);
        List<RSETI> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<RsetiTranslation> findAllTranslationsByLanguageId(Long languageId) {
        TypedQuery<RsetiTranslation> query = entityManager.createQuery(
            "SELECT DISTINCT rt FROM RsetiTranslation rt where rt.language.id = :languageId" , 
            RsetiTranslation.class);
            query.setParameter("languageId", languageId);
        return query.getResultList();
    
    }
    @Transactional
    public RSETI save(RSETI rseti) {
        if (rseti.getUuid() == null) {
            rseti.setUuid(UUID.randomUUID());
            entityManager.persist(rseti);
        } else {
            rseti = entityManager.merge(rseti);
        }
        return rseti;
    }

    @Transactional
    public void deleteByUuid(UUID uuid) {
        RSETI rseti = findByUuid(uuid).orElse(null);
        if (rseti != null) {
            entityManager.remove(rseti);
        }
    }
    
    @Transactional(readOnly = true)
    public List<RSETI> findByStateId(UUID stateId) {
        TypedQuery<RSETI> query = entityManager.createQuery(
            "SELECT r FROM RSETI r LEFT JOIN FETCH r.translations WHERE r.stateId = :stateId", RSETI.class);
        query.setParameter("stateId", stateId);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public List<Object[]> findAllWithCourseCount() {
        String jpql = "SELECT r, COUNT(rc), rt " +
                      "FROM RSETI r " +
                      "LEFT JOIN r.rsetiCourses rc " +
                      "LEFT JOIN r.translations rt " +
                      "GROUP BY r, rt";
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        
        return query.getResultList();
    }

    private final JdbcTemplate jdbcTemplate;

    public RsetiDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Long> findAllRsetiIdsByName() {
        String sql = "SELECT rt.name, r.id FROM rsetis r JOIN rseti_translations rt ON r.id = rt.rseti_id WHERE rt.language_id = 1";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{rs.getString("name"), rs.getLong("id")})
                .stream()
                .collect(Collectors.toMap(arr -> (String) arr[0], arr -> (Long) arr[1]));
    }
    
    public boolean existsByExtId(String extId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(r) FROM RSETI r WHERE r.extId = :extId", Long.class);
        query.setParameter("extId", extId);
        return query.getSingleResult() > 0;
    }
}
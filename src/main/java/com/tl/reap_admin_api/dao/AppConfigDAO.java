package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.AppConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AppConfigDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<AppConfig> findAll() {
        TypedQuery<AppConfig> query = entityManager.createQuery(
            "SELECT a FROM AppConfig a", AppConfig.class);
        return query.getResultList();
    }

    public Optional<AppConfig> findByKey(String key) {
        AppConfig appConfig = entityManager.find(AppConfig.class, key);
        return Optional.ofNullable(appConfig);
    }

    public AppConfig save(AppConfig appConfig) {
        if (entityManager.find(AppConfig.class, appConfig.getKey()) == null) {
            entityManager.persist(appConfig);
        } else {
            appConfig = entityManager.merge(appConfig);
        }
        return appConfig;
    }

    public void delete(String key) {
        AppConfig appConfig = entityManager.find(AppConfig.class, key);
        if (appConfig != null) {
            entityManager.remove(appConfig);
        }
    }

    public boolean existsByKey(String key) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM AppConfig a WHERE a.key = :key", Long.class);
        query.setParameter("key", key);
        return query.getSingleResult() > 0;
    }
    
    public List<AppConfig> findByKeyContaining(String keyPattern) {
        TypedQuery<AppConfig> query = entityManager.createQuery(
            "SELECT a FROM AppConfig a WHERE a.key LIKE :pattern", AppConfig.class);
        query.setParameter("pattern", "%" + keyPattern + "%");
        return query.getResultList();
    }
}
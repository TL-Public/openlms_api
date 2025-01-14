package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.Category;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CategoryDao {
    
    @PersistenceContext
    private EntityManager entityManager;
  
    public Category save(Category category) {
        if (category.getId() == null) {
            entityManager.persist(category);
        } else {
            category = entityManager.merge(category);
        }
        return category;
    }

    public Optional<Category> findByExtIdAndLanguageCode(Integer extId, String languageCode) {
        List<Category> results = entityManager.createQuery(
            "SELECT c FROM Category c WHERE c.extId = :extId AND c.languageCode = :languageCode", 
            Category.class
        )
        .setParameter("extId", extId)
        .setParameter("languageCode", languageCode)
        .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Category> findAll() {
        return entityManager.createQuery("SELECT c FROM Category c ORDER BY c.extId, c.languageCode", Category.class)
            .getResultList();
    }

    public Optional<Category> findByNameAndLanguageCode(String name, String languageCode) {
        List<Category> results = entityManager.createQuery(
            "SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.languageCode = :languageCode", 
            Category.class
        )
        .setParameter("name", name.toLowerCase())
        .setParameter("languageCode", languageCode)
        .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public int deleteByExtId(Integer extId) {
        return entityManager.createQuery("DELETE FROM Category c WHERE c.extId = :extId")
            .setParameter("extId", extId)
            .executeUpdate();
    }
    
    public List<Category> findAllByExtId(Integer extId) {
        return entityManager.createQuery(
            "SELECT c FROM Category c WHERE c.extId = :extId ORDER BY c.languageCode", 
            Category.class
        )
        .setParameter("extId", extId)
        .getResultList();
    }

    public Integer getMaxExtId() {
        return entityManager.createQuery("SELECT MAX(c.extId) FROM Category c", Integer.class)
            .getSingleResult();
    }

    public Optional<Category> findByName(String name) {
        List<Category> results = entityManager.createQuery(
            "SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)", 
            Category.class
        )
        .setParameter("name", name.toLowerCase())
        .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}


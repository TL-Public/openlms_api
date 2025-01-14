package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.FAQ;
import com.tl.reap_admin_api.model.FAQCategory;
import com.tl.reap_admin_api.model.Language;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FAQDao {

    @PersistenceContext
    private EntityManager entityManager;

    public FAQ save(FAQ faq) {
        if (faq.getId() == null) {
            entityManager.persist(faq);
        } else {
            faq = entityManager.merge(faq);
        }
        return faq;
    }

    public Optional<FAQ> findByUuid(UUID uuid) {
        TypedQuery<FAQ> query = entityManager.createQuery(
            "SELECT f FROM FAQ f LEFT JOIN FETCH f.translations WHERE f.uuid = :uuid", FAQ.class);
        query.setParameter("uuid", uuid);
        List<FAQ> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<FAQ> findAll() {
        TypedQuery<FAQ> query = entityManager.createQuery(
            "SELECT DISTINCT f FROM FAQ f LEFT JOIN FETCH f.translations ORDER BY f.category.id", FAQ.class);
        return query.getResultList();
    }

    public void delete(FAQ faq) {
        entityManager.remove(entityManager.contains(faq) ? faq : entityManager.merge(faq));
    }

    public void deleteByUuid(UUID uuid) {
        findByUuid(uuid).ifPresent(this::delete);
    }

    public Optional<FAQCategory> findCategoryById(Long id) {
        return Optional.ofNullable(entityManager.find(FAQCategory.class, id));
    }

  
    public Optional<Language> findLanguageByCode(String code) {
        TypedQuery<Language> query = entityManager.createQuery(
            "SELECT l FROM Language l WHERE l.code = :code", Language.class);
        query.setParameter("code", code);
        List<Language> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<FAQ> findByCategoryId(Long categoryId) {
        TypedQuery<FAQ> query = entityManager.createQuery(
            "SELECT DISTINCT f FROM FAQ f LEFT JOIN FETCH f.translations t LEFT JOIN FETCH t.language WHERE f.category.id = :categoryId",
            FAQ.class
        );
        query.setParameter("categoryId", categoryId);
        return query.getResultList();
    }
}
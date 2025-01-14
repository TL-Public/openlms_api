package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.Language;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class LanguageDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Language> findAll() {
        TypedQuery<Language> query = entityManager.createQuery("SELECT l FROM Language l", Language.class);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Optional<Language> findById(Long id) {
        Language language = entityManager.find(Language.class, id);
        return Optional.ofNullable(language);
    }

  

    public Optional<Language> findByCode(String code) {
        TypedQuery<Language> query = entityManager.createQuery("SELECT l FROM Language l WHERE l.code = :code", Language.class);

        query.setParameter("code", code);
        List<Language> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Transactional


    public Language save(Language language) {
        if (language.getId() == null) {
            entityManager.persist(language);
        } else {
            language = entityManager.merge(language);
        }
        return language;
    }

    @Transactional
    public void delete(Language language) {
        entityManager.remove(entityManager.contains(language) ? language : entityManager.merge(language));
    }
}
package com.tl.reap_admin_api.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tl.reap_admin_api.model.FAQCategory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.persistence.NoResultException;

@Repository
@Transactional
public class FAQCategoryDao {
    
    @PersistenceContext
    private EntityManager entityManager;
  
    public FAQCategory save(FAQCategory faqCategory) {
        if (faqCategory.getExtId() == null) {
            Integer maxExtId = getMaxExtId();
            faqCategory.setExtId(maxExtId == null ? 1 : maxExtId + 1);
        }
        
        if (faqCategory.getId() == null) {
            entityManager.persist(faqCategory);
        } else {
            faqCategory = entityManager.merge(faqCategory);
        }
        
        return faqCategory;
    }

    public FAQCategory update(FAQCategory faqCategory) {
        return entityManager.merge(faqCategory);
    }

    public Optional<FAQCategory> findByExtIdAndLanguageCode(Integer extId, String languageCode) {
        try {
            FAQCategory faqCategory = entityManager.createQuery("SELECT f FROM FAQCategory f WHERE f.extId = :extId AND f.languageCode = :languageCode", FAQCategory.class)
                    .setParameter("extId", extId)
                    .setParameter("languageCode", languageCode)
                    .getSingleResult();
            return Optional.of(faqCategory);
        } catch (NoResultException e) {
            return Optional.empty(); 
        }
    }

    public List<FAQCategory>  findByExtId(Integer extId) {
        try {
            List<FAQCategory> faqCategoryList= entityManager.createQuery("SELECT f FROM FAQCategory f WHERE f.extId = :extId", FAQCategory.class)
                    .setParameter("extId", extId).getResultList();
            return faqCategoryList;
        } catch (NoResultException e) {
            return null; 
        }
    }


    public List<FAQCategory> findAll() {
        return entityManager.createQuery("SELECT f FROM FAQCategory f ORDER BY f.extId, f.languageCode", FAQCategory.class).getResultList();
    }

    public Integer getMaxExtId() {
        try {
            return entityManager.createQuery("SELECT MAX(f.extId) FROM FAQCategory f", Integer.class)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Optional<FAQCategory> findByNameAndLanguageCode(String category, String languageCode) {
        try {
            FAQCategory faqCategory = entityManager.createQuery("SELECT f FROM FAQCategory f WHERE LOWER(f.category) = LOWER(:category) AND f.languageCode = :languageCode", FAQCategory.class)
                    .setParameter("category", category.toLowerCase())
                    .setParameter("languageCode", languageCode)
                    .getSingleResult();
            return Optional.of(faqCategory);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<FAQCategory> findByName(String category) {
        System.out.println("category --" + category);
        return entityManager.createQuery("SELECT f FROM FAQCategory f WHERE LOWER(f.category) = LOWER(:category)", FAQCategory.class)
                .setParameter("category", category.toLowerCase())
                .getResultList();
    }

    public int deleteByExtId(String extId) {
        return entityManager.createQuery("DELETE FROM FAQCategory f WHERE f.extId = :extId")
                .setParameter("extId", extId)
                .executeUpdate(); 
    }
    
    public List<FAQCategory> findAll(String extId) {
        String query = "SELECT f FROM FAQCategory f WHERE f.extId = :extId ORDER BY f.extId, f.languageCode";
        return entityManager.createQuery(query, FAQCategory.class)
                            .setParameter("extId", extId)  
                            .getResultList();
    }

}

package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.District;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class DistrictDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // Get the maximum extId
    public Integer getMaxExtId() {
        try {
            return entityManager.createQuery("SELECT MAX(d.extId) FROM District d", Integer.class)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    // Save a district
    public District save(District district) {
        if (district.getId() == null) {
            entityManager.persist(district);
        } else {
            district = entityManager.merge(district);
        }
        return district;
    }
}
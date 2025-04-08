package com.tl.reap_admin_api.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tl.reap_admin_api.model.State;

import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional
public class StateDao {
    
    @PersistenceContext
    private EntityManager entityManager;
  
 // Save a new State or update an existing one
    public State save(State state) {
        if (state.getExtId() == null) {
            Integer maxExtId = getMaxExtId();
            state.setExtId(maxExtId == null ? 1 : maxExtId + 1);
        }
        
        if (state.getId() == null) {
            entityManager.persist(state);
        } else {
            state = entityManager.merge(state);
        }
        
        return state;
    }

    // Update an existing State
    public State update(State state) {
        return entityManager.merge(state);
    }

    // Find a State by extId and languageCode
    public Optional<State> findByExtIdAndLanguageCode(Integer extId, String languageCode) {
        try {
            State state = entityManager.createQuery("SELECT s FROM State s WHERE s.extId = :extId AND s.languageCode = :languageCode", State.class)
                    .setParameter("extId", extId)
                    .setParameter("languageCode", languageCode)
                    .getSingleResult();
            return Optional.of(state);
        } catch (NoResultException e) {
            return Optional.empty(); 
        }
    }

    public List<State> findAllByLanguageCode(String languageCode) {
        List<State> states = new ArrayList<>();
        try {
             states = entityManager.createQuery("SELECT s FROM State s WHERE s.languageCode = :languageCode", State.class)
                    .setParameter("languageCode", languageCode)
                    .getResultList();
            return states;
        } catch (NoResultException e) {
            return null; 
        }
    }
    // Find all States
    public List<State> findAll() {
        return entityManager.createQuery("SELECT s FROM State s ORDER BY s.extId, s.languageCode", State.class).getResultList();
    }


    // Get the maximum extId
    public Integer getMaxExtId() {
        try {
            return entityManager.createQuery("SELECT MAX(s.extId) FROM State s", Integer.class)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // Find a State by name and languageCode
    public Optional<State> findByNameAndLanguageCode(String name, String languageCode) {
        try {
            State state = entityManager.createQuery("SELECT s FROM State s WHERE LOWER(s.name) = LOWER(:name) AND s.languageCode = :languageCode", State.class)
                    .setParameter("name", name.toLowerCase())
                    .setParameter("languageCode", languageCode)
                    .getSingleResult();
            return Optional.of(state);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Find States by name
    public List<State> findByName(String name) {
        return entityManager.createQuery("SELECT s FROM State s WHERE LOWER(s.name) = LOWER(:name)", State.class)
                .setParameter("name", name.toLowerCase())
                .getResultList();
    }

    public List<State> findByExtId(Integer extId) {
        return entityManager.createQuery("SELECT s FROM State s WHERE s.extId = :extId ORDER BY s.languageCode", State.class)
                .setParameter("extId", extId)
                .getResultList();
    }

    public int deleteByExtId(String extId) {
        return entityManager.createQuery("DELETE FROM State s WHERE s.extId = :extId")
                .setParameter("extId", extId)
                .executeUpdate(); 
    }
    
    
 // Find all States with Districts
    public List<State> findAllWithDistricts() {
        return entityManager.createQuery(
            "SELECT DISTINCT s FROM State s LEFT JOIN FETCH s.districts d ORDER BY s.extId, s.languageCode", 
            State.class
        ).getResultList();
    }

    // Find a State by extId and languageCode with Districts
    public Optional<State> findByExtIdAndLanguageCodeWithDistricts(Integer extId, String languageCode) {
        try {
            State state = entityManager.createQuery(
                "SELECT DISTINCT s FROM State s LEFT JOIN FETCH s.districts WHERE s.extId = :extId AND s.languageCode = :languageCode", 
                State.class
            )
            .setParameter("extId", extId)
            .setParameter("languageCode", languageCode)
            .getSingleResult();
            return Optional.of(state);
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    // Find States by extId with Districts
    public List<State> findByExtIdWithDistricts(Integer extId) {
        return entityManager.createQuery(
            "SELECT DISTINCT s FROM State s LEFT JOIN FETCH s.districts WHERE s.extId = :extId ORDER BY s.languageCode", 
            State.class
        )
        .setParameter("extId", extId)
        .getResultList();
    }

	 public Optional<State> findByExtId(String extId) {
        try {
            State state = entityManager.createQuery(
                    "SELECT s FROM State s WHERE s.extId = :extId", State.class)
                    .setParameter("extId", extId)
                    .getSingleResult();
            return Optional.of(state);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }




}
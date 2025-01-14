package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.TraineeCredential;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TraineeCredentialDao {

    @PersistenceContext
    private EntityManager entityManager;

    public TraineeCredential save(TraineeCredential trainee) {
        if (trainee.getId() == null) {
            entityManager.persist(trainee);
        } else {
            trainee = entityManager.merge(trainee);
        }
        return trainee;
    }

    public Optional<TraineeCredential> findByUuid(UUID uuid) {
        TypedQuery<TraineeCredential> query = entityManager.createQuery(
            "SELECT t FROM TraineeCredential t WHERE t.uuid = :uuid", TraineeCredential.class);
        query.setParameter("uuid", uuid);
        List<TraineeCredential> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<TraineeCredential> findByUsername(String username) {
        TypedQuery<TraineeCredential> query = entityManager.createQuery(
            "SELECT t FROM TraineeCredential t WHERE t.username = :username", TraineeCredential.class);
        query.setParameter("username", username);
        List<TraineeCredential> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<TraineeCredential> findAll() {
        TypedQuery<TraineeCredential> query = entityManager.createQuery("SELECT t FROM TraineeCredential t", TraineeCredential.class);
        return query.getResultList();
    }

    public void delete(TraineeCredential trainee) {
        entityManager.remove(entityManager.contains(trainee) ? trainee : entityManager.merge(trainee));
    }
    
    
    public Optional<TraineeCredential> findByEmail(String email) {
        TypedQuery<TraineeCredential> query = entityManager.createQuery(
            "SELECT t FROM TraineeCredential t WHERE t.email = :email", TraineeCredential.class);
        query.setParameter("email", email);
        List<TraineeCredential> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

}
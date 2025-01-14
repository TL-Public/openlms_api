package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.User;
import com.tl.reap_admin_api.model.UserProfile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class UserProfileDao {
    @PersistenceContext
    private EntityManager entityManager;

    public UserProfile save(UserProfile userProfile) {
        if (userProfile.getId() == null) {
            entityManager.persist(userProfile);
        } else {
            userProfile = entityManager.merge(userProfile);
        }
        return userProfile;
    }

    public Optional<UserProfile> findById(Long id) {
        return Optional.ofNullable(entityManager.find(UserProfile.class, id));
    }

    public Optional<UserProfile> findByUuid(UUID uuid) {
        TypedQuery<UserProfile> query = entityManager.createQuery(
            "SELECT up FROM UserProfile up WHERE up.uuid = :uuid", UserProfile.class);
        query.setParameter("uuid", uuid);
        List<UserProfile> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<UserProfile> findByUser(User user) {
        TypedQuery<UserProfile> query = entityManager.createQuery(
            "SELECT up FROM UserProfile up WHERE up.user = :user", UserProfile.class);
        query.setParameter("user", user);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
 
    public List<UserProfile> findAll() {
        return entityManager.createQuery("SELECT up FROM UserProfile up", UserProfile.class).getResultList();
    }


    public void delete(UserProfile userProfile) {
        entityManager.remove(entityManager.contains(userProfile) ? userProfile : entityManager.merge(userProfile));
    }
}

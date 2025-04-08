package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.Role;
import com.tl.reap_admin_api.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            user = entityManager.merge(user);
        }
        return user;
    }

    public Optional<User> findByUuid(UUID uuid) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.uuid = :uuid", User.class);
        query.setParameter("uuid", uuid);
        List<User> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> findByUsername(String username) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        List<User> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", email);
        List<User> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u", User.class);
        return query.getResultList();
    }

    public void delete(User user) {
        entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
    }

    public boolean existsByUsername(String username) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
        query.setParameter("username", username);
        return query.getSingleResult() > 0;
    }

    public boolean existsByEmail(String email) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
        query.setParameter("email", email);
        return query.getSingleResult() > 0;
    }
    
    public List<User> findAllWithProfiles() {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userProfile where u.role!='SUPER_ADMIN'", User.class);
        return query.getResultList();
    }

    public List<User> findAllWithProfilesByStateId(Integer stateId) {
        String queryStr = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userProfile" +
                        " where (u.role NOT IN ('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')) AND" +
                        "  u.userProfile.state.extId = :stateId";
        TypedQuery<User> query = entityManager.createQuery(queryStr, User.class);
        query.setParameter("stateId", stateId);           
        return query.getResultList();
    }

    public List<User> findAllWithProfilesByRseti(UUID rsetiId) {
        String queryStr = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userProfile" +
                        " where (u.role NOT IN ('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF')) AND" +
                        "  u.userProfile.rseti.uuid = :rsetiId";
        TypedQuery<User> query = entityManager.createQuery(queryStr, User.class);
        query.setParameter("rsetiId", rsetiId);           
        return query.getResultList();
    }
    
    public List<User> findUsersWithLowerRoles(Role role, Integer stateId, UUID rsetiId, UUID userId) {
     
        String queryStr = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userProfile WHERE u.roleId > :role" + 
                            " AND u.uuid != :userId order by u.roleId ASC";
        
        if (stateId != null) {
            queryStr += " AND u.userProfile.state.extId = :stateId";
        }
        
        if (rsetiId != null) {
            queryStr += " AND u.userProfile.rseti.uuid = :rsetiId";
        }
        
        TypedQuery<User> query = entityManager.createQuery(queryStr, User.class);
        query.setParameter("role", role.getNumber());
        query.setParameter("userId", userId);
        
        if (stateId != null) {
            query.setParameter("stateId", stateId);
        }
        
        if (rsetiId != null) {
            query.setParameter("rsetiId", rsetiId);
        }
        
        return query.getResultList();
    }

}
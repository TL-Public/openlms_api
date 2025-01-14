package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.Playlist;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PlaylistDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Playlist save(Playlist playlist) {
        if (playlist.getId() == null) {
            entityManager.persist(playlist);
        } else {
            playlist = entityManager.merge(playlist);
        }
        return playlist;
    }

    public Optional<Playlist> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Playlist.class, id));
    }

    public Optional<Playlist> findByUuid(UUID uuid) {
        TypedQuery<Playlist> query = entityManager.createQuery(
            "SELECT p FROM Playlist p WHERE p.uuid = :uuid", Playlist.class);
        query.setParameter("uuid", uuid);
        List<Playlist> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Playlist> findAll() {
        TypedQuery<Playlist> query = entityManager.createQuery("SELECT p FROM Playlist p", Playlist.class);
        return query.getResultList();
    }

    public void delete(Playlist playlist) {
        entityManager.remove(entityManager.contains(playlist) ? playlist : entityManager.merge(playlist));
    }
}
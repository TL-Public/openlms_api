package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.Channel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ChannelDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Channel save(Channel channel) {
        if (channel.getId() == null) {
            entityManager.persist(channel);
        } else {
            channel = entityManager.merge(channel);
        }
        return channel;
    }

    public Optional<Channel> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Channel.class, id));
    }

    public Optional<Channel> findByUuid(UUID uuid) {
        TypedQuery<Channel> query = entityManager.createQuery(
            "SELECT c FROM Channel c WHERE c.uuid = :uuid", Channel.class);
        query.setParameter("uuid", uuid);
        List<Channel> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Channel> findAll() {
        TypedQuery<Channel> query = entityManager.createQuery("SELECT c FROM Channel c", Channel.class);
        return query.getResultList();
    }

    public void delete(Channel channel) {
        entityManager.remove(entityManager.contains(channel) ? channel : entityManager.merge(channel));
    }
}

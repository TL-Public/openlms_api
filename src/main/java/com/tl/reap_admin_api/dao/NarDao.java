package com.tl.reap_admin_api.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tl.reap_admin_api.model.Nar;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
public class NarDao {
	@PersistenceContext
	private EntityManager entityManager;

	public Nar save(Nar nar) {
		if (nar.getId() == null) {
			entityManager.persist(nar);
		} else {
			nar = entityManager.merge(nar);
		}
		return nar;
	}

	public Optional<Nar> findByUuid(String uuid) {
		TypedQuery<Nar> query = entityManager.createQuery("SELECT n FROM Nar n WHERE n.uuid = :uuid", Nar.class);
		query.setParameter("uuid", uuid);
		List<Nar> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	public List<Nar> findAll() {
		TypedQuery<Nar> query = entityManager.createQuery("SELECT n FROM Nar n", Nar.class);
		return query.getResultList();
	}

	public void delete(Nar nar) {
		entityManager.remove(entityManager.contains(nar) ? nar : entityManager.merge(nar));
	}
}

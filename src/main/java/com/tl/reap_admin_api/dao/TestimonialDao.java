package com.tl.reap_admin_api.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.model.Testimonial;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
public class TestimonialDao {

	@PersistenceContext
	private EntityManager entityManager;

	public Testimonial save(Testimonial testimonial) {
		if (testimonial.getId() == null) {
			entityManager.persist(testimonial);
		} else {
			testimonial = entityManager.merge(testimonial);
		}
		return testimonial;
	}

	public Optional<Testimonial> findByUuid(UUID uuid) {
		TypedQuery<Testimonial> query = entityManager.createQuery(
				"SELECT t FROM Testimonial t LEFT JOIN FETCH t.translations WHERE t.uuid = :uuid", Testimonial.class);
		query.setParameter("uuid", uuid);
		List<Testimonial> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	public List<Testimonial> findAll() {
		TypedQuery<Testimonial> query = entityManager
				.createQuery("SELECT DISTINCT t FROM Testimonial t LEFT JOIN FETCH t.translations", Testimonial.class);
		return query.getResultList();
	}

	public void delete(Testimonial testimonial) {
		entityManager.remove(entityManager.contains(testimonial) ? testimonial : entityManager.merge(testimonial));
	}

	public void deleteByUuid(UUID uuid) {
		findByUuid(uuid).ifPresent(this::delete);
	}

	public Optional<Language> findLanguageByCode(String code) {
		TypedQuery<Language> query = entityManager.createQuery("SELECT l FROM Language l WHERE l.code = :code",
				Language.class);
		query.setParameter("code", code);
		List<Language> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}
}
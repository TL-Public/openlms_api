package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.TraineeTestimonial;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TraineeTestimonialDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional(readOnly = true)
	public List<TraineeTestimonial> findAll() {
		TypedQuery<TraineeTestimonial> query = entityManager.createQuery(
				"SELECT DISTINCT t FROM TraineeTestimonial t LEFT JOIN FETCH t.translations LEFT JOIN FETCH t.course",
				TraineeTestimonial.class);
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public Optional<TraineeTestimonial> findByUuid(UUID uuid) {
		TypedQuery<TraineeTestimonial> query = entityManager.createQuery(
				"SELECT DISTINCT t FROM TraineeTestimonial t LEFT JOIN FETCH t.translations LEFT JOIN FETCH t.course WHERE t.uuid = :uuid",
				TraineeTestimonial.class);
		query.setParameter("uuid", uuid);
		List<TraineeTestimonial> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	@Transactional
	public TraineeTestimonial save(TraineeTestimonial testimonial) {
		if (testimonial.getId() == null) {
			entityManager.persist(testimonial);
		} else {
			testimonial = entityManager.merge(testimonial);
		}
		return testimonial;
	}

	@Transactional
	public void deleteByUuid(UUID uuid) {
		TraineeTestimonial testimonial = findByUuid(uuid).orElse(null);
		if (testimonial != null) {
			entityManager.remove(testimonial);
		}
	}

	@Transactional(readOnly = true)
	public List<TraineeTestimonial> findByCourseUuid(UUID courseUuid) {
		TypedQuery<TraineeTestimonial> query = entityManager.createQuery(
				"SELECT DISTINCT t FROM TraineeTestimonial t LEFT JOIN FETCH t.translations WHERE t.course.uuid = :courseUuid",
				TraineeTestimonial.class);
		query.setParameter("courseUuid", courseUuid);
		return query.getResultList();
	}
}
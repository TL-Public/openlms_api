package com.tl.reap_admin_api.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tl.reap_admin_api.model.TraineeProfile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
public class TraineeProfileDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional(readOnly = true)
	public List<TraineeProfile> findAll() {
		TypedQuery<TraineeProfile> query = entityManager.createQuery("SELECT t FROM TraineeProfile t",
				TraineeProfile.class);
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public List<TraineeProfile> findAllByStateId(Integer stateId) {
		String queryStr = "SELECT tp FROM TraineeProfile tp WHERE tp.id IN " +
                  "(SELECT DISTINCT tr.traineeProfile.id FROM TraineeRseti tr WHERE tr.rseti.id IN "+
				 	 "(SELECT DISTINCT id from RSETI r where r.stateId = :stateId ))";
		TypedQuery<TraineeProfile> query = entityManager.createQuery(queryStr, TraineeProfile.class);
		query.setParameter("stateId", stateId);
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public List<TraineeProfile> findAllByRsetiId(UUID rsetiUuid) {
		String queryStr = "SELECT tp FROM TraineeProfile tp WHERE tp.id IN " +
                  "(SELECT DISTINCT tr.traineeProfile.id FROM TraineeRseti tr WHERE tr.rseti.id = "+
				  "(select id from RSETI where uuid = :rsetiUuid))";
		TypedQuery<TraineeProfile> query = entityManager.createQuery(queryStr, TraineeProfile.class);
		query.setParameter("rsetiUuid", rsetiUuid);
		return query.getResultList();

	}

	@Transactional(readOnly = true)
	public TraineeProfile findByUuid(UUID uuid) {
		TypedQuery<TraineeProfile> query = entityManager
				.createQuery("SELECT t FROM TraineeProfile t WHERE t.uuid = :uuid", TraineeProfile.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}

	@Transactional(readOnly = true)
	public TraineeProfile findByTraineeCredential(Long trainee_id) {
		TypedQuery<TraineeProfile> query = entityManager
				.createQuery("SELECT t FROM TraineeProfile t WHERE t.trainee.id = :trainee_id", TraineeProfile.class);
		query.setParameter("trainee_id", trainee_id);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	public void update(TraineeProfile traineeProfile) {
		entityManager.merge(traineeProfile);
	}

	@Transactional
	public TraineeProfile save(TraineeProfile traineeProfile) {
		if (traineeProfile.getUuid() == null) {
			entityManager.persist(traineeProfile);
		} else {
			traineeProfile = entityManager.merge(traineeProfile);
		}
		return traineeProfile;
	}

	public void delete(TraineeProfile traineeProfile) {
		if (traineeProfile != null) {
			entityManager.remove(
					entityManager.contains(traineeProfile) ? traineeProfile : entityManager.merge(traineeProfile));
		}
	}

	@Transactional(readOnly = true)
    public TraineeProfile findByEnrollId(String enrollId) {
        try {
            TypedQuery<TraineeProfile> query = entityManager.createQuery(
                "SELECT tp FROM TraineeProfile tp WHERE tp.enrollId = :enrollId", TraineeProfile.class);
            query.setParameter("enrollId", enrollId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
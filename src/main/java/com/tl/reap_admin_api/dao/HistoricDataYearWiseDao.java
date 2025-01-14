package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.HistoricDataYearWise;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class HistoricDataYearWiseDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(HistoricDataYearWise historicData) {
        if (historicData.getId() == null) {
            entityManager.persist(historicData);
        } else {
            entityManager.merge(historicData);
        }
    }

    public HistoricDataYearWise findById(Long id) {
        return entityManager.find(HistoricDataYearWise.class, id);
    }

    public List<HistoricDataYearWise> findByRsetiId(Long rsetiId) {
        TypedQuery<HistoricDataYearWise> query = entityManager.createQuery(
            "SELECT h FROM HistoricDataYearWise h WHERE h.rseti.id = :rsetiId", HistoricDataYearWise.class);
        query.setParameter("rsetiId", rsetiId);
        return query.getResultList();
    }

    public List<HistoricDataYearWise> findByStateId(Integer stateId) {
        TypedQuery<HistoricDataYearWise> query = entityManager.createQuery(
            "SELECT h FROM HistoricDataYearWise h WHERE h.stateId = :stateId", HistoricDataYearWise.class);
        query.setParameter("stateId", stateId);
        return query.getResultList();
    }

    public List<HistoricDataYearWise> findByFiscalYear(String fiscalYear) {
        TypedQuery<HistoricDataYearWise> query = entityManager.createQuery(
            "SELECT h FROM HistoricDataYearWise h WHERE h.fiscalYear = :fiscalYear", HistoricDataYearWise.class);
        query.setParameter("fiscalYear", fiscalYear);
        return query.getResultList();
    }

    public List<Object[]> findTotalsByYear() {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT h.fiscalYear, SUM(h.trainedCount) as trained_count, SUM(h.settledCount) as settled_count " +
            "FROM HistoricDataYearWise h GROUP BY h.fiscalYear", Object[].class);
        return query.getResultList();
    }

    public List<Object[]> findTotalsByState() {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT h.stateId, h.fiscalYear, SUM(h.trainedCount) as trained_count, SUM(h.settledCount) as settled_count " +
            "FROM HistoricDataYearWise h GROUP BY h.stateId, h.fiscalYear", Object[].class);
        return query.getResultList();
    }

    public List<Object[]> findTotalsByRseti() {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT h.rsetiUuid, h.fiscalYear, SUM(h.trainedCount) as trained_count, SUM(h.settledCount) as settled_count " +
            "FROM HistoricDataYearWise h GROUP BY h.rsetiUuid, h.fiscalYear", Object[].class);
        return query.getResultList();
    }

    @Transactional
    public void delete(HistoricDataYearWise historicData) {
        entityManager.remove(entityManager.contains(historicData) ? historicData : entityManager.merge(historicData));
    }

    public Long getTotalTrainedCount() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT SUM(h.trainedCount) FROM HistoricDataYearWise h", Long.class);
        return query.getSingleResult();
    }

    public Long getTotalSettledCount() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT SUM(h.settledCount) FROM HistoricDataYearWise h", Long.class);
        return query.getSingleResult();
    }
}
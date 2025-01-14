package com.tl.reap_admin_api.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.tl.reap_admin_api.model.Bank;

import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional
public class BankDao {

    @PersistenceContext
    private EntityManager entityManager;

    // Save a new Bank
    public Bank save(Bank bank) {
        entityManager.persist(bank);
        return bank;
    }

    // Update an existing Bank
    public Bank update(Bank bank) {
        return entityManager.merge(bank);
    }

    // Find a Bank by UUID
    public Optional<Bank> findByUuid(UUID uuid) {
        try {
            Bank bank = entityManager.createQuery("SELECT b FROM Bank b WHERE b.uuid = :uuid", Bank.class)
                    .setParameter("uuid", uuid)
                    .getSingleResult();
            return Optional.of(bank);
        } catch (NoResultException e) {
            return Optional.empty(); 
        }
    }

    // Find all Banks
    public List<Bank> findAll() {
        return entityManager.createQuery("SELECT b FROM Bank b", Bank.class).getResultList();
    }

     // Find all Banks
     public Bank findByName(String name) {
        return entityManager.createQuery("SELECT b FROM Bank b where b.name = :name", Bank.class)
            .setParameter("name", name)
            .getSingleResult();
    }

    // Delete a Bank
    public void delete(Bank bank) {
        if (entityManager.contains(bank)) {
            entityManager.remove(bank);
        } else {
            entityManager.remove(entityManager.merge(bank));
        }
    }
}

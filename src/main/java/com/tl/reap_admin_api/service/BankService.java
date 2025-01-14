package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.BankDao;
import com.tl.reap_admin_api.dto.BankDto;
import com.tl.reap_admin_api.mapper.BankMapper;
import com.tl.reap_admin_api.model.Bank;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankService {

    private final BankDao bankDao;
    private final BankMapper bankMapper;

    public BankService(BankDao bankDao, BankMapper bankMapper) {
        this.bankDao = bankDao;
        this.bankMapper = bankMapper;
    }

    // Create new bank
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public BankDto createBank(BankDto bankDto) {
        Bank bank = bankMapper.toEntity(bankDto);
        bank.setUuid(UUID.randomUUID()); 
        Bank savedBank = bankDao.save(bank);
        return bankMapper.toDTO(savedBank);
    }

    // Get all banks
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public List<BankDto> getAllBanks() {
        List<Bank> banks = bankDao.findAll();
        return banks.stream().map(bankMapper::toDTO).collect(Collectors.toList());
    }

    // Get bank by UUID
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public BankDto getBankByUuid(UUID uuid) {
        Bank bank = bankDao.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Bank not found"));
        return bankMapper.toDTO(bank);
    }

    // Update bank by UUID
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public BankDto updateBank(UUID uuid, BankDto bankDto) {
        Bank bank = bankDao.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Bank not found"));
        bankMapper.updateEntityFromDTO(bankDto, bank);
        Bank updatedBank = bankDao.update(bank);
        return bankMapper.toDTO(updatedBank);
    }

    // Delete bank by UUID
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public void deleteBank(UUID uuid) {
        Bank bank = bankDao.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Bank not found"));
        bankDao.delete(bank);
    }
}

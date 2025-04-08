package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.BankDao;
import com.tl.reap_admin_api.dto.BankDto;
import com.tl.reap_admin_api.mapper.BankMapper;
import com.tl.reap_admin_api.model.Bank;
import com.tl.reap_admin_api.model.User;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankService {

    private final BankDao bankDao;
    private final BankMapper bankMapper;
    private final UserService userService;

    public BankService(BankDao bankDao, BankMapper bankMapper, UserService userService) {
        this.bankDao = bankDao;
        this.bankMapper = bankMapper;
        this.userService = userService;
    }

    // Create new bank
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public BankDto createBank(BankDto bankDto) {
        Bank bank = bankMapper.toEntity(bankDto);
        bank.setUuid(UUID.randomUUID()); 
        bank.setCreatedAt(ZonedDateTime.now());
        bank.setUpdatedAt(ZonedDateTime.now());
        User currentUser = userService.getCurrentUser();
        bank.setCreatedBy(currentUser.getUsername());
        bank.setUpdatedBy(currentUser.getUsername());

        Bank savedBank = bankDao.save(bank);
        return bankMapper.toDTO(savedBank);
    }

    // Get all banks
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public List<BankDto> getAllBanks() {
        List<Bank> banks = bankDao.findAll();
        return banks.stream().map(bankMapper::toDTO).collect(Collectors.toList());
    }

    // Get bank by UUID
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public BankDto getBankByUuid(UUID uuid) {
        Bank bank = bankDao.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Bank not found"));
        return bankMapper.toDTO(bank);
    }

    // Update bank by UUID
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public BankDto updateBank(UUID uuid, BankDto bankDto) {
        Bank bank = bankDao.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Bank not found"));
        bankMapper.updateEntityFromDTO(bankDto, bank);
        Bank updatedBank = bankDao.update(bank);
        updatedBank.setUpdatedAt(ZonedDateTime.now());
        User currentUser = userService.getCurrentUser();
        updatedBank.setUpdatedBy(currentUser.getUsername());
        return bankMapper.toDTO(updatedBank);
    }

 // Delete bank by UUID
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public void deleteBank(UUID uuid) {
        Bank bank = bankDao.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Bank not found"));
        User currentUser = userService.getCurrentUser();
        bank.setUpdatedBy(currentUser.getUsername());

        bank.setUpdatedAt(ZonedDateTime.now());
        bankDao.delete(bank);        
    }
}

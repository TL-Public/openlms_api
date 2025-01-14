package com.tl.reap_admin_api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import com.tl.reap_admin_api.dto.BankDto;
import com.tl.reap_admin_api.service.BankService;

@RestController
@RequestMapping("/apis/v1/banks")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    public ResponseEntity<?> createBank(@RequestBody BankDto bankDto) {
        try {
            BankDto createdBank = bankService.createBank(bankDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBank);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllBanks() {
        try {
            List<BankDto> banks = bankService.getAllBanks();
            if (banks.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(banks, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getBankByUuid(@PathVariable UUID uuid) {
        try {
            BankDto bank = bankService.getBankByUuid(uuid);
            if (bank != null) {
                return new ResponseEntity<>(bank, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateBank(@PathVariable UUID uuid, @RequestBody BankDto bankDto) {
        try {
            BankDto updatedBank = bankService.updateBank(uuid, bankDto);
            if (updatedBank != null) {
                return new ResponseEntity<>(updatedBank, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteBank(@PathVariable UUID uuid) {
        try {
            bankService.deleteBank(uuid);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
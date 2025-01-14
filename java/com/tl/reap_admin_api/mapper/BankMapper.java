package com.tl.reap_admin_api.mapper;
import com.tl.reap_admin_api.dto.BankDto;
import com.tl.reap_admin_api.model.Bank;
import org.springframework.stereotype.Component;

@Component
public class BankMapper {
	 public BankDto toDTO(Bank bank) {
	        if (bank == null) {
	            return null;
	        }
	        return new BankDto(
	            bank.getUuid(),
	            bank.getName()
	        );
	    }

	    public Bank toEntity(BankDto dto) {
	        if (dto == null) {
	            return null;
	        }
	        Bank bank = new Bank();
	        bank.setUuid(dto.getUuid());
	        bank.setName(dto.getName());
	        return bank;
	    }

	    public void updateEntityFromDTO(BankDto dto, Bank bank) {
	        if (dto == null || bank == null) {
	            return;
	        }
	        bank.setName(dto.getName());
	    }

}

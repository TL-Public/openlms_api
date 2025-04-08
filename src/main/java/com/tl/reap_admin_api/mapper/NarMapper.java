package com.tl.reap_admin_api.mapper;

import java.util.UUID;

import com.tl.reap_admin_api.dto.NarDto;
import com.tl.reap_admin_api.model.Nar;

public class NarMapper {

	public static NarDto convertToDTO(Nar entity) {
		if (entity == null) {
			return null;
		}

		NarDto dto = new NarDto();
		dto.setUuid(entity.getUuid());
		dto.setAddress(entity.getAddress());
		dto.setPhoneNumber(entity.getPhoneNumber());
		dto.setEmail(entity.getEmail());

		return dto;
	}

	public static Nar convertToEntity(NarDto dto) {
		if (dto == null) {
			return null;
		}

		Nar entity = new Nar();

		if (dto.getAddress() != null) {
			entity.setAddress(dto.getAddress());
		}
		if (dto.getPhoneNumber() != null) {
			entity.setPhoneNumber(dto.getPhoneNumber());
		}
		if (dto.getEmail() != null) {
			entity.setEmail(dto.getEmail());
		}

		if (dto.getUuid() != null) {
			entity.setUuid(dto.getUuid());
		} else {
			entity.setUuid(UUID.randomUUID().toString());
		}

		return entity;
	}
}

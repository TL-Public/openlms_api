package com.tl.reap_admin_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tl.reap_admin_api.dao.NarDao;
import com.tl.reap_admin_api.dto.NarDto;
import com.tl.reap_admin_api.mapper.NarMapper;
import com.tl.reap_admin_api.model.Nar;
import com.tl.reap_admin_api.model.User;

import jakarta.persistence.EntityNotFoundException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NarService {

	@Autowired
	private NarDao narDao;
	@Autowired
	private UserService userService;

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public NarDto createNar(NarDto narDto) {
		Nar nar = NarMapper.convertToEntity(narDto);
		// Get the current user
        User currentUser = userService.getCurrentUser();
        nar.setUpdatedBy(currentUser.getUsername());
        nar.setUpdatedAt(ZonedDateTime.now());
        nar.setCreatedAt(ZonedDateTime.now());
        nar.setCreatedBy(currentUser.getUsername());
		Nar savedNar = narDao.save(nar);
		return NarMapper.convertToDTO(savedNar);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public NarDto getNarByUuid(String uuid) {
		Nar nar = narDao.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Nar not found with uuid: " + uuid));
		return NarMapper.convertToDTO(nar);
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public List<NarDto> getAllNars() {
		List<Nar> nars = narDao.findAll();
		return nars.stream().map(NarMapper::convertToDTO).collect(Collectors.toList());
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public NarDto updateNar(String uuid, NarDto narDto) {
		Nar existingNar = narDao.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Nar not found with uuid: " + uuid));

		// Update only the fields that are not null in the DTO
		if (narDto.getAddress() != null) {
			existingNar.setAddress(narDto.getAddress());
		}
		if (narDto.getPhoneNumber() != null) {
			existingNar.setPhoneNumber(narDto.getPhoneNumber());
		}
		if (narDto.getEmail() != null) {
			existingNar.setEmail(narDto.getEmail());
		}

		Nar updatedNar = narDao.save(existingNar);
		 // Get the current user
        User currentUser = userService.getCurrentUser();
        updatedNar.setUpdatedBy(currentUser.getUsername());
        updatedNar.setUpdatedAt(ZonedDateTime.now());
		return NarMapper.convertToDTO(updatedNar);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
	public void deleteNar(String uuid) {
		Nar nar = narDao.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException("Nar not found with uuid: " + uuid));
		// Get the current user
        User currentUser = userService.getCurrentUser();
        nar.setUpdatedBy(currentUser.getUsername());
        nar.setUpdatedAt(ZonedDateTime.now());
		narDao.delete(nar);
	}
}

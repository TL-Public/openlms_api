package com.tl.reap_admin_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tl.reap_admin_api.dto.NarDto;
import com.tl.reap_admin_api.service.NarService;

@RestController
@RequestMapping("/apis/v1/nars")
public class NarsController {
	@Autowired
	private NarService narService;

	@PostMapping
	public ResponseEntity<NarDto> createNar(@RequestBody NarDto narDto) {
		try {
			NarDto createdNar = narService.createNar(narDto);
			return new ResponseEntity<>(createdNar, HttpStatus.CREATED);
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{uuid}")	
	public ResponseEntity<NarDto> getNarByUuid(@PathVariable String uuid) {
		try {
			NarDto nar = narService.getNarByUuid(uuid);
			return ResponseEntity.ok(nar);
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping
	public ResponseEntity<List<NarDto>> getAllNars() {
		try {
			List<NarDto> nars = narService.getAllNars();
			return ResponseEntity.ok(nars);
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/{uuid}")
	public ResponseEntity<NarDto> updateNar(@PathVariable String uuid, @RequestBody NarDto narDto) {
		try {
			NarDto updatedNar = narService.updateNar(uuid, narDto);
			return ResponseEntity.ok(updatedNar);
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/{uuid}")
	public ResponseEntity<Void> deleteNar(@PathVariable String uuid) {
		try {
			narService.deleteNar(uuid);
			return ResponseEntity.noContent().build();
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}

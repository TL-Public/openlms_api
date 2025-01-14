package com.tl.reap_admin_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tl.reap_admin_api.dto.StateDto;
import com.tl.reap_admin_api.service.StateService;

@RestController
@RequestMapping("/apis/v1/states")
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @PostMapping
    public ResponseEntity<?> createState(@RequestBody StateDto stateDto) {
        try {
            StateDto createdState = stateService.createState(stateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdState);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
    @GetMapping
    public ResponseEntity<?> getAllStates() {
        try {
            List<StateDto> states = stateService.getAllStates();
            return ResponseEntity.ok(states);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{extId}/{languageCode}")
    public ResponseEntity<?> getStateByExtIdAndLanguageCode(@PathVariable Integer extId, @PathVariable String languageCode) {
        try {
            StateDto state = stateService.getStateByExtIdAndLanguageCode(extId, languageCode);
            return ResponseEntity.ok(state);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{extId}")
    public ResponseEntity<?> updateState(@PathVariable Integer extId, @RequestBody StateDto stateDto) {
        try {
            StateDto updatedState = stateService.updateState(extId, stateDto);
            return ResponseEntity.ok(updatedState);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
    
    @GetMapping("/{extId}")
    public ResponseEntity<?> getStatesByExtId(@PathVariable Integer extId) {
        try {
            List<StateDto> states = stateService.getAllStates(extId);
            return ResponseEntity.ok(states);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
    
    @DeleteMapping("/{extId}")
    public ResponseEntity<?> deleteStateByExtId(@PathVariable String extId) {
        try {
            boolean deleted = stateService.deleteStateByExtId(extId); 
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("State(s) not found");
            }
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); 
        }
    }







}
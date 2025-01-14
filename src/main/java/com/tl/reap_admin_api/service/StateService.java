package com.tl.reap_admin_api.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tl.reap_admin_api.dao.StateDao;
import com.tl.reap_admin_api.dto.StateDto;
import com.tl.reap_admin_api.mapper.StateMapper;
import com.tl.reap_admin_api.model.State;

import jakarta.transaction.Transactional;

@Service
public class StateService {

    private final StateDao stateDAO;
    private final StateMapper stateMapper;

    public StateService(StateDao stateDAO, StateMapper stateMapper) {
        this.stateDAO = stateDAO;
        this.stateMapper = stateMapper;
    }

 // Create new state
    @Transactional
    //@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public StateDto createState(StateDto stateDto) {
        try {
            // Check if a state with the same name and language code already exists
            Optional<State> existingStateByName = stateDAO.findByNameAndLanguageCode(stateDto.getName(), stateDto.getLanguageCode());
            if (existingStateByName.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "State already exists with the given name and language code");
            }

            // Check if a state with the same extId and language code already exists
            if (stateDto.getExtId() != null) {
                Optional<State> existingStateByExtId = stateDAO.findByExtIdAndLanguageCode(stateDto.getExtId(), stateDto.getLanguageCode());
                if (existingStateByExtId.isPresent()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "State already exists with the given extId and language code");
                }
            }

            // Check if the state exists in other languages
            List<State> statesWithSameName = stateDAO.findByExtId(stateDto.getExtId());

            State newState;
            if (!statesWithSameName.isEmpty()) {
                // State exists in other language, create new translation
                Integer extId = statesWithSameName.get(0).getExtId();
                System.out.println("Add new state in another language- "+ stateDto.getName()+"-- "+ extId+"-- "+ stateDto.getLanguageCode());
                newState = new State(stateDto.getName(), extId, stateDto.getLanguageCode());
            } else {
                // Completely new state, create with new extId
                Integer maxExtId = stateDAO.getMaxExtId();
                Integer newExtId = (maxExtId == null) ? 1 : maxExtId + 1;
                System.out.println("Add new state - "+ stateDto.getName()+"-- "+ newExtId+"-- "+ stateDto.getLanguageCode());
                newState = new State(stateDto.getName(), newExtId, stateDto.getLanguageCode());
            }

            State savedState = stateDAO.save(newState);
            return stateMapper.toDTO(savedState);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
           
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the state", e);
        }
    }

 // Get all states
    public List<StateDto> getAllStates() {
        List<State> states = stateDAO.findAllWithDistricts();
        return states.stream()
            .map(stateMapper::toDTO)
            .collect(Collectors.toList());
    }


 // Get state by extId and languageCode
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public StateDto getStateByExtIdAndLanguageCode(Integer extId, String languageCode) {
        State state = stateDAO.findByExtIdAndLanguageCodeWithDistricts(extId, languageCode)
                .orElseThrow(() -> new RuntimeException("State not found"));
        return stateMapper.toDTO(state);
    }


    // Update state by extId and languageCode
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public StateDto updateState(Integer extId, StateDto stateDto) {
        State state = stateDAO.findByExtIdAndLanguageCode(extId, stateDto.getLanguageCode())
                .orElseThrow(() -> new RuntimeException("State not found"));
        stateMapper.updateEntityFromDTO(stateDto, state);
        State updatedState = stateDAO.update(state);
        return stateMapper.toDTO(updatedState);
    }

 // Get all states by extId
    public List<StateDto> getAllStates(Integer extId) {
        List<State> states;
        if (extId != null) {
            states = stateDAO.findByExtIdWithDistricts(extId);
        } else {
            states = stateDAO.findAllWithDistricts();
        }
        return states.stream().map(stateMapper::toDTO).collect(Collectors.toList());
    }
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF')")
    public boolean deleteStateByExtId(String extId) {
        int deletedCount = stateDAO.deleteByExtId(extId); 
        return deletedCount > 0; 
    }




}
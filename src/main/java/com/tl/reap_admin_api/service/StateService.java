package com.tl.reap_admin_api.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tl.reap_admin_api.dao.DistrictDao;
import com.tl.reap_admin_api.dao.StateDao;
import com.tl.reap_admin_api.dto.DistrictDto;
import com.tl.reap_admin_api.dto.StateDto;
import com.tl.reap_admin_api.mapper.StateMapper;
import com.tl.reap_admin_api.model.District;
import com.tl.reap_admin_api.model.State;
import com.tl.reap_admin_api.model.User;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class StateService {

    private final StateDao stateDAO;
    private final StateMapper stateMapper;
    private final UserService userService;
    private final DistrictDao districtDao;

    public StateService(StateDao stateDAO, StateMapper stateMapper,UserService userService, DistrictDao districtDao) {
        this.stateDAO = stateDAO;
        this.stateMapper = stateMapper;
        this.userService = userService;
        this.districtDao = districtDao;
    }

 // Create new state
    @Transactional
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
            List<State> statesWithSameName = stateDto.getExtId() != null ? stateDAO.findByExtId(stateDto.getExtId()) : new ArrayList<>();

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
            
            // Set createdBy and updatedBy
            User currentUser = userService.getCurrentUser();
            newState.setCreatedBy(currentUser.getUsername());
            newState.setUpdatedBy(currentUser.getUsername());
            newState.setUpdatedAt(ZonedDateTime.now());
            newState.setCreatedAt(ZonedDateTime.now());
            newState.setIsoCode(stateDto.getIsoCode());

            // Save the state first to get an ID
            State savedState = stateDAO.save(newState);
            
            // Now handle districts if they exist
            if (stateDto.getDistricts() != null && !stateDto.getDistricts().isEmpty()) {
                for (DistrictDto districtDto : stateDto.getDistricts()) {
                    District district = new District();
                    district.setName(districtDto.getName());
                    district.setLanguageCode(districtDto.getLanguageCode());
                    
                    // Get max district extId or use 1 if none exists
                    Integer maxDistrictExtId = districtDao.getMaxExtId();
                    Integer newDistrictExtId = (maxDistrictExtId == null) ? 1 : maxDistrictExtId + 1;
                    district.setExtId(newDistrictExtId);
                    
                    district.setState(savedState);
                    district.setCreatedBy(currentUser.getUsername());
                    district.setUpdatedBy(currentUser.getUsername());
                    district.setCreatedAt(ZonedDateTime.now());
                    district.setUpdatedAt(ZonedDateTime.now());
                    
                    savedState.addDistrict(district);
                }
                
                // Save again with districts
                savedState = stateDAO.save(savedState);
            }
            
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public StateDto getStateByExtIdAndLanguageCode(Integer extId, String languageCode) {
        State state = stateDAO.findByExtIdAndLanguageCodeWithDistricts(extId, languageCode)
                .orElseThrow(() -> new RuntimeException("State not found"));
        return stateMapper.toDTO(state);
    }


    // Update state by extId and languageCode
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public StateDto updateState(Integer extId, StateDto stateDto) {
        State state = stateDAO.findByExtIdAndLanguageCode(extId, stateDto.getLanguageCode())
                .orElseThrow(() -> new RuntimeException("State not found"));
        stateMapper.updateEntityFromDTO(stateDto, state);
        State updatedState = stateDAO.update(state);
     // Get the current user
        User currentUser = userService.getCurrentUser();
        updatedState.setUpdatedBy(currentUser.getUsername());
        updatedState.setUpdatedAt(ZonedDateTime.now());
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
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public boolean deleteStateByExtId(String extId) {
        State state = stateDAO.findByExtId(extId)
            .orElseThrow(() -> new EntityNotFoundException("State not found for extId: " + extId));

        User currentUser = userService.getCurrentUser();
        state.setUpdatedBy(currentUser.getUsername());
        state.setUpdatedAt(ZonedDateTime.now());

        stateDAO.save(state); 
        int deletedCount = stateDAO.deleteByExtId(extId);
        
        return deletedCount > 0;
    }





}
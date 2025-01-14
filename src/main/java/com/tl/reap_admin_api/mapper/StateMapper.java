package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.StateDto;
import com.tl.reap_admin_api.dto.DistrictDto;
import com.tl.reap_admin_api.model.State;
import com.tl.reap_admin_api.model.District;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class StateMapper {
    public StateDto toDTO(State state) {
        if (state == null) {
            return null;
        }
        StateDto stateDto = new StateDto(
            state.getExtId(),
            state.getName(),
            state.getIsoCode(),
            state.getLanguageCode()
        );
        
        if (state.getDistricts() != null) {
            stateDto.setDistricts(state.getDistricts().stream()
                .filter(district -> district.getLanguageCode().equals(state.getLanguageCode()))
                .map(this::districtToDTO)
                .collect(Collectors.toList()));
        }
        
        return stateDto;
    }

    public State toEntity(StateDto dto) {
        if (dto == null) {
            return null;
        }
        State state = new State();
        state.setExtId(dto.getExtId());
        state.setName(dto.getName());
        state.setIsoCode(dto.getIsoCode());
        state.setLanguageCode(dto.getLanguageCode());
        
        if (dto.getDistricts() != null) {
            state.setDistricts(dto.getDistricts().stream()
                .map(this::districtToEntity)
                .collect(Collectors.toList()));
        }
        
        return state;
    }

    public void updateEntityFromDTO(StateDto dto, State state) {
        if (dto == null || state == null) {
            return;
        }
        if(dto.getName() != null) state.setName(dto.getName());
        if(dto.getIsoCode() != null) state.setIsoCode(dto.getIsoCode());
        if(dto.getLanguageCode() != null) state.setLanguageCode(dto.getLanguageCode());
        
        if (dto.getDistricts() != null) {
            state.setDistricts(dto.getDistricts().stream()
                .map(this::districtToEntity)
                .collect(Collectors.toList()));
        }
    }

    private DistrictDto districtToDTO(District district) {
        return new DistrictDto(district.getExtId(), district.getName(), district.getLanguageCode());
    }

    private District districtToEntity(DistrictDto dto) {
        District district = new District();
        district.setExtId(dto.getExtId());
        district.setName(dto.getName());
        district.setLanguageCode(dto.getLanguageCode());
        return district;
    }
}
package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.AppConfigDTO;
import com.tl.reap_admin_api.model.AppConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppConfigMapper {

    public AppConfigDTO toDTO(AppConfig entity) {
        if (entity == null) {
            return null;
        }
        
        return new AppConfigDTO(
            entity.getKey(),
            entity.getValue()
        );
    }

    public AppConfig toEntity(AppConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        
        AppConfig entity = new AppConfig();
        entity.setKey(dto.getKey());
        entity.setValue(dto.getValue());
        // Audit fields will be set in the service layer
        
        return entity;
    }

    public List<AppConfigDTO> toDTOList(List<AppConfig> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(AppConfigDTO dto, AppConfig entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        entity.setValue(dto.getValue());
        // We don't update key as it's the ID
        // Audit fields will be updated in the service layer
    }
}
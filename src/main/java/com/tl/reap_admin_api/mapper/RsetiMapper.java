package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.RsetiDto;
import com.tl.reap_admin_api.dto.RsetiListDto;
import com.tl.reap_admin_api.dto.RsetiTranslationDto;
import com.tl.reap_admin_api.exception.LanguageNotFoundException;
import com.tl.reap_admin_api.model.Language;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.RsetiTranslation;
import com.tl.reap_admin_api.service.LanguageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RsetiMapper {
    
    @Autowired
    private LanguageService languageService;

    public RsetiDto toDTO(RSETI rseti) {
        if (rseti == null) {
            return null;
        }

        RsetiDto dto = new RsetiDto(
            rseti.getUuid(),
            rseti.getBankId(),
            rseti.getStateId(),
            rseti.getExtId(),
            rseti.getEmail(),
            rseti.getContactNo(),
            rseti.getDirectorContactNo(),
            rseti.getStatus()
            
        );
        
        dto.setTranslations(rseti.getTranslations().stream()
            .map(this::toTranslationDTO)
            .collect(Collectors.toList()));
        return dto;
    }

    public RSETI toEntity(RsetiDto dto) {
        if (dto == null) {
            return null;
        }
        RSETI rseti = new RSETI();
        updateEntityFromDTO(dto, rseti);
        return rseti;
    }
   
    public RsetiTranslationDto toTranslationDTO(RsetiTranslation translation) {
        if(translation == null) {
            return null;
        }
        return new RsetiTranslationDto(
            translation.getId(),
            translation.getLanguage().getCode(),
            translation.getDistrict(),
            translation.getDistrictId(),
            translation.getName(),            
            translation.getAddress(),
            translation.getDirectorName()
        );
    }

    public RsetiTranslation toTranslationEntity(RsetiTranslationDto dto) {
        RsetiTranslation translation = new RsetiTranslation();
        if (dto.getId() != null) translation.setId(dto.getId());
        if (dto.getLanguageCode() != null) {
            Language language = languageService.getLanguageByCode(dto.getLanguageCode());
            if (language == null) {
                throw new LanguageNotFoundException("Language not found with code: " + dto.getLanguageCode());
            }
            translation.setLanguage(language);
        }
        if (dto.getName() != null) translation.setName(dto.getName());
        if (dto.getDistrict() != null) translation.setDistrict(dto.getDistrict());
        if (dto.getDistrictId() != null) translation.setDistrictId(dto.getDistrictId());
        if (dto.getAddress() != null) translation.setAddress(dto.getAddress());
        if (dto.getDirectorName() != null) translation.setDirectorName(dto.getDirectorName());
        return translation;
    }

    public void updateEntityFromDTO(RsetiDto dto, RSETI rseti) {
        if (dto == null || rseti == null) {
            return;
        }

        // Update only non-null fields
        if (dto.getExtId() != null) rseti.setExtId(dto.getExtId());
        if (dto.getEmail() != null) rseti.setEmail(dto.getEmail());
        if (dto.getContactNo() != null) rseti.setContactNo(dto.getContactNo());
        if (dto.getDirectorContactNo() != null) rseti.setDirectorContactNo(dto.getDirectorContactNo());
        if (dto.getStateId() != null) rseti.setStateId(dto.getStateId());
        if (dto.getBankId() != null) rseti.setBankId(dto.getBankId());
        if (dto.getStatus() != null) rseti.setStatus(dto.getStatus());

    }

    public void updateTranslationFromDTO(RsetiTranslationDto dto, RsetiTranslation translation) {
        if (dto == null || translation == null) {
            return;
        }

        // Update only non-null fields        
        if (dto.getLanguageCode() != null) {
            Language language = languageService.getLanguageByCode(dto.getLanguageCode());
            if (language == null) {
                throw new LanguageNotFoundException("Language not found with code: " + dto.getLanguageCode());
            }
            translation.setLanguage(language);
        }
        if (dto.getName() != null) translation.setName(dto.getName());
        if (dto.getDistrict() != null) translation.setDistrict(dto.getDistrict());
        if (dto.getDistrictId() != null) translation.setDistrictId(dto.getDistrictId());
        if (dto.getAddress() != null) translation.setAddress(dto.getAddress());
        if (dto.getDirectorName() != null) translation.setDirectorName(dto.getDirectorName());
    }

    public RsetiListDto toListDTO(RSETI rseti) {
        if (rseti == null) {
            return null;
        }

        List<RsetiTranslationDto> translations = rseti.getTranslations().stream()
            .map(this::toTranslationDTO)
            .collect(Collectors.toList());

        return new RsetiListDto(
            rseti.getUuid(),
            rseti.getExtId(),
            rseti.getEmail(),
            rseti.getContactNo(),
            rseti.getDirectorContactNo(),
            rseti.getStateId(),
            rseti.getBankId(),
            rseti.getRsetiCourses().size(),
            translations
        );
    }

}

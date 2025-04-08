package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.AppConfigDAO;
import com.tl.reap_admin_api.dto.AppConfigDTO;
import com.tl.reap_admin_api.mapper.AppConfigMapper;
import com.tl.reap_admin_api.model.AppConfig;
import com.tl.reap_admin_api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppConfigService {

    private final AppConfigDAO appConfigDAO;
    private final AppConfigMapper appConfigMapper;
    private final UserService userService;

    @Autowired
    public AppConfigService(AppConfigDAO appConfigDAO, AppConfigMapper appConfigMapper, UserService userService) {
        this.appConfigDAO = appConfigDAO;
        this.appConfigMapper = appConfigMapper;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public List<AppConfigDTO> findAll() {
        List<AppConfig> appConfigs = appConfigDAO.findAll();
        return appConfigMapper.toDTOList(appConfigs);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public Optional<AppConfigDTO> findByKey(String key) {
        return appConfigDAO.findByKey(key)
                .map(appConfigMapper::toDTO);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public AppConfigDTO create(AppConfigDTO appConfigDTO) {
        if (appConfigDAO.existsByKey(appConfigDTO.getKey())) {
            throw new IllegalArgumentException("AppConfig with key " + appConfigDTO.getKey() + " already exists");
        }
        
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Current user not available");
        }
        
        AppConfig appConfig = new AppConfig(
            appConfigDTO.getKey(),
            appConfigDTO.getValue(),
            currentUser.getUsername()
        );
        
        // Set created and updated timestamps
        appConfig.setCreatedAt(ZonedDateTime.now());
        appConfig.setUpdatedAt(ZonedDateTime.now());
        
        AppConfig savedAppConfig = appConfigDAO.save(appConfig);
        return appConfigMapper.toDTO(savedAppConfig);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public Optional<AppConfigDTO> update(String key, AppConfigDTO appConfigDTO) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Current user not available");
        }
        
        return appConfigDAO.findByKey(key)
                .map(existingAppConfig -> {
                    existingAppConfig.setValue(appConfigDTO.getValue());
                    existingAppConfig.setUpdatedBy(currentUser.getUsername());
                    existingAppConfig.setUpdatedAt(ZonedDateTime.now());
                    
                    AppConfig updatedAppConfig = appConfigDAO.save(existingAppConfig);
                    return appConfigMapper.toDTO(updatedAppConfig);
                });
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public boolean delete(String key) {
        if (!appConfigDAO.existsByKey(key)) {
            return false;
        }
        
        appConfigDAO.delete(key);
        return true;
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public List<AppConfigDTO> findByKeyContaining(String keyPattern) {
        List<AppConfig> appConfigs = appConfigDAO.findByKeyContaining(keyPattern);
        return appConfigMapper.toDTOList(appConfigs);
    }
}
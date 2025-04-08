package com.tl.reap_admin_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tl.reap_admin_api.dto.AppConfigDTO;
import com.tl.reap_admin_api.service.AppConfigService;

@RestController
@RequestMapping("apis/v1/app-config")
public class AppConfigController {

    private final AppConfigService appConfigService;

    @Autowired
    public AppConfigController(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @GetMapping
    public ResponseEntity<List<AppConfigDTO>> getAllAppConfigs() {
        List<AppConfigDTO> appConfigs = appConfigService.findAll();
        return ResponseEntity.ok(appConfigs);
    }

    @GetMapping("/{key}")
    public ResponseEntity<AppConfigDTO> getAppConfigByKey(@PathVariable String key) {
        return appConfigService.findByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<AppConfigDTO>> searchAppConfigs(@RequestParam String keyPattern) {
        List<AppConfigDTO> appConfigs = appConfigService.findByKeyContaining(keyPattern);
        return ResponseEntity.ok(appConfigs);
    }

    @PostMapping
    public ResponseEntity<AppConfigDTO> createAppConfig(@RequestBody AppConfigDTO appConfigDTO) {
        try {
            AppConfigDTO createdAppConfig = appConfigService.create(appConfigDTO);
            return new ResponseEntity<>(createdAppConfig, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{key}")
    public ResponseEntity<AppConfigDTO> updateAppConfig(
            @PathVariable String key,
            @RequestBody AppConfigDTO appConfigDTO) {
        return appConfigService.update(key, appConfigDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteAppConfig(@PathVariable String key) {
        boolean deleted = appConfigService.delete(key);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
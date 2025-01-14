package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.UserDto;
import com.tl.reap_admin_api.model.Role;
import com.tl.reap_admin_api.model.User;
import com.tl.reap_admin_api.model.UserProfile;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoleId(user.getRole().getNumber());
        dto.setStatus(user.getStatus());
        return dto;
    }

    public User toEntity(UserDto dto) {
        User user = new User();
        user.setUuid(dto.getUuid());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(Role.fromNumber(dto.getRoleId()));
        user.setStatus(dto.getStatus());
        return user;
    }
    
    
    public UserDto mapUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoleId(user.getRole().getNumber());
        dto.setStatus(user.getStatus());

        // Handle UserProfile data
        UserProfile profile = user.getUserProfile();
        if (profile != null) {
            dto.setName(profile.getName());
            dto.setExtId(profile.getExtId());
            dto.setDesignation(profile.getDesignation());
            dto.setContactNumber(profile.getContactNumber());
            dto.setPermanentAddr(profile.getPermanentAddr());
            dto.setCurrentAddr(profile.getCurrentAddr());
            dto.setStateId(profile.getState() != null ? profile.getState().getExtId() : null);
            dto.setRsetiId(profile.getRseti() != null ? profile.getRseti().getUuid() : null);
            dto.setPhotoUrl(profile.getPhotoUrl());
            dto.setStatus(user.getStatus());
        }

        return dto;
    }
}
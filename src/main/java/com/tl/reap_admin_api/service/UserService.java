package com.tl.reap_admin_api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tl.reap_admin_api.dao.StateDao;
import com.tl.reap_admin_api.controller.UserController;
import com.tl.reap_admin_api.dao.RsetiDao;
import com.tl.reap_admin_api.dao.UserDao;
import com.tl.reap_admin_api.dao.UserProfileDao;
import com.tl.reap_admin_api.dto.UserDto;
import com.tl.reap_admin_api.model.State;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.Role;
import com.tl.reap_admin_api.model.User;
import com.tl.reap_admin_api.model.UserProfile;
import com.tl.reap_admin_api.util.SecurityUtils;
import com.tl.reap_admin_api.exception.UserNotFoundException;
import com.tl.reap_admin_api.mapper.UserMapper;
import com.tl.reap_admin_api.security.UserPrincipal;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserDao userDao;
    private final UserProfileDao userProfileDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private final StateDao stateDao;

    private final RsetiDao rsetiDao;
   
    private final AmazonS3 amazonS3Client;

    @Value("${aws.s3.crsimg.bucket-name}")
    private String imgBucketName;

    @Value("${aws.s3.crsimg.bucket-url}")
    private String imgBucketUrl;
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
       

    @Autowired
    public UserService(UserDao userDao, UserProfileDao userProfileDao, UserMapper userMapper, PasswordEncoder passwordEncoder, 
                 AmazonS3 amazonS3Client, StateDao stateDao, RsetiDao rsetiDao) {
        this.userDao = userDao;
        this.userProfileDao = userProfileDao;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;       
        this.amazonS3Client = amazonS3Client;
        this.stateDao = stateDao;
        this.rsetiDao = rsetiDao;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")  
    public UserDto createUser(UserDto userDto, String password) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.fromNumber(userDto.getRoleId()));    
        user.setRoleId(userDto.getRoleId());    

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setName(userDto.getName());
        profile.setExtId(userDto.getExtId());
        profile.setDesignation(userDto.getDesignation());
        profile.setContactNumber(userDto.getContactNumber());
        profile.setEmail(userDto.getEmail());
        profile.setPermanentAddr(userDto.getPermanentAddr());
        profile.setCurrentAddr(userDto.getCurrentAddr()); 

        profile.setState(getState(userDto));
        
        // Set RSETI based on the current user's RSETI if not provided
        if (userDto.getRsetiId() == null) {
            User currentUser = getCurrentUser();
            if (currentUser.getRole() == Role.RSETI_ADMIN || currentUser.getRole() == Role.RSETI_STAFF) {
                profile.setRseti(currentUser.getUserProfile().getRseti());
            } else {
                profile.setRseti(getRseti(userDto));
            }
        } else {
            profile.setRseti(getRseti(userDto));
        }
        
        profile.setPhotoUrl(userDto.getPhotoUrl());
        profile.setCreatedBy(getCurrentUser().getUsername());
        profile.setUpdatedBy(getCurrentUser().getUsername());
        // Get the current user using this.getCurrentUser()
        User currentUser = this.getCurrentUser();

        user.setUserProfile(profile);
        checkUserReadPermission(user);
        user.getUserProfile().setUpdatedBy(currentUser.getUsername());
        user.getUserProfile().setUpdatedOn(ZonedDateTime.now());

        user = userDao.save(user);
        userProfileDao.save(profile);

        return mapUserToDto(user, profile);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public UserDto uploadProfilePic(UUID userUuid, MultipartFile file) throws IOException {
        try {
            User user = userDao.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found with UUID: " + userUuid));
            user.setCreatedBy(getCurrentUser().getUsername());
            user.setUpdatedBy(getCurrentUser().getUsername());
            // Get the current user using this.getCurrentUser()
            User currentUser = this.getCurrentUser();
            user.getUserProfile().setUpdatedBy(currentUser.getUsername());
            user.getUserProfile().setUpdatedOn(ZonedDateTime.now());

            checkUserReadPermission(user); // check the current user can update
           
            String key = "userprofile/" + user.getUsername() + ".png";

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3Client.putObject(new PutObjectRequest(imgBucketName, key, file.getInputStream(), metadata));

            String imageUrl = amazonS3Client.getUrl(imgBucketName, key).toString();
            user.getUserProfile().setPhotoUrl(imageUrl);

            User updatedUser = userDao.save(user);
            return userMapper.toDto(updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error uploading course image", e);
        }
    }
   
    private State  getState(UserDto userDto) {
        State state = null;
        if(userDto.getStateId() != null) {         
            Optional<State> stateOptional = stateDao.findByExtIdAndLanguageCodeWithDistricts(userDto.getStateId(),"en");
            if(stateOptional.isPresent()) {
                state = stateOptional.get();
            }
        }
        return state;
    }

    private RSETI  getRseti(UserDto userDto) {
        RSETI rseti = null;
        if(userDto.getRsetiId()!= null) {
            Optional<RSETI> rsetiOptional = rsetiDao.findByUuid(userDto.getRsetiId());
            if(rsetiOptional.isPresent()) {
                rseti = rsetiOptional.get();
            }
        }

        return rseti;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")   
    public UserDto getUserByUuid(UUID uuid) {
        User user = userDao.findByUuid(uuid)
        .orElseThrow(() -> new UserNotFoundException("User not found with UUID: " + uuid));

        checkUserReadPermission(user);

        UserProfile profile = null;
        Optional<UserProfile> profileOptional = userProfileDao.findByUser(user);
        if(profileOptional.isPresent()) {
            profile = profileOptional.get();
        }
               // .orElseThrow(() -> new UserNotFoundException("User profile not found for user with UUID: " + uuid));
        return mapUserToDto(user, profile);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public UserDto getCurrentUserProfile() {
        User currentUser = getCurrentUser();
       
        UserProfile profile = null;
        Optional<UserProfile> profileOptional = userProfileDao.findByUser(currentUser);
        if(profileOptional.isPresent()) {
            profile = profileOptional.get();
        }
              //  .orElseThrow(() -> new UserNotFoundException("User profile not found for user with UUID: " + currentUser.getUuid()));
        return mapUserToDto(currentUser, profile);
       
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public List<UserDto> getAllUsers() {
        User currentUser = getCurrentUser();
        List<User> users;

        Integer stateId = null;
        UUID rsetiId = null;

        switch (currentUser.getRole()) {
            case SUPER_ADMIN:
                users = userDao.findUsersWithLowerRoles(Role.PUBLIC, null, null, currentUser.getUuid());
                break;
            case NAR_ADMIN:
            case NAR_STAFF:
                users = userDao.findUsersWithLowerRoles(currentUser.getRole(), null, null, currentUser.getUuid());
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                stateId = getCurrentUserStateId();
                users = userDao.findUsersWithLowerRoles(currentUser.getRole(), stateId, null, currentUser.getUuid());
                break;
            case RSETI_ADMIN:
            case RSETI_STAFF:
                rsetiId = getCurrentUserRsetiId();
                users = userDao.findUsersWithLowerRoles(currentUser.getRole(), null, rsetiId, currentUser.getUuid());
                break;
            default:
                throw new AccessDeniedException("You don't have permission to access User list");
        }

        return users.stream()
            .map(userMapper::mapUserToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public UserDto updateUser(UUID uuid, UserDto userDto) {
        User user = userDao.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found with UUID: " + uuid));
     // Get the current user using this.getCurrentUser()
        User currentUser = this.getCurrentUser();
        user.getUserProfile().setUpdatedBy(currentUser.getUsername());
        user.getUserProfile().setUpdatedOn(ZonedDateTime.now());

        checkUserReadPermission(user);
        UserProfile profile = null;
        Optional<UserProfile> profileOptional = userProfileDao.findByUser(user);
        if(profileOptional.isPresent()) {
            profile = profileOptional.get();
        }
        else {
            profile = new UserProfile();
        }
      //          .orElseThrow(() -> new UserNotFoundException("User profile not found for user with UUID: " + uuid));

        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getRoleId() != null) {
            user.setRole(Role.fromNumber(userDto.getRoleId()));
        }
        user.setUpdatedAt(ZonedDateTime.now());

        if (user.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }

        if (userDto.getName() != null) {
            profile.setName(userDto.getName());
        }
        if (userDto.getExtId() != null) {
            profile.setExtId(userDto.getExtId());
        }
        if (userDto.getDesignation() != null) {
            profile.setDesignation(userDto.getDesignation());
        }
        if (userDto.getContactNumber() != null) {
            profile.setContactNumber(userDto.getContactNumber());
        }
        if (userDto.getEmail() != null) {
            profile.setEmail(userDto.getEmail());
        }
        if (userDto.getPermanentAddr() != null) {
            profile.setPermanentAddr(userDto.getPermanentAddr());
        }
        if (userDto.getCurrentAddr() != null) {
            profile.setCurrentAddr(userDto.getCurrentAddr());
        }
        if (userDto.getStateId() != null) {
            profile.setState(getState(userDto));
        }
        if (userDto.getRsetiId() != null) {
            profile.setRseti(getRseti(userDto));
        }
        if (userDto.getPhotoUrl() != null) {
            profile.setPhotoUrl(userDto.getPhotoUrl());
        }
        profile.setUpdatedOn(ZonedDateTime.now());
        profile.setUpdatedBy(getCurrentUser().getUsername());

        user = userDao.save(user);
        profile = userProfileDao.save(profile);

        return mapUserToDto(user, profile);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public void deleteUser(UUID uuid) {
        User user = userDao.findByUuid(uuid)
        .orElseThrow(() -> new UserNotFoundException("User not found with UUID: " + uuid));
        // Get the current user using this.getCurrentUser()
        User currentUser = this.getCurrentUser();
        user.getUserProfile().setUpdatedBy(currentUser.getUsername());
        user.getUserProfile().setUpdatedOn(ZonedDateTime.now());

        checkUserReadPermission(user);
        UserProfile profile = null;
        Optional<UserProfile> profileOptional = userProfileDao.findByUser(user);
        if(profileOptional.isPresent()) {
            profile = profileOptional.get();
            userProfileDao.delete(profile);
        }
        //        .orElseThrow(() -> new UserNotFoundException("User profile not found for user with UUID: " + uuid));

       
        userDao.delete(user);
    }

    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        UserPrincipal userPrincipal = SecurityUtils.getCurrentUser();
        if (userPrincipal == null) {
            User user = new User();
            user.setRole(Role.PUBLIC);
            return user;
        }

        Optional<User> optUser = userDao.findByUsername(userPrincipal.getUsername());
        if (optUser.isEmpty()) {
            throw new UserNotFoundException("User not found with username: " + userPrincipal.getUsername());
        }
        return optUser.get();
    }

    public Integer getCurrentUserStateId() {
        User currentUser = getCurrentUser();
        // Assuming the User entity has a stateId field
        return currentUser.getUserProfile().getState().getExtId();
    }

    public UUID getCurrentUserRsetiId() {
        User currentUser = getCurrentUser();
        // Assuming the User entity has a rsetiId field
        return currentUser.getUserProfile().getRseti().getUuid();
    }

    @Transactional
    public User updateUserProfile(String newEmail) {
        User currentUser = getCurrentUser();
        currentUser.setEmail(newEmail);
        return userDao.save(currentUser);
    }

    @Transactional    
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public void resetPassword(UUID uuid, String newPassword) {
        User user = userDao.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found with UUID: " + uuid));
        
        User currentUser = this.getCurrentUser();
        user.getUserProfile().setUpdatedBy(currentUser.getUsername());
        user.getUserProfile().setUpdatedOn(ZonedDateTime.now());

        checkUserReadPermission(user);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(ZonedDateTime.now());
        userDao.save(user);
    }

    @Transactional    
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public void resetPassword(JSONObject pwdJson) {
        if (!pwdJson.has("oldPwd") || !pwdJson.has("newPwd")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing oldPwd or newPwd in the request");
        }
        
        
        User currentUser = getCurrentUser();

        
        String oldPwdSupplied = pwdJson.getString("oldPwd");
        String newPwdSupplied = pwdJson.getString("newPwd");

        System.out.println("newPwdSupplied " + newPwdSupplied);
      //  String oldPwdEncrypt = passwordEncoder.encode(oldPwdSupplied);

        //System.out.println(oldPwdEncrypt);
        System.out.println(currentUser.getPassword());
        if(!passwordEncoder.matches(oldPwdSupplied,currentUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password not correct"); 
        }
        
        
        currentUser.setPassword(passwordEncoder.encode(newPwdSupplied));
        currentUser.setUpdatedAt(ZonedDateTime.now());
        currentUser.setCreatedAt(ZonedDateTime.now());
     // Ensure user profile exists before updating
        if (currentUser.getUserProfile() != null) {
            currentUser.getUserProfile().setUpdatedBy(currentUser.getUsername());
        }
        // Set createdBy only if it's null (preserving original creator)
        if (currentUser.getUserProfile().getCreatedBy() == null) {
            currentUser.getUserProfile().setCreatedBy(currentUser.getUsername());
        }
        userDao.save(currentUser);
    }


    private UserDto mapUserToDto(User user, UserProfile profile) {
        UserDto dto = new UserDto();
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoleId(user.getRole().getNumber());
        if(profile != null) {
            dto.setName(profile.getName());
            dto.setExtId(profile.getExtId());
            dto.setDesignation(profile.getDesignation());
            dto.setContactNumber(profile.getContactNumber());
            dto.setPermanentAddr(profile.getPermanentAddr());
            dto.setCurrentAddr(profile.getCurrentAddr());
            dto.setStateId(profile.getState() != null ? profile.getState().getExtId() : null);
            dto.setRsetiId(profile.getRseti() != null ? profile.getRseti().getUuid() : null);
            dto.setPhotoUrl(profile.getPhotoUrl());
        }
        
        return dto;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public UserDto getUserByUsername(String username) {
        try {
            User user = userDao.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

            UserProfile profile = userProfileDao.findByUser(user)
                .orElse(null);

            return userMapper.mapUserToDto(user, profile);
        } catch (Exception e) {
            logger.error("Error in getUserByUsername for username: " + username, e);
            throw e;
        }
    }
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'NAR_STAFF', 'STATE_ADMIN', 'STATE_STAFF', 'RSETI_ADMIN', 'RSETI_STAFF')")
    public UserDto getUserByEmail(String email) {
        try {
            User user = userDao.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            UserProfile profile = userProfileDao.findByUser(user)
                .orElse(null);

            return userMapper.mapUserToDto(user, profile);
        } catch (UserNotFoundException e) {
            logger.warn("User not found with email: " + email);
            throw e;
        } catch (Exception e) {
            logger.error("Error in getUserByEmail for email: " + email, e);
            throw e;
        }
    }
    private void checkUserReadPermission(User user) {
        User currentUser = getCurrentUser();
        Role userRole = currentUser.getRole();

        switch (userRole) {
            case SUPER_ADMIN:
            case NAR_ADMIN:
            case NAR_STAFF:
                // These roles have access to all RSETIs
                break;
            case STATE_ADMIN:
            case STATE_STAFF:
                if (user.getUserProfile().getState().getExtId() != currentUser.getUserProfile().getState().getExtId()) {
                    throw new AccessDeniedException("You don't have permission to access this RSETI");
                }
                break;
            case RSETI_ADMIN:
            case RSETI_STAFF:
                // Check if the user being created has an RSETI assigned
                if (user.getUserProfile().getRseti() == null) {
                    throw new AccessDeniedException("RSETI must be specified for new user");
                }
                // Allow RSETI_ADMIN to create users for their own RSETI
                if (!user.getUserProfile().getRseti().getUuid().equals(currentUser.getUserProfile().getRseti().getUuid())) {
                    throw new AccessDeniedException("You don't have permission to create users for other RSETIs");
                }
                break;
            case PUBLIC:
            default:
                throw new AccessDeniedException("You don't have permission to access RSETI data");
        }
    }
}
package com.tl.reap_admin_api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tl.reap_admin_api.dao.StateDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public UserDto createUser(UserDto userDto, String password) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.fromNumber(userDto.getRoleId()));
        user = userDao.save(user);

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
        profile.setRseti(getRseti(userDto));
        
        profile.setPhotoUrl(userDto.getPhotoUrl());
        profile.setCreatedBy(getCurrentUser().getUsername());
        profile.setUpdatedBy(getCurrentUser().getUsername());
        userProfileDao.save(profile);

        return mapUserToDto(user, profile);
    }

    @Transactional
    public UserDto uploadProfilePic(UUID userUuid, MultipartFile file) throws IOException {
        try {
            User user = userDao.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found with UUID: " + userUuid));

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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public UserDto getUserByUuid(UUID uuid) {
        User user = userDao.findByUuid(uuid)
        .orElseThrow(() -> new UserNotFoundException("User not found with UUID: " + uuid));
        UserProfile profile = null;
        Optional<UserProfile> profileOptional = userProfileDao.findByUser(user);
        if(profileOptional.isPresent()) {
            profile = profileOptional.get();
        }
               // .orElseThrow(() -> new UserNotFoundException("User profile not found for user with UUID: " + uuid));
        return mapUserToDto(user, profile);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public List<UserDto> getAllUsers() {
        List<User> users = userDao.findAllWithProfiles();
        return users.stream()
                .map(userMapper::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public UserDto updateUser(UUID uuid, UserDto userDto) {
        User user = userDao.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found with UUID: " + uuid));
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public void deleteUser(UUID uuid) {
        User user = userDao.findByUuid(uuid)
        .orElseThrow(() -> new UserNotFoundException("User not found with UUID: " + uuid));
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
            throw new RuntimeException("No authenticated user found");
        }

        Optional<User> optUser = userDao.findByUsername(userPrincipal.getUsername());
        if (optUser.isEmpty()) {
            throw new UserNotFoundException("User not found with username: " + userPrincipal.getUsername());
        }
        return optUser.get();
    }

    @Transactional
    public User updateUserProfile(String newEmail) {
        User currentUser = getCurrentUser();
        currentUser.setEmail(newEmail);
        return userDao.save(currentUser);
    }

    @Transactional
    public void resetPassword(UUID uuid, String newPassword) {
        User user = userDao.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found with UUID: " + uuid));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(ZonedDateTime.now());
        userDao.save(user);
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public UserDto getUserByUsername(String username) {
        User user = userDao.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        UserProfile profile = null;
        Optional<UserProfile> profileOptional = userProfileDao.findByUser(user);
        if(profileOptional.isPresent()) {
            profile = profileOptional.get();
        }
        return mapUserToDto(user, profile);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public UserDto getUserByEmail(String email) {
        User user = userDao.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        UserProfile profile = null;
        Optional<UserProfile> profileOptional = userProfileDao.findByUser(user);
        if(profileOptional.isPresent()) {
            profile = profileOptional.get();
        }
        return mapUserToDto(user, profile);
    }
}
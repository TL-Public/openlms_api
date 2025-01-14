package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.UserDto;
import com.tl.reap_admin_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/apis/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto,"12345678");
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/{uuid}/image")
    public ResponseEntity<UserDto> uploadProfilePic(@PathVariable UUID uuid, @RequestParam("file") MultipartFile file) {
        try {
            UserDto updatedCourse = userService.uploadProfilePic(uuid, file);
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<UserDto> getUserByUuid(@PathVariable UUID uuid) {
        UserDto user = userService.getUserByUuid(uuid);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        UserDto user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID uuid, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(uuid, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid) {
        userService.deleteUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{uuid}/reset-password")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN')")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID uuid, @RequestBody String newPassword) {
        userService.resetPassword(uuid, newPassword);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/usernames/{username}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/emails/{email}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NAR_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
}
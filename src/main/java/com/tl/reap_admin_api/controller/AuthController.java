package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.LoginRequest;
import com.tl.reap_admin_api.dto.SignUpRequest;
import com.tl.reap_admin_api.dto.UserDto;
import com.tl.reap_admin_api.security.JwtTokenProvider;
import com.tl.reap_admin_api.security.UserPrincipal;
import com.tl.reap_admin_api.service.KPointService;
import com.tl.reap_admin_api.service.UserService;
import com.tl.reap_admin_api.util.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tl.reap_admin_api.dto.ApiResponse;
import com.tl.reap_admin_api.dto.JwtAuthenticationResponse;
import com.tl.reap_admin_api.model.GlobalAPIResponse;
import com.tl.reap_admin_api.model.TokenRequest;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/apis/v1/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
     private final KPointService kPointService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService,
                          PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider,  KPointService kPointService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.kPointService = kPointService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateToken(authentication);

            UserPrincipal userPrincipal = SecurityUtils.getCurrentUser();
            if (userPrincipal == null) {
                throw new RuntimeException("No authenticated user found");
            } 
           
           return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, userPrincipal.getRole().getNumber()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid username or password"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, "Username is already taken!"));
        }

        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, "Email Address already in use!"));
        }

        // Creating user's account
        UserDto userDto = new UserDto();
        userDto.setUsername(signUpRequest.getUsername());
        userDto.setEmail(signUpRequest.getEmail());
        userDto.setRoleId(signUpRequest.getRoleId()); // Default role, adjust as needed
       
       
        try {
            userDto = userService.createUser(userDto, signUpRequest.getPassword());
            return ResponseEntity.created(null).body(userDto);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error occurred while registering the user"));
        }
    }

    @PostMapping("/kpoint/token")
	public ResponseEntity<GlobalAPIResponse> generateToken(@RequestBody TokenRequest tokenRequest) {
		String token;
		try {
			token = kPointService.getAuthtoken(tokenRequest.getEmail(), tokenRequest.getDisplayName());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new GlobalAPIResponse("error",  "Internal server error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		GlobalAPIResponse globalAPIResponse = new GlobalAPIResponse("success",
				"Token generated successfully", token);
		return new ResponseEntity<>(globalAPIResponse, HttpStatus.OK);
	}
}
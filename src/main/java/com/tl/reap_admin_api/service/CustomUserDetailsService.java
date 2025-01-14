package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.UserDao;
import com.tl.reap_admin_api.dao.TraineeCredentialDao;
import com.tl.reap_admin_api.model.User;
import com.tl.reap_admin_api.security.UserPrincipal;
import com.tl.reap_admin_api.model.Role;
import com.tl.reap_admin_api.model.TraineeCredential;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.tl.reap_admin_api.exception.UserNotFoundException;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;
    private final TraineeCredentialDao traineeDao;

    @Autowired
    public CustomUserDetailsService(UserDao userDao, TraineeCredentialDao traineeDao) {
        this.userDao = userDao;
        this.traineeDao = traineeDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Optional<User> user = userDao.findByUsername(username);
            if(user.isEmpty()) {
                throw new UserNotFoundException("User not found with username: " + username);
            }
            User userEntity = user.get();
            return UserPrincipal.create(userEntity);
        } catch (UserNotFoundException e) {
            try {
                Optional<TraineeCredential> optionalTrainee = traineeDao.findByUsername(username);
                if(optionalTrainee.isEmpty()) {
                    throw new UserNotFoundException("User not found with username: " + username);
                }
                TraineeCredential trainee = optionalTrainee.get();
                User user = new User();
                user.setUuid(trainee.getUuid());
                user.setUsername(trainee.getUsername());
                user.setEmail(trainee.getEmail());
                user.setPassword(trainee.getPassword());
                user.setRole(Role.fromNumber(9));
                return UserPrincipal.create(user);
            } catch (UserNotFoundException ex) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }
        }
    }
}
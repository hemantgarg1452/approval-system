package com.company.approval_system.security;

import com.company.approval_system.entity.User;
import com.company.approval_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Custom implementation of spring security's userDetailsService.
//Loads user from db and concerts to UserPrincipal for authentication.
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(()->new UsernameNotFoundException("user not found with email: "+ username));

        return UserPrincipal.create(user);
    }

    // Load user by ID (used in JWT auth filters)
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()->new UsernameNotFoundException("User not found with id: "+ id));

        return UserPrincipal.create(user);
    }
}

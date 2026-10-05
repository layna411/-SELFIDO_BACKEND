package com.simats.selfora.security;

import com.simats.selfora.entity.User;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        String cleanInput = usernameOrEmail != null ? usernameOrEmail.trim() : "";
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(cleanInput, cleanInput)
                .orElseGet(() -> userRepository.findByUsernameOrEmail(cleanInput, cleanInput)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + cleanInput)));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.getIsActive() != null ? user.getIsActive() : true,
                true,
                true,
                true,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );
    }
}

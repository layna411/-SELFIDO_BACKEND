package com.simats.selfora.config;

import com.simats.selfora.entity.Role;
import com.simats.selfora.entity.User;
import com.simats.selfora.repository.RoleRepository;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Ensure Roles exist
        Role adminRole = roleRepository.findByName("ROLE_SUPER_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_SUPER_ADMIN", "Super Administrator")));

        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_THERAPIST", "Therapist")));

        Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_CAREGIVER", "Caregiver")));

        // Guarantee active users with BCrypt password "password123"
        ensureUser("admin", "password123", "System Administrator", "admin@selfora.org", adminRole);
        ensureUser("therapist1", "password123", "Dr. Sarah Jenkins (OT)", "sarah.jenkins@selfora.org", therapistRole);
        ensureUser("caregiver1", "password123", "Priya Sharma (Parent)", "priya.sharma@example.com", caregiverRole);
    }

    private void ensureUser(String username, String rawPassword, String fullName, String email, Role role) {
        Optional<User> existingUser = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Re-encode password dynamically using the runtime PasswordEncoder bean
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setIsActive(true);
            user.setMustChangePassword(false);
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            if (user.getRoles().stream().noneMatch(r -> r.getName().equals(role.getName()))) {
                user.getRoles().add(role);
            }
            userRepository.save(user);
        } else {
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone("+919999999999");
            user.setIsActive(true);
            user.setMustChangePassword(false);
            user.setRoles(new HashSet<>());
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }
}

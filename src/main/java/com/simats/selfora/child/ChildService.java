package com.simats.selfora.child;

import com.simats.selfora.dto.ChildDto;
import com.simats.selfora.entity.Child;
import com.simats.selfora.entity.User;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.ChildRepository;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChildService {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ChildDto> getAssignedChildrenForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Child> children;
        boolean isTherapist = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_THERAPIST"));
        if (isTherapist) {
            children = childRepository.findByTherapistId(user.getId());
        } else {
            children = childRepository.findByCaregiverId(user.getId());
        }

        return children.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ChildDto getChildById(Long id) {
        validateChildAccessForCurrentUser(id);
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with ID: " + id));
        return mapToDto(child);
    }

    public boolean validateChildAccessForCurrentUser(Long childId) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("User is not authenticated");
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }

        boolean isTherapist = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_THERAPIST"));
        if (isTherapist) {
            boolean hasAccess = childRepository.existsByChildIdAndTherapistId(childId, user.getId());
            if (!hasAccess) {
                throw new org.springframework.security.access.AccessDeniedException("Therapist " + username + " is not assigned to child ID: " + childId);
            }
            return true;
        }

        boolean isCaregiver = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_CAREGIVER"));
        if (isCaregiver) {
            boolean hasAccess = childRepository.existsByChildIdAndCaregiverId(childId, user.getId());
            if (!hasAccess) {
                throw new org.springframework.security.access.AccessDeniedException("Caregiver " + username + " is not linked to child ID: " + childId);
            }
            return true;
        }

        throw new org.springframework.security.access.AccessDeniedException("User role is not authorized to access child records");
    }

    public ChildDto createChild(ChildDto dto) {
        Child child = Child.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .avatarUrl(dto.getAvatarUrl() != null ? dto.getAvatarUrl() : "ic_avatar_default")
                .diagnosisNotes(dto.getDiagnosisNotes())
                .isActive(true)
                .build();

        Child saved = childRepository.save(child);
        return mapToDto(saved);
    }

    private ChildDto mapToDto(Child c) {
        return ChildDto.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .dateOfBirth(c.getDateOfBirth())
                .gender(c.getGender())
                .avatarUrl(c.getAvatarUrl())
                .diagnosisNotes(c.getDiagnosisNotes())
                .isActive(c.getIsActive())
                .build();
    }
}

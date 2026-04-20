package com.AuthSecurity.AuthenticationApp.Services;

import com.AuthSecurity.AuthenticationApp.Dtos.RoleDTO;
import com.AuthSecurity.AuthenticationApp.Dtos.UserDTO;
import com.AuthSecurity.AuthenticationApp.Entities.Provider;
import com.AuthSecurity.AuthenticationApp.Entities.Role;
import com.AuthSecurity.AuthenticationApp.Entities.Users;
import com.AuthSecurity.AuthenticationApp.Exceptions.ResourceNotFoundException;
import com.AuthSecurity.AuthenticationApp.Repositories.RoleRepo;
import com.AuthSecurity.AuthenticationApp.Repositories.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EmailService emailService;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO create(UserDTO userDTO) {
        // Validation guards
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required");
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank())
            throw new IllegalArgumentException("Password is required");
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank())
            throw new IllegalArgumentException("Username is required");

        // ── BUG FIX: was checking email twice, now checks username correctly ──
        if (userRepo.existsByEmail(userDTO.getEmail()))
            throw new IllegalArgumentException("Email already exists");
        if (userRepo.existsByUsername(userDTO.getUsername()))
            throw new IllegalArgumentException("Username already exists");

        Users user = modelMapper.map(userDTO, Users.class);
        user.setProvider(userDTO.getProvider() == null ? Provider.LOCAL : userDTO.getProvider());

        // Resolve roles from DB
        Set<Role> roles = userDTO.getRoles().stream()
                .map(r -> roleRepo.findByName(r.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + r.getName())))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        Users saved = userRepo.save(user);

        // Send welcome email (non-blocking — ignore failure in prod)
        try {
            emailService.emailSend(saved);
        } catch (Exception ignored) {
            // Email failure should NOT fail registration
        }

        return modelMapper.map(saved, UserDTO.class);
    }

    public UserDTO getUserByEmail(String email) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return modelMapper.map(user, UserDTO.class);
    }

    public UserDTO updateUser(UserDTO userDTO, Long userId) {
        Users existing = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (userDTO.getName() != null) existing.setName(userDTO.getName());
        if (userDTO.getImage() != null) existing.setImage(userDTO.getImage());
        if (userDTO.getEmail() != null) existing.setEmail(userDTO.getEmail());
        // Encode password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        return modelMapper.map(userRepo.save(existing), UserDTO.class);
    }

    public void deleteUser(Long userId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepo.delete(user);
    }

    public UserDTO getUserById(Long userId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return modelMapper.map(user, UserDTO.class);
    }

    @Transactional
    public Iterable<UserDTO> getAllUsers() {
        return userRepo.findAll().stream()
                .map(u -> modelMapper.map(u, UserDTO.class))
                .toList();
    }
}

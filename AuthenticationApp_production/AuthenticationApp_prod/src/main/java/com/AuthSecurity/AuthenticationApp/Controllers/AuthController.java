package com.AuthSecurity.AuthenticationApp.Controllers;

import com.AuthSecurity.AuthenticationApp.Dtos.LoginRequest;
import com.AuthSecurity.AuthenticationApp.Dtos.RoleDTO;
import com.AuthSecurity.AuthenticationApp.Dtos.TokenResponse;
import com.AuthSecurity.AuthenticationApp.Dtos.UserDTO;
import com.AuthSecurity.AuthenticationApp.Entities.Users;
import com.AuthSecurity.AuthenticationApp.Repositories.UserRepo;
import com.AuthSecurity.AuthenticationApp.Security.JwtService;
import com.AuthSecurity.AuthenticationApp.Services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    /** Register a standard user */
    @PostMapping("/register/user")
    public ResponseEntity<UserDTO> userRegister(@Valid @RequestBody UserDTO userDTO) {
        userDTO.setRoles(Set.of(new RoleDTO(null, "ROLE_USER")));
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDTO));
    }

    /** Register an admin user */
    @PostMapping("/register/admin")
    public ResponseEntity<UserDTO> adminRegister(@Valid @RequestBody UserDTO userDTO) {
        userDTO.setRoles(Set.of(new RoleDTO(null, "ROLE_ADMIN")));
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDTO));
    }

    /** Unified login endpoint – works for both USER and ADMIN */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        authenticate(loginRequest);
        Users users = userRepo.findByEmailOrUsername(loginRequest.identifier(), loginRequest.identifier())
                .orElseThrow(() -> new BadCredentialsException("Invalid Credentials"));
        if (!users.isEnable()) throw new DisabledException("Account is disabled. Please contact support.");
        String accessToken = jwtService.generateToken(users);
        TokenResponse tokenResponse = TokenResponse.of(
                accessToken, "", jwtService.getAccessItlSeconds(),
                modelMapper.map(users, UserDTO.class));
        return ResponseEntity.ok(tokenResponse);
    }

    /** Keep backwards-compatible endpoints */
    @PostMapping("/login/admin")
    public ResponseEntity<TokenResponse> adminLogin(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    @PostMapping("/login/user")
    public ResponseEntity<TokenResponse> userLogin(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    /** Simple logout hint (stateless – client should discard token) */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully. Please discard your token."));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void authenticate(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.identifier(), loginRequest.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}

package com.AuthSecurity.AuthenticationApp.Controllers;

import com.AuthSecurity.AuthenticationApp.Dtos.*;
import com.AuthSecurity.AuthenticationApp.Entities.RefreshToken;
import com.AuthSecurity.AuthenticationApp.Entities.Users;
import com.AuthSecurity.AuthenticationApp.Repositories.RefreshRepo;
import com.AuthSecurity.AuthenticationApp.Repositories.UserRepo;
import com.AuthSecurity.AuthenticationApp.Security.JwtService;
import com.AuthSecurity.AuthenticationApp.Services.AuthService;
import com.AuthSecurity.AuthenticationApp.Services.CookiesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.CloseableThreadContext;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

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
    private final RefreshRepo refreshRepo;
    private final CookiesService cookiesService;

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
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        authenticate(loginRequest);
        Users users = userRepo.findByEmailOrUsername(loginRequest.identifier(), loginRequest.identifier())
                .orElseThrow(() -> new BadCredentialsException("Invalid Credentials"));
        if (!users.isEnable()) throw new DisabledException("Account is disabled. Please contact support.");
        String jti= UUID.randomUUID().toString();
        RefreshToken refresh=new RefreshToken(jti, LocalDateTime.now(),LocalDateTime.now().plusSeconds(jwtService.getRefreshItlSeconds()),false,users,null);
        refreshRepo.save(refresh);

        String accessToken = jwtService.generateToken(users);
        String refreshToken=jwtService.generateRefreshToken(users,jti);
        //cookieService
        cookiesService.attachRefreshCookie(refreshToken,response,(int)jwtService.getRefreshItlSeconds());

        TokenResponse tokenResponse = TokenResponse.of(
                accessToken, refreshToken, jwtService.getAccessItlSeconds(),
                modelMapper.map(users, UserDTO.class));
        return ResponseEntity.ok(tokenResponse);
    }

    /** Keep backwards-compatible endpoints */
    @PostMapping("/login/admin")
    public ResponseEntity<TokenResponse> adminLogin(@RequestBody LoginRequest loginRequest,HttpServletResponse response) {
        return login(loginRequest,response);
    }

    @PostMapping("/login/user")
    public ResponseEntity<TokenResponse> userLogin(@RequestBody LoginRequest loginRequest,HttpServletResponse response) {
        return login(loginRequest,response);
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
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body, HttpServletResponse response, HttpServletRequest request){
        String rft = readRefreshToken(body, request).orElseThrow(()->new BadCredentialsException("Invalid or expired refresh token"));
        if(!jwtService.isRefreshToken(rft)){
            throw new BadCredentialsException("Invalid refresh token type..");
        }
        String jti=jwtService.getJti(rft);
        String email=jwtService.getUserEmail(rft);
        RefreshToken storedRToken=refreshRepo.findByJti(jti).orElseThrow(()->new BadCredentialsException("Invalid token."));
        if(storedRToken.isRevoked()){
            throw new BadCredentialsException("Refresh Token Revoked.");
        }
        if(storedRToken.getExpiredAt().isBefore(LocalDateTime.now())) throw new BadCredentialsException("Refresh Token Expired");
        if(!storedRToken.getUser().getEmail().equals(email)) throw new BadCredentialsException("Email mismatch and not belong to this user");
        storedRToken.setRevoked(true);
        String newJti=UUID.randomUUID().toString();
        storedRToken.setReplacedByToken(newJti);
        refreshRepo.save(storedRToken);
        Users u=storedRToken.getUser();
        RefreshToken newRefresh=new RefreshToken(newJti, LocalDateTime.now()
                ,LocalDateTime.now().plusSeconds(jwtService.getRefreshItlSeconds())
                ,false,u,null);
        refreshRepo.save(newRefresh);

        String newAccessToken = jwtService.generateToken(u);
        String newRefreshToken=jwtService.generateRefreshToken(u,newJti);
        //cookieService
        cookiesService.attachRefreshCookie(newRefreshToken,response,(int)jwtService.getRefreshItlSeconds());

        TokenResponse tokenResponse = TokenResponse.of(
                newAccessToken, newRefreshToken, jwtService.getAccessItlSeconds(),
                modelMapper.map(u, UserDTO.class));
        return ResponseEntity.ok(tokenResponse);

    }


    private Optional<String> readRefreshToken(RefreshTokenRequest body, HttpServletRequest request) {
        if(request.getCookies()!=null){
            Optional<String > fromRequest= Arrays.stream(request.getCookies())
                    .filter(c->cookiesService.getRefreshCookiesTokenName().equals(c.getName()))
                    .map(c->c.getValue())
                    .filter(v->!v.isBlank())
                    .findFirst();
            if(fromRequest.isPresent()) return fromRequest;
        }
        if(body!=null&&body.refreshToken()!=null&&body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken());
        }
        return Optional.empty();
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,HttpServletResponse response){
        readRefreshToken(null,request).ifPresent((token)->{
                if(jwtService.isRefreshToken(token)){
                    String jti=jwtService.getJti(token);
                    refreshRepo.findByJti(jti).ifPresent(rt->{
                        rt.setRevoked(true);
                        refreshRepo.save(rt);
                    });
                }
        });

        cookiesService.clearRefreshCookie(response);
        cookiesService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}

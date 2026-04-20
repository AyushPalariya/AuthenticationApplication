package com.AuthSecurity.AuthenticationApp.Security;


import com.AuthSecurity.AuthenticationApp.Entities.Role;
import com.AuthSecurity.AuthenticationApp.Entities.Users;
import com.AuthSecurity.AuthenticationApp.Repositories.UserRepo;
import com.AuthSecurity.AuthenticationApp.Services.CustomUserDetailService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
public class JwtService {

    private String key;
    private final Key SECRET_KEY;
    private long accessItlSeconds;
    private long refreshItlSeconds;
    private String issuer;

    public JwtService(@Value("${security.jwt.secret}") String key, @Value("${security.jwt.access-ttl-seconds}") long accessItlSeconds,
                      @Value("${security.jwt.refresh-ttl-seconds}") long refreshItlSeconds, @Value("${security.jwt.issuer}") String issuer) {
        this.key = key;
        SECRET_KEY = Keys.hmacShaKeyFor(key.getBytes());
        this.accessItlSeconds = accessItlSeconds;
        this.refreshItlSeconds = refreshItlSeconds;
        this.issuer = issuer;
    }

    public String generateToken(Users user) {
        List<String> roles = user.getRoles()==null?List.of():user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getEmail())  // ✔ only email (unique identifier)
                .setIssuedAt(new Date())
                .setIssuer(issuer)
                .setExpiration(new Date(System.currentTimeMillis()+ accessItlSeconds*1000))
                .addClaims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "typ", "access"
                ))
                .signWith(SECRET_KEY) // make sure key is correct type
                .compact();
    }

    public String generateRefreshToken(Users user, String jti) {
        return Jwts.builder()
                .setId(jti)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setIssuer(issuer)
                .setExpiration(new Date(System.currentTimeMillis() + refreshItlSeconds*100))
                .addClaims(Map.of(
                        "typ", "refresh"
                ))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
    }

    public boolean isAccessToken(String token){
        Claims claims = parse(token).getBody();
        return "access".equals(claims.get("typ"));
    }

    public boolean isRefreshToken(String token){
        Claims claims =  parse(token)
                .getBody();
        return "refresh".equals(claims.get("typ"));
    }
}

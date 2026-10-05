package com.AuthSecurity.AuthenticationApp.Repositories;

import com.AuthSecurity.AuthenticationApp.Entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshRepo extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByJti(String jti);
}

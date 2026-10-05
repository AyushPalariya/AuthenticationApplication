package com.AuthSecurity.AuthenticationApp.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="refresh_token",indexes = {
        @Index(name="refresh_index",unique = true,columnList = "jti"),
        @Index(name="user_index",columnList = "user_id")
})
@Data
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false,updatable = false)
    private String jti;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private boolean revoked;//false means revoke kr sakta hu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",referencedColumnName = "id")
    private Users user;
    private String replacedByToken;

    public RefreshToken(String jti, LocalDateTime createdAt, LocalDateTime expiredAt, boolean revoked, Users user, String replacedByToken) {
        this.jti = jti;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.revoked = revoked;
        this.user = user;
        this.replacedByToken = replacedByToken;
    }
}

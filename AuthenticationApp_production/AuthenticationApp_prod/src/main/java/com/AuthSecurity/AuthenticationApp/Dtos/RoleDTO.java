package com.AuthSecurity.AuthenticationApp.Dtos;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RoleDTO {
    private Long id;
    private String name;
}

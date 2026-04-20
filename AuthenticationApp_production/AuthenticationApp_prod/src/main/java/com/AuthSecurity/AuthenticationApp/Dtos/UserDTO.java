package com.AuthSecurity.AuthenticationApp.Dtos;

import com.AuthSecurity.AuthenticationApp.Entities.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String image;

    private boolean enable = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Provider provider;

    private Set<RoleDTO> roles;
}

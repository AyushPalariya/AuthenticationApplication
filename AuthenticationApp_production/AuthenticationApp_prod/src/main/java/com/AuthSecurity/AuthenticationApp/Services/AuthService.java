package com.AuthSecurity.AuthenticationApp.Services;

import com.AuthSecurity.AuthenticationApp.Dtos.UserDTO;
import com.AuthSecurity.AuthenticationApp.Entities.Role;
import com.AuthSecurity.AuthenticationApp.Repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;


    public UserDTO registerUser(UserDTO userDTO){
        return userService.create(userDTO);
    }
}

package com.AuthSecurity.AuthenticationApp.Services;

import com.AuthSecurity.AuthenticationApp.Entities.Users;
import com.AuthSecurity.AuthenticationApp.Exceptions.ResourceNotFoundException;
import com.AuthSecurity.AuthenticationApp.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Users users = userRepo.findByEmailOrUsername(identifier,identifier).orElseThrow(() -> new ResourceNotFoundException("Invalid Username and Password!!"));
        return new User(users.getEmail(), users.getPassword(),true,true,true,true,
                users.getRoles().stream().map(role->new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet()));
    }
}

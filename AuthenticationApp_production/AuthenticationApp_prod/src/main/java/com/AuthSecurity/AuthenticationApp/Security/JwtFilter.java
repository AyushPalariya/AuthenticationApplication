package com.AuthSecurity.AuthenticationApp.Security;

import com.AuthSecurity.AuthenticationApp.Entities.Users;
import com.AuthSecurity.AuthenticationApp.Repositories.UserRepo;
import com.AuthSecurity.AuthenticationApp.Services.CustomUserDetailService;
import com.AuthSecurity.AuthenticationApp.Services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    //after successful login this work when subsequent request comes
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private static final Logger logger=LoggerFactory.getLogger(JwtFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        logger.info("Authorization header : {}", header);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if(!jwtService.isAccessToken(token)){
                    filterChain.doFilter(request,response);
                    return ;
                }
                Jws<Claims> parse = jwtService.parse(token);
                Claims body = parse.getBody();
                String email = body.getSubject();
                    //user get from database
                userRepo.findByEmail(email).ifPresent(user ->{
                    //check for user enable or not
                    if(user.isEnable()){
                        //get roles of user
                        List<GrantedAuthority> authorities=user.getRoles()==null?List.of():user.getRoles().stream().map(r->new SimpleGrantedAuthority(r.getName()))
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(user.getEmail()  ,null,authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        //set context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }

                } );



            } catch (ExpiredJwtException e) {
                request.setAttribute("error","token expired");
                System.out.println("token expired " + e.getMessage());
            } catch (SignatureException e) {
                request.setAttribute("error","Invalid Token");
                System.out.println("Signature invalid " + e.getMessage());
            } catch (Exception e) {
                request.setAttribute("error","Invalid Token");
                System.out.println("JWt exception " + e.getMessage());
            }

        }
        filterChain.doFilter(request,response);
    }
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        boolean b = request.getRequestURI().startsWith("/api/v1/auth");
        return b;
    }
}



package com.AuthSecurity.AuthenticationApp.Util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

//@Component
//public class JwtUtil {
//
//    private static final String SECRETE_KEY_STRING="Ayush@2003rtererwe**83452efftrr434er4fdfdfsewedfe43s3";
//    private final Key SECRETE_KEY= Keys.hmacShaKeyFor(SECRETE_KEY_STRING.getBytes());
//
//    public String generateToken(String username,String pass){
//        String token= Jwts.builder()
//                .setSubject(username).setSubject(pass)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis()+1000*300))
//                .signWith(SECRETE_KEY)
//                .compact();
//        System.out.println("generate token");
//        return token;
//    }
//
//    public boolean validate(String token){
//        try{
//            Jwts.parserBuilder().setSigningKey(SECRETE_KEY).build().parseClaimsJws(token);
//            return true;
//        }
//        catch (ExpiredJwtException e){
//            System.out.println("token expired "+e.getMessage());
//            return false;
//        }
//        catch (SignatureException e){
//            System.out.println("Signature invalid "+e.getMessage());
//            return false;
//        }
//        catch (Exception e){
//            System.out.println("JWt exception "+e.getMessage());
//            return false;
//        }
//    }
//}


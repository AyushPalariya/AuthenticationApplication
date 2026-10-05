package com.AuthSecurity.AuthenticationApp.Services;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Data
public class CookiesService {
    private final String refreshCookiesTokenName;
    private final boolean cookieSecure;

    private final boolean cookieHttpOnly;
    private final String cookieDomain;
    private final String cookieSameSite;

    public CookiesService(@Value("${security.jwt.refresh-token-cookie-name}") String refreshCookiesTokenName, @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
                          @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly, @Value("${security.jwt.cookie-domain}") String cookieDomain,
                          @Value("${security.jwt.cookie-same-site}") String cookieSameSite) {
        this.refreshCookiesTokenName = refreshCookiesTokenName;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }
    public void attachRefreshCookie(String rft, HttpServletResponse response,int maxAge){
       ResponseCookie.ResponseCookieBuilder responseCookieBuilder=ResponseCookie.from(refreshCookiesTokenName,rft).httpOnly(cookieHttpOnly).secure(cookieSecure)
               .path("/").sameSite(cookieSameSite)
               .maxAge(Duration.ofSeconds(maxAge));
       if(cookieDomain!=null&&!cookieDomain.isBlank()){
           responseCookieBuilder.domain(cookieDomain);
       }
       ResponseCookie responseCookie=responseCookieBuilder.build();
       response.addHeader(HttpHeaders.SET_COOKIE,responseCookie.toString());

    }
    public void clearRefreshCookie(HttpServletResponse response){
        ResponseCookie.ResponseCookieBuilder responseCookieBuilder=ResponseCookie.from(refreshCookiesTokenName,"").httpOnly(cookieHttpOnly).secure(cookieSecure)
                .path("/").sameSite(cookieSameSite)
                .maxAge(0);
        if(cookieDomain!=null&&!cookieDomain.isBlank()){
            responseCookieBuilder.domain(cookieDomain);
        }
        ResponseCookie responseCookie=responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE,responseCookie.toString());

    }


    public void addNoStoreHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}

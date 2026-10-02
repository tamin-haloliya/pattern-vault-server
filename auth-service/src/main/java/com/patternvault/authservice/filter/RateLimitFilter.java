package com.patternvault.authservice.filter;

import com.patternvault.authservice.service.RateLimitService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ConsumptionProbe probe;

        if(request.getServletPath().startsWith("/api/auth")){
            probe = rateLimitService.checkAuth(request.getRemoteAddr());
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                filterChain.doFilter(request, response);
                return;
            }
            probe = rateLimitService.checkUser(authentication.getName());
        }

        if(probe.isConsumed()){
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
    }
}

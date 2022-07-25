package com.server.springboot.security;

import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.service.RefreshTokenService;
import com.server.springboot.service.impl.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    public JwtRequestFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, UserRepository userRepository,
                            RefreshTokenService refreshTokenService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestHeader = request.getHeader("Authorization");
            String accessJwtToken = null;
            if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
                accessJwtToken = requestHeader.substring(7);
            }

            if (accessJwtToken != null && jwtUtils.validateJwtToken(accessJwtToken)) {
                String login = jwtUtils.geLoginFromJwtToken(accessJwtToken);

                if (userRepository.existsByUsernameOrEmail(login, login)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(login);

                    if (!refreshTokenService.existByUsername(userDetails.getUsername())) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is logged out");
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    LOGGER.info("---- Token is correct");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("---- User authentication error: {}", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
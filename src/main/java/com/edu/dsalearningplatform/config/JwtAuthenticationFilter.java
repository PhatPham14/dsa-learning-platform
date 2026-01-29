package com.edu.dsalearningplatform.config;

import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.security.jwt.JwtUtils;
import com.edu.dsalearningplatform.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] permitAllPaths = {
                "/api/auth/**",
                "/api/payments/**",
                "/swagger-ui/**", // Swagger UI
                "/v3/api-docs/**", // Swagger JSON
                "/error","/", "/courses", "/login", "/register",
                "/css/**", "/js/**", "/images/**",
                "/api/public/**", "/h2-console/**"
        };
        AntPathMatcher pathMatcher = new AntPathMatcher();
        String path = request.getRequestURI();
        return Arrays.stream(permitAllPaths)
                .anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        logger.debug("Processing request to: {}", request.getRequestURI());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtUtils.getUsernameFromJwtToken(token);
            logger.debug("Token found for user: {}", username);
        } else {
            // Check for token in cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token != null) {
                username = jwtUtils.getUsernameFromJwtToken(token);
                logger.debug("Token found in cookie for user: {}", username);
            } else {
                logger.debug("No valid Authorization header or accessToken cookie found");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Dòng này chỉ để lấy 'authorities' (quyền)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtils.validateJwtToken(token)) {
                // Tải đối tượng User (Entity) thật từ database
                User userEntity = userRepository.findByPhone(username)
                        .orElseThrow(() -> new RuntimeException("User not found in JWT filter"));

                // Tạo một token xác thực mới
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userEntity, // Đặt User làm Principal
                                null,
                                userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("User authenticated successfully with roles: {}", userDetails.getAuthorities());
            } else {
                logger.debug("Token validation failed");
            }
        }
        filterChain.doFilter(request, response);
    }
}
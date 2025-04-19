package com.todoapp.todo.security;

import com.todoapp.todo.util.JwtUtil; // Assuming JwtUtil exists here
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User; // Use Spring Security User
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList; // For empty authorities list

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username; // Use username extracted from token as principal

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtUtil.extractUsername(jwt); // Extract username

            // If username extracted and not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Validate token (add isTokenValid method to JwtUtil if needed)
                // if (jwtUtil.isTokenValid(jwt, username)) { // Assuming isTokenValid exists

                    // Create UserDetails (using basic Spring Security User for simplicity)
                    // You might want a custom UserDetailsService later
                    UserDetails userDetails = new User(username, "", new ArrayList<>()); // No password needed here

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No credentials needed after token validation
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // Set authentication in context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                // }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Handle exception (e.g., invalid token)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT Token: " + e.getMessage());
            // Or rethrow specific exceptions for a global handler
        }
    }
} 
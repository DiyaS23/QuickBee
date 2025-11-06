package com.quickbee.backend.security;

import com.quickbee.backend.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService; // This is your UserDetailsServiceImpl

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get the Authorization header from the request
        final String authHeader = request.getHeader("Authorization");

        final String jwt;
        final String userEmail;

        // 2. Check if the header is missing or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Pass to the next filter
            return; // Stop here
        }

        // 3. Extract the token (the part after "Bearer ")
        jwt = authHeader.substring(7);

        // 4. Extract the user's email from the token
        try {
            userEmail = jwtUtil.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            // Handle expired token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has expired");
            return;
        }

        // 5. Check if the user is ALREADY authenticated for this request
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Load the user from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 7. Validate the token
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 8. If valid, create an authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // We don't need credentials
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. THIS IS THE MAGIC STEP:
                // Set the user as authenticated in Spring Security's context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Pass the request to the next filter
        filterChain.doFilter(request, response);
    }
}
package com.barlog.loyaltyapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extragem header-ul Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Verificăm dacă header-ul există și începe cu "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Dacă nu, trecem la următorul filtru din lanț și încheiem execuția
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extragem token-ul (șirul de după "Bearer ")
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt); // Extragem email-ul din token

        // 4. Verificăm dacă avem un email și utilizatorul nu este deja autentificat
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Încărcăm detaliile utilizatorului din baza de date
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Verificăm dacă token-ul este valid
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Creăm un obiect de autentificare
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Nu avem nevoie de credențiale (parolă) aici
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 6. Actualizăm SecurityContextHolder cu noua autentificare
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Trecem la următorul filtru
        filterChain.doFilter(request, response);
    }
}
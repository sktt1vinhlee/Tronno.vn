package com.tronno.app.security;

import com.tronno.app.models.User;
import com.tronno.app.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserRepository users;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwtService.extractEmail(header.substring(7));
                User user = users.findByEmail(email).orElse(null);
                if (user == null) {
                    log.warn("Rejected JWT: subject is not a registered account");
                } else if (!user.isActive()) {
                    log.warn("Rejected JWT for blocked account");
                } else {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );

                    auth.setDetails(user.getId());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("Rejected expired JWT");
            } catch (io.jsonwebtoken.security.SignatureException e) {
                log.warn("Rejected JWT with invalid signature; check app.jwt.secret");
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
                log.warn("Rejected malformed JWT: {}", e.getMessage());
            }
        }
        chain.doFilter(req, res);
    }
}

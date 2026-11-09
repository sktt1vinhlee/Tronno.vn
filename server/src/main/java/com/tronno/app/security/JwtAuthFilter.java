package com.tronno.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tronno.app.models.User;
import com.tronno.app.repository.UserRepository;

/**
 * JwtAuthFilter
 *
 * Version 1.0
 *
 * Date: 09-11-2026
 *
 * Copyright VinhHL
 *
 * Modification Logs:
 * DATE                 AUTHOR          DESCRIPTION
 * -----------------------------------------------------------------------
 * 09-11-2026         VinhHL             Create
 */

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserRepository users;

    /**
     * Kiểm tra token trong header và thiết lập ngữ cảnh xác thực cho request.
     *
     * @param req request hiện tại
     * @param res response hiện tại
     * @param chain chuỗi filter
     * @throws ServletException nếu xử lý filter thất bại
     * @throws IOException nếu ghi response thất bại
     */
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

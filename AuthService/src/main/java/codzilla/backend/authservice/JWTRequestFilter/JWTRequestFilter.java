package codzilla.backend.authservice.JWTRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import codzilla.backend.authservice.JWTUtils.JWTUtils;

@Slf4j
@Component
public class JWTRequestFilter extends OncePerRequestFilter {
    JWTUtils jwtUtils;

    public JWTRequestFilter(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("In filter.");
        String token = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                log.info("Cookie: " + cookie.getName());
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null) {

            log.info("Got token.");
            try {
                String username = jwtUtils.getUsernameFromToken(token);
                List<String> roles = jwtUtils.getRolesFromToken(token);
                log.info("{} has jwt. His roles: {}", username, roles);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();


                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );


                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.error("dont auth user");
            }
        }

        filterChain.doFilter(request, response);

    }
}

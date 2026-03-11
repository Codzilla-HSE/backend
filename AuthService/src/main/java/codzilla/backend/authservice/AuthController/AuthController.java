package codzilla.backend.authservice.AuthController;

import codzilla.backend.authservice.JWTUtils.JWTUtils;
import codzilla.backend.authservice.User;
import codzilla.backend.authservice.UserService;
import codzilla.backend.authservice.config.Settings;
import codzilla.backend.authservice.dto.LoginRequestDTO;
import codzilla.backend.authservice.dto.LoginResponseDTO;
import codzilla.backend.authservice.dto.RegisterRequestDTO;
import codzilla.backend.authservice.dto.RegisterResponseDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JWTUtils jwtUtils;
    private final Settings settings;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authManager,
                          JWTUtils jwtUtils, Settings settings, UserService userService) {
        this.userService = userService;
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.settings = settings;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        log.info("Auth user by password...");
        log.info(request.email() + request.rawPassword());
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.rawPassword())
        );

        var accessToken = jwtUtils.generateAccessToken(auth);
        Cookie jwtCookie = new Cookie("jwt", accessToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) settings.getRefreshTokenTtl().toSeconds());
        response.addCookie(jwtCookie);

        var refreshToken = jwtUtils.generateRefreshToken(auth);
        Cookie refreshCookie = new Cookie("refresh_jwt", refreshToken);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge((int) settings.getRefreshTokenTtl().toSeconds());
        refreshCookie.setSecure(false);
        response.addCookie(refreshCookie);

        UserDetails user = userService.getByEmail(request.email());
        return ResponseEntity.ok(new LoginResponseDTO(user.getUsername()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody RegisterRequestDTO request) {
        userService.registerUser(request);
        return ResponseEntity.ok(new RegisterResponseDTO(request.username()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("refresh_jwt".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }

            if (refreshToken == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token in cookie.");
            if (!jwtUtils.validateToken(refreshToken))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is wrong.");

            String email = jwtUtils.getEmailFromToken(refreshToken);

            User user = userService.getByEmail(email);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );

            var accessToken = jwtUtils.generateAccessToken(auth);
            Cookie cookie = new Cookie("jwt", accessToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge((int) settings.getRefreshTokenTtl().toSeconds());
            response.addCookie(cookie);
            return ResponseEntity.ok("Jwt access was updated.");

        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No cookie here.");
    }

    @PostMapping("/create-admin")
    void createAdmin() {
        userService.createAdmin();
    }

}
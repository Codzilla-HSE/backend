package codzilla.backend.authservice.AuthController;

import codzilla.backend.authservice.JWTUtils.JWTUtils;
import codzilla.backend.authservice.config.Settings;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final InMemoryUserDetailsManager manager;
    private final PasswordEncoder encoder;
    private final JWTUtils jwtUtils;
    private final Settings settings;

    @Autowired
    public AuthController(AuthenticationManager authManager, InMemoryUserDetailsManager manager, PasswordEncoder encoder,
                          JWTUtils jwtUtils, Settings settings) {
        this.manager = manager;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.settings = settings;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        log.info("Auth user by password...");
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password)
        );

        var accessToken = jwtUtils.generateAccessToken(auth);
        Cookie jwtCookie = new Cookie("jwt", accessToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(/*(int) settings.getRefreshTokenTtl().toSeconds()*/ 3600);
        response.addCookie(jwtCookie);

        var refreshToken = jwtUtils.generateRefreshToken(auth);
        Cookie refreshCookie = new Cookie("refresh_jwt", refreshToken);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(/*(int) settings.getRefreshTokenTtl().toSeconds()*/ 3600);
        refreshCookie.setSecure(false);
        response.addCookie(refreshCookie);

        UserDetails user = (UserDetails) auth.getPrincipal();
        return ResponseEntity.ok("You are logged in! Your roles are " + user.getAuthorities().toString() + ".");
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
    public ResponseEntity<?> signUp(@RequestBody SignupRequest request) {

        UserDetails userDetails = User.
                withUsername(request.username).
                password(encoder.encode(request.password)).
                roles("USER").build();
        manager.createUser(userDetails);

        return ResponseEntity.ok("User created!");
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

            String username = jwtUtils.getUsernameFromToken(refreshToken);

            UserDetails userDetails = manager.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            var accessToken = jwtUtils.generateAccessToken(auth);
            Cookie cookie = new Cookie("jwt", accessToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(/*(int) settings.getRefreshTokenTtl().toSeconds()*/ 3600);
            response.addCookie(cookie);

            return ResponseEntity.ok("Jwt access was updated.");

        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No cookie here.");
    }

}

class LoginRequest {
    public String username;
    public String password;
}

class SignupRequest {
    public String username;
    public String password;
}

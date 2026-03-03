package codzilla.backend.authservice.AuthController;

import codzilla.backend.authservice.JWTUtils.JWTUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Autowired
    public AuthController(AuthenticationManager authManager, InMemoryUserDetailsManager manager, PasswordEncoder encoder) {
        this.manager = manager;
        this.encoder = encoder;
        this.authManager = authManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        log.info("Auth user by password...");
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password)
        );

        var token = JWTUtils.generateToken(auth);

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);


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

}

class LoginRequest {
    public String username;
    public String password;
}

class SignupRequest {
    public String username;
    public String password;
}

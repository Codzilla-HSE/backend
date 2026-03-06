package codzilla.backend.authservice.AdminController;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    String getAdminInfo() {
        return "Some admin info.";
    }
}

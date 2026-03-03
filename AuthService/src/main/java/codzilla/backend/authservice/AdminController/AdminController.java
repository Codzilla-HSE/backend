package codzilla.backend.authservice.AdminController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/")
    String getAdminInfo() {
        return "Some admin info.";
    }
}

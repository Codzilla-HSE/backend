package codzilla.backend.authservice;

import codzilla.backend.authservice.Exceptions.UserAlreadyExistsException;
import codzilla.backend.authservice.dto.ErrorResponseDTO;
import codzilla.backend.authservice.dto.RegisterRequestDTO;
import codzilla.backend.authservice.dto.RegisterResponseDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class RegisterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void testSignUpSimple() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("user", "email", "password");

        RegisterResponseDTO expectedResponse = new RegisterResponseDTO("user");

        mockMvc.perform(
                       post("/auth/signup")
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

        assert (userRepository.existsByEmail(request.email()));
        assert (userRepository.existsByUsername(request.username()));
        User addedUser = userRepository.findByEmail(request.email()).get();
        assert (addedUser.getAuthorities().stream().allMatch(s -> {
            return "USER".equals(s.getAuthority());
        }));
        assertThat(passwordEncoder.matches(
                request.rawPassword(),
                addedUser.getPassword()
        ));
    }

    @Test
    public void testUserAlreadyExists() throws Exception {
        RegisterRequestDTO request1 = new RegisterRequestDTO("user1", "email", "password");
        RegisterRequestDTO request2 = new RegisterRequestDTO("user2", "email", "password");

        ErrorResponseDTO expectedError = new ErrorResponseDTO(new UserAlreadyExistsException());

        mockMvc.perform(
                post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/auth/signup")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request2)))
               .andExpect(status().isConflict())
               .andExpect(content().json(objectMapper.writeValueAsString(expectedError)));
    }
}

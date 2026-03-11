package com.codzilla.backend.auth;

import com.codzilla.backend.auth.JWTUtils.JWTUtils;
import com.codzilla.backend.auth.dto.LoginRequestDTO;
import com.codzilla.backend.auth.dto.LoginResponseDTO;
import com.codzilla.backend.auth.dto.RegisterRequestDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class LoginIntegrationTest extends BaseIntegrationTest {

    @Autowired
    JWTUtils jwtUtils;

    @Test
    void testSimpleLogin() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("nick", "email", "password");
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isOk());

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                registerRequestDTO.email(),
                registerRequestDTO.rawPassword()
        );

        LoginResponseDTO expectedResponse = new LoginResponseDTO(registerRequestDTO.nickname());

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(expectedResponse)
                ))
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().exists("refresh_jwt"))
                .andReturn();

    }
}

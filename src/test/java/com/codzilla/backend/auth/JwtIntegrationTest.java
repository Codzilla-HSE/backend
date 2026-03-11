package com.codzilla.backend.auth;


import com.codzilla.backend.auth.JWTUtils.JWTUtils;
import com.codzilla.backend.auth.config.Settings;
import com.codzilla.backend.auth.dto.LoginRequestDTO;
import com.codzilla.backend.auth.dto.RegisterRequestDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.List;
import java.util.Timer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class JwtIntegrationTest extends BaseIntegrationTest {

    @Autowired
    JWTUtils jwtUtils;

    @Autowired
    Settings settings;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("nick", "email", "password");
        mockMvc.perform(post("/auth/signup")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerRequestDTO)))
               .andExpect(status().isOk());
    }

    @Test
    void testGetAccessWithJwt() throws Exception {

        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("nick", "email", "password");
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                registerRequestDTO.email(),
                registerRequestDTO.rawPassword()
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(loginRequestDTO)))
                                  .andExpect(status().isOk())
                                  .andExpect(cookie().exists("jwt"))
                                  .andExpect(cookie().exists("refresh_jwt"))
                                  .andReturn();

        var JwtCookie = result.getResponse().getCookie("jwt");
        String token = JwtCookie.getValue();
        assertThat(jwtUtils.getEmailFromToken(token)).isEqualTo("email");
        assertThat(jwtUtils.getRolesFromToken(token)).isEqualTo(List.of("USER"));

        mockMvc.perform(get("/endpoint")
                       .cookie(JwtCookie)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerRequestDTO)))
               .andExpect(status().isOk());
    }

    @Test
    void testExpirationToken() throws Exception {
        settings.setAccessTokenTtl(Duration.ZERO);
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("nick", "email", "password");
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                registerRequestDTO.email(),
                registerRequestDTO.rawPassword()
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(loginRequestDTO)))
                                  .andExpect(status().isOk())
                                  .andExpect(cookie().exists("jwt"))
                                  .andExpect(cookie().exists("refresh_jwt"))
                                  .andReturn();

        var JwtCookie = result.getResponse().getCookie("jwt");

        mockMvc.perform(get("/endpoint")
                       .cookie(JwtCookie)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerRequestDTO)))
               .andExpect(status().is(401));
    }

    @Test
    void testTokenShouldExpiredAndRefreshed() throws Exception {
        settings.setAccessTokenTtl(Duration.ofMillis(1000));
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("nick", "email", "password");
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                registerRequestDTO.email(),
                registerRequestDTO.rawPassword()
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(loginRequestDTO)))
                                  .andExpect(status().isOk())
                                  .andExpect(cookie().exists("jwt"))
                                  .andExpect(cookie().exists("refresh_jwt"))
                                  .andReturn();

        var JwtAccessCookie = result.getResponse().getCookie("jwt");
        var JwtRefreshCookie = result.getResponse().getCookie("refresh_jwt");


        mockMvc.perform(get("/endpoint")
                       .cookie(JwtAccessCookie)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerRequestDTO)))
               .andExpect(status().isOk());
        Thread.sleep(1000);
        mockMvc.perform(get("/endpoint")
                       .cookie(JwtAccessCookie)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerRequestDTO)))
               .andExpect(status().is(401));

        MvcResult refresh = mockMvc.perform(post("/auth/refresh")
                                           .cookie(JwtRefreshCookie))
                                  .andExpect(status().isOk())
                                  .andExpect(cookie().exists("jwt"))
                                  .andReturn();

        var newJwtAccessCookie = refresh.getResponse().getCookie("jwt");
        mockMvc.perform(get("/endpoint")
                       .cookie(newJwtAccessCookie)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerRequestDTO)))
               .andExpect(status().isOk());
    }
}

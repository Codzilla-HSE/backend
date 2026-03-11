package com.codzilla.backend.auth;


import com.codzilla.backend.auth.dto.LoginRequestDTO;
import com.codzilla.backend.auth.dto.RegisterRequestDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class RoleAccessIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequestDTO user = new RegisterRequestDTO("nick", "email", "password");
        mockMvc.perform(post("/auth/signup")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(user)))
               .andExpect(status().isOk());

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                user.email(),
                user.rawPassword()
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(loginRequestDTO)))
                                  .andExpect(status().isOk())
                                  .andExpect(cookie().exists("jwt"))
                                  .andExpect(cookie().exists("refresh_jwt"))
                                  .andReturn();

        var accessCookie = result.getResponse().getCookie("jwt");


        mockMvc.perform(post("/auth/create-admin")
                       .contentType(MediaType.APPLICATION_JSON)
                       .cookie(accessCookie))
               .andExpect(status().isOk());
    }

    @Test
    void testUserCantUseAdminEndpoint () throws Exception {
        RegisterRequestDTO user = new RegisterRequestDTO("nick", "email", "password");
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                user.email(),
                user.rawPassword()
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(loginRequestDTO)))
                                  .andExpect(status().isOk())
                                  .andExpect(cookie().exists("jwt"))
                                  .andExpect(cookie().exists("refresh_jwt"))
                                  .andReturn();

        var accessCookie = result.getResponse().getCookie("jwt");

        mockMvc.perform(get("/admin/users")
                       .cookie(accessCookie))
               .andExpect(status().is(403));

    }

    @Test
    void testAdminCanUseAdminMethod() throws Exception {
        RegisterRequestDTO admin = new RegisterRequestDTO("admin", "a@gmail.com", "0");
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                admin.email(),
                admin.rawPassword()
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(loginRequestDTO)))
                                  .andExpect(status().isOk())
                                  .andExpect(cookie().exists("jwt"))
                                  .andExpect(cookie().exists("refresh_jwt"))
                                  .andReturn();

        var accessCookie = result.getResponse().getCookie("jwt");

        mockMvc.perform(get("/admin/users")
                       .cookie(accessCookie))
               .andExpect(status().isOk());
    }
}

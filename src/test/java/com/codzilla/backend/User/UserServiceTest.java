package com.codzilla.backend.User;

import com.codzilla.backend.Authentication.Exceptions.UserAlreadyExistsException;
import com.codzilla.backend.Authentication.Exceptions.UserNotFoundException;
import com.codzilla.backend.Authentication.Exceptions.UsernameIsTakenException;
import com.codzilla.backend.Authentication.dto.RegisterRequestDTO;
import com.codzilla.backend.Authentication.dto.UserResponseDTO;
import com.codzilla.backend.User.DTO.ChangeUserRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.not;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    UserService service;


    @Test
    void registerUser_ThrowsWhenEmailOrNicknameAlreadyExists() {
        String email = "email";
        String nickname = "nickname";
        when(repository.existsByEmail(email)).thenReturn(true);
        when(repository.existsByNickname(nickname)).thenReturn(true);
        RegisterRequestDTO dtoForEmail = new RegisterRequestDTO(
                "",
                email,
                ""
        );
        assertThrows(
                UserAlreadyExistsException.class,
                () -> service.registerUser(dtoForEmail)
        );
        RegisterRequestDTO dtoForNickname = new RegisterRequestDTO(
                nickname,
                "",
                ""
        );
        assertThrows(
                UsernameIsTakenException.class,
                () -> service.registerUser(dtoForNickname)
        );

    }

    @Test
    void registerUser_DefaultCase() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "nickname",
                "email",
                "password"
        );
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByNickname(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("encoded");
        service.registerUser(dto);

        verify(
                repository,
                times(1)
        ).save(User.builder()
                   .nickname(dto.nickname())
                   .email(dto.email())
                   .password(not(eq(dto.rawPassword())))
                   .build());
    }

    @Test
    void getByEmail_ThrowsWhenNotExists() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(
                UserNotFoundException.class,
                () -> service.getByEmail("email")
        );
    }

    @Test
    void getByEmail_DefaultCase() {
        User user = User.builder()
                        .email("email")
                        .build();
        when(repository.findByEmail("email"))
                .thenReturn(Optional.of(user));


        assertEquals(
                user,
                service.getByEmail("email")
        );

        verify(
                repository,
                times(1)
        ).findByEmail("email");


    }

    @Test
    void getAllUsers_DefaultCase() {
        User user1 = User.builder().nickname("1").build();
        User user2 = User.builder().nickname("2").build();

        List<UserResponseDTO> dtos = List.of(
                new UserResponseDTO(
                        user1.getNickname(),
                        user1.getEmail(),
                        user1.getId(),
                        user1.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
                ),
                new UserResponseDTO(
                        user2.getNickname(),
                        user2.getEmail(),
                        user2.getId(),
                        user2.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
                )
        );

        when(repository.findAll()).thenReturn(List.of(
                user1,
                user2
        ));

        assertEquals(
                dtos,
                service.getAllUsers()
        );

        verify(
                repository,
                times(1)
        ).findAll();


    }

    @Test
    void updateUser_ThrowsIfNotExists() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(
                UserNotFoundException.class,
                () -> service.updateUser(
                        "",
                        new ChangeUserRequestDTO("")
                )
        );
    }

    @Test
    void updateUser_DontChangeNicknameIfItsBlank() {
        User user = User.builder().nickname("nickname").build();
        ChangeUserRequestDTO dto = new ChangeUserRequestDTO(
                ""
        );

        when(repository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        service.updateUser("", dto);

        verify(
                repository,
                times(1)
        ).save(user);
    }

    @Test
    void updateUser_DefaultCase() {
        User oldUser = User.builder().nickname("nickname").build();
        User newUser = User.builder().nickname("new nickname").build();
        ChangeUserRequestDTO dto = new ChangeUserRequestDTO(
                "new nickname"
        );

        when(repository.findByEmail(anyString()))
                .thenReturn(Optional.of(oldUser));

        service.updateUser("",dto);

        verify(repository, times(1))
                .save(newUser);
    }
}
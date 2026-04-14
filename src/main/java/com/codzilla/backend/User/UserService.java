package com.codzilla.backend.User;

import com.codzilla.backend.Authentication.Exceptions.UserAlreadyExistsException;
import com.codzilla.backend.Authentication.Exceptions.UserNotFoundException;
import com.codzilla.backend.Authentication.Exceptions.UsernameIsTakenException;
import com.codzilla.backend.Authentication.dto.RegisterRequestDTO;
import com.codzilla.backend.Authentication.dto.UserResponseDTO;
import com.codzilla.backend.User.DTO.ChangeUserRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UUID getIdByEmail(String email) {
        try {
            return userRepository.findIdByEmail(email).get();
        } catch (NoSuchElementException e) {
            throw new UserNotFoundException();
        }
    }

    public void registerUser(RegisterRequestDTO dto) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException();
        }

        if (userRepository.existsByNickname(dto.nickname())) {
            throw new UsernameIsTakenException();
        }

        var user = User.builder()
                       .email(dto.email())
                       .nickname(dto.nickname())
                       .password(passwordEncoder.encode(dto.rawPassword())).build();
        userRepository.save(user);

    }

    public User getByEmail(String email) throws UserNotFoundException {
        var user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            log.info("username " + user.get().getNickname());
            return user.get();
        } else {
            throw new UserNotFoundException();
        }
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getNickname(),
                        user.getEmail(),
                        user.getId(),
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority).toList()))
                .toList();
    }


    public void createAdmin() {
        if (!userRepository.existsByNickname("a")) {
            User admin = User.builder()
                             .nickname("a")
                             .email("a@gmail.com")
                             .password(passwordEncoder.encode("0"))
                             .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
                             .build();
            userRepository.save(admin);
        }
    }

    public void updateUser(String email, ChangeUserRequestDTO dto) throws UserNotFoundException {
        var user = userRepository.findByEmail(email);
        if (user.isEmpty()) throw new UserNotFoundException();

        if (dto.nickname() != null && !dto.nickname().isBlank()) {
            user.get().setNickname(dto.nickname());
        }

        userRepository.save(user.get());
    }
}

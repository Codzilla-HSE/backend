package com.codzilla.backend.auth;

import com.codzilla.backend.auth.Exceptions.UserAlreadyExistsException;
import com.codzilla.backend.auth.Exceptions.UserNotFoundException;
import com.codzilla.backend.auth.Exceptions.UsernameIsTakenException;
import com.codzilla.backend.auth.Repository.UserRepository;
import com.codzilla.backend.auth.dto.RegisterRequestDTO;
import com.codzilla.backend.auth.dto.UserResponseDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(RegisterRequestDTO dto) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException();
        }

        if (userRepository.existsByUsername(dto.username())) {
            throw new UsernameIsTakenException();
        }

        var user = User.builder()
                       .email(dto.email())
                       .username(dto.username())
                       .password(passwordEncoder.encode(dto.rawPassword())).build();
        userRepository.save(user);

    }

    public User getByEmail(String email) {
        var user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new UserNotFoundException();
        }
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getUsername(),
                        user.getEmail(),
                        user.getId(),
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority).toList()))
                .toList();
    }


    public void createAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                             .username("admin")
                             .email("admin")
                             .password(passwordEncoder.encode("0"))
                             .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
                             .build();
            userRepository.save(admin);
        }
    }

}

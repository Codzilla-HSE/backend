package com.codzilla.backend.User;

import com.codzilla.backend.Rating.Glicko2Rating;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DialectOverride;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails, CredentialsContainer {

    private String nickname;
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    private Integer rating = (int) Glicko2Rating.DEFAULT_RATING;

    @Builder.Default
    private double ratingDeviation = Glicko2Rating.DEFAULT_RD;

    @Builder.Default
    private double volatility = Glicko2Rating.DEFAULT_VOLATILITY;

    private Instant lastMatchAt;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Builder.Default
    private List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("USER");

    @Override
    public void eraseCredentials() {

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.authorities == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return this.authorities;
    }
    @Override
    public @Nullable String getPassword() {
        return password;
    }

    public String getUsername() {
        return email;
    }
}

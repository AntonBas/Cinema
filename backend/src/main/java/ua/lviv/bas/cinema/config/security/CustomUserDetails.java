package ua.lviv.bas.cinema.config.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID = 2L;

    private final Long userId;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final String role;

    public CustomUserDetails(User user) {
        this(user.getId(), user.getEmail(), user.getPassword(), user.isEnabled(), user.getUserRole().name());
    }

    @JsonCreator
    public CustomUserDetails(@JsonProperty("userId") Long userId, @JsonProperty("email") String email,
            @JsonProperty("password") String password, @JsonProperty("enabled") boolean enabled,
            @JsonProperty("role") String role) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAdmin() {
        return UserRole.ROLE_ADMIN.name().equals(role);
    }
}

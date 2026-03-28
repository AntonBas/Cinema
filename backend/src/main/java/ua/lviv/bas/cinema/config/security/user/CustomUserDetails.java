package ua.lviv.bas.cinema.config.security.user;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;

@Slf4j
@Getter
public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;

	private final User user;
	private final Long userId;
	private final String email;
	private final String password;
	private final boolean enabled;
	private final String role;

	public CustomUserDetails(User user) {
		this.user = user;
		this.userId = user.getId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.enabled = user.isEnabled();
		this.role = user.getUserRole().name();

		log.debug("Created CustomUserDetails for user: {}, role: {}, enabled: {}", email, role, enabled);
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
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return enabled;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public String getFullName() {
		return user.getFirstName() + " " + user.getLastName();
	}

	public boolean isAdmin() {
		return user.getUserRole() == UserRole.ROLE_ADMIN;
	}

	public boolean isCashier() {
		return user.getUserRole() == UserRole.ROLE_CASHIER;
	}

	public boolean isContentManager() {
		return user.getUserRole() == UserRole.ROLE_CONTENT_MANAGER;
	}
}
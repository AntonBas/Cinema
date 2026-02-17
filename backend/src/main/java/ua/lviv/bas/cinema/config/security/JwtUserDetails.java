package ua.lviv.bas.cinema.config.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

@Getter
public class JwtUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;

	private final Long userId;
	private final String email;
	private final String role;
	private final boolean enabled;
	private final Collection<? extends GrantedAuthority> authorities;

	public JwtUserDetails(Long userId, String email, String role, boolean enabled,
			Collection<? extends GrantedAuthority> authorities) {
		this.userId = userId;
		this.email = email;
		this.role = role;
		this.enabled = enabled;
		this.authorities = authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return email;
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
}
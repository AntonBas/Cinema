package ua.lviv.bas.cinema.config.security.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.repository.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#email", unless = "#result == null")
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		log.debug("Attempting to load user by email: {}", email);

		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			log.warn("User not found with email: {}", email);
			return new UsernameNotFoundException("User not found with email: " + email);
		});

		if (!user.isEnabled()) {
			log.warn("User {} is disabled but trying to login", email);
		}

		log.debug("User loaded successfully: {}, role: {}, enabled: {}", email, user.getUserRole(), user.isEnabled());

		return new CustomUserDetails(user);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#userId", unless = "#result == null")
	public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
		log.debug("Attempting to load user by id: {}", userId);

		User user = userRepository.findById(userId).orElseThrow(() -> {
			log.warn("User not found with id: {}", userId);
			return new UsernameNotFoundException("User not found with id: " + userId);
		});

		log.debug("User loaded successfully by id: {}, email: {}", userId, user.getEmail());

		return new CustomUserDetails(user);
	}
}
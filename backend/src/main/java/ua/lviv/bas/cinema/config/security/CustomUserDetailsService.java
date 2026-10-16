package ua.lviv.bas.cinema.config.security;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return new CustomUserDetails(findUser(email));
    }

    @Cacheable(value = "userDetails", key = "T(ua.lviv.bas.cinema.common.EmailNormalizer).normalize(#email)")
    @Transactional(readOnly = true)
    public CustomUserDetails loadTokenPrincipal(String email) throws UsernameNotFoundException {
        return CustomUserDetails.withoutPassword(findUser(email));
    }

    private User findUser(String email) {
        log.debug("Attempting to load user by email: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("User not found with email: {}", email);
            return new UsernameNotFoundException("User not found with email: " + email);
        });

        if (!user.isEnabled()) {
            log.warn("User {} is blocked but trying to login", email);
        }

        log.debug("User loaded successfully: {}, role: {}, enabled: {}", email, user.getUserRole(), user.isEnabled());

        return user;
    }

    @CacheEvict(value = "userDetails", key = "T(ua.lviv.bas.cinema.common.EmailNormalizer).normalize(#email)")
    public void evict(String email) {
        log.debug("Evicted cached user details for email: {}", email);
    }
}

package ua.lviv.bas.cinema.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public void registerUser(UserRegistrationDto userDto) {
        logger.info("Attempting to register user with email: {}", userDto.getEmail());

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            logger.error("Registration failed: Email {} is already registered", userDto.getEmail());
            throw new RuntimeException("Email is already registered");
        }

        if (!userDto.getPassword().equals(userDto.getPasswordConfirm())) {
            logger.error("Registration failed: Passwords do not match for email {}", userDto.getEmail());
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        userRepository.save(user);
        logger.info("User registered successfully with email: {}", userDto.getEmail());
    }

    @Transactional(readOnly = true)
    public Optional<User> findOptionalByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User updateUser(User user) {
        logger.info("Updating user with ID: {}", user.getId());
        return userRepository.save(user);
    }

    @Transactional
    public void verifyEmail(String email) {
        logger.info("Verifying email: {}", email);
        User user = findByEmail(email);
        user.setEnabled(true);
        userRepository.save(user);
        logger.info("Email verified successfully for: {}", email);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
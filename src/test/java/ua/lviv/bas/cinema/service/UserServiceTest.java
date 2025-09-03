package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityNotFoundException;
import ua.lviv.bas.cinema.dao.UserRepository;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
//@Import(TestSecurityConfig.class)
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserService userService;

	@Test
	void registerUser_ShouldSaveUser_WhenValidData() {
		UserRegistrationDto dto = UserRegistrationDto.builder().email("test@example.com").firstName("Anton")
				.lastName("Bas").dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.password("password123").passwordConfirm("password123").build();

		User user = User.builder().email("test@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.userRole(UserRole.ROLE_USER).enabled(false).build();

		when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
		when(userMapper.toEntity(dto)).thenReturn(user);
		when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenReturn(user);

		assertDoesNotThrow(() -> userService.registerUser(dto));

		verify(userRepository).findByEmail(dto.getEmail());
		verify(userMapper).toEntity(dto);
		verify(passwordEncoder).encode(dto.getPassword());
		verify(userRepository).save(any(User.class));
	}

	@Test
	void registerUser_ShouldThrowException_WhenEmailExists() {
		UserRegistrationDto dto = UserRegistrationDto.builder().email("existing@example.com").build();

		User existingUser = new User();
		when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));

		assertThrows(RuntimeException.class, () -> userService.registerUser(dto));
		verify(userRepository).findByEmail(dto.getEmail());
		verifyNoInteractions(userMapper, passwordEncoder);
	}

	@Test
	void registerUser_ShouldThrowException_WhenPasswordDontMatch() {
		UserRegistrationDto dto = UserRegistrationDto.builder().email("test@example.com").password("password123")
				.passwordConfirm("incorrectPassword").build();

		when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
		verify(userRepository).findByEmail(dto.getEmail());
		verifyNoInteractions(userMapper, passwordEncoder);
	}

	@Test
	void findByEmail_ShouldReturnUser_WhenExists() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		User result = userService.findByEmail(email);

		assertNotNull(result);
		assertEquals(email, result.getEmail());
		verify(userRepository).findByEmail(email);
	}

	@Test
	void findByEmail_ShouldThrowException_WhenNotFound() {
		String email = "noneexistent@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> userService.findByEmail(email));
		verify(userRepository).findByEmail(email);
	}

	@Test
	void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
		String email = "existing@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

		boolean result = userService.existsByEmail(email);

		assertTrue(result);
		verify(userRepository).findByEmail(email);
	}

	@Test
	void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
		String email = "noneexistent@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		boolean result = userService.existsByEmail(email);

		assertFalse(result);
		verify(userRepository).findByEmail(email);
	}
}

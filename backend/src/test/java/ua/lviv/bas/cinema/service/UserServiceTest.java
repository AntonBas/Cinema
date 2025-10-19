package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.UserDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.exception.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserService userService;

	private UserRegistrationDto validUserDto;
	private User user;
	private User savedUser;
	private UserDto userDto;

	@BeforeEach
	void setUp() {
		validUserDto = UserRegistrationDto.builder().email("test@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.password("password123").passwordConfirm("password123").build();

		user = User.builder().id(1L).email("test@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.userRole(UserRole.ROLE_USER).enabled(false).build();

		savedUser = User.builder().id(1L).email("test@example.com").firstName("Anton").lastName("Bas")
				.userRole(UserRole.ROLE_USER).enabled(false).build();

		userDto = UserDto.builder().id(1L).email("test@example.com").firstName("Anton").lastName("Bas")
				.userRole(UserRole.ROLE_USER).enabled(false).build();
	}

	@Test
	void registerUser_ShouldSaveUser_WhenValidData() {
		when(userRepository.findByEmail(validUserDto.getEmail())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(validUserDto.getPassword())).thenReturn("encodedPassword");
		when(userMapper.toEntityWithPassword(validUserDto, "encodedPassword")).thenReturn(user);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userMapper.toDto(savedUser)).thenReturn(userDto);

		UserDto result = userService.registerUser(validUserDto);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("test@example.com", result.getEmail());

		verify(userRepository).findByEmail(validUserDto.getEmail());
		verify(passwordEncoder).encode(validUserDto.getPassword());
		verify(userMapper).toEntityWithPassword(validUserDto, "encodedPassword");
		verify(userRepository).save(user);
		verify(userMapper).toDto(savedUser);
	}

	@Test
	void registerUser_ShouldThrowException_WhenEmailExists() {
		when(userRepository.findByEmail(validUserDto.getEmail())).thenReturn(Optional.of(user));

		assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(validUserDto));

		verify(userRepository).findByEmail(validUserDto.getEmail());
		verifyNoInteractions(passwordEncoder, userMapper);
	}

	@Test
	void findByEmail_ShouldReturnUser_WhenExists() {
		String email = "test@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		User result = userService.findByEmail(email);

		assertNotNull(result);
		assertEquals(email, result.getEmail());
		verify(userRepository).findByEmail(email);
	}

	@Test
	void findByEmail_ShouldThrowException_WhenNotFound() {
		String email = "nonexistent@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.findByEmail(email));
		verify(userRepository).findByEmail(email);
	}

	@Test
	void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
		String email = "test@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		boolean result = userService.existsByEmail(email);

		assertTrue(result);
		verify(userRepository).findByEmail(email);
	}

	@Test
	void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
		String email = "nonexistent@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		boolean result = userService.existsByEmail(email);

		assertFalse(result);
		verify(userRepository).findByEmail(email);
	}

	@Test
	void verifyEmail_ShouldEnableUser() {
		String email = "test@example.com";
		User disabledUser = User.builder().email(email).enabled(false).build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(disabledUser));
		when(userRepository.save(any(User.class))).thenReturn(disabledUser);

		userService.verifyEmail(email);

		assertTrue(disabledUser.isEnabled());
		verify(userRepository).findByEmail(email);
		verify(userRepository).save(disabledUser);
	}

	@Test
	void findById_ShouldReturnUser_WhenExists() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		User result = userService.findById(userId);

		assertNotNull(result);
		assertEquals(userId, result.getId());
		verify(userRepository).findById(userId);
	}

	@Test
	void findById_ShouldThrowException_WhenNotFound() {
		Long userId = 999L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
		verify(userRepository).findById(userId);
	}

	@Test
	void updateUser_ShouldSaveAndReturnUser() {
		when(userRepository.save(user)).thenReturn(savedUser);

		User result = userService.updateUser(user);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		verify(userRepository).save(user);
	}

	@Test
	void findOptionalByEmail_ShouldReturnOptionalUser() {
		String email = "test@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		Optional<User> result = userService.findOptionalByEmail(email);

		assertTrue(result.isPresent());
		assertEquals(email, result.get().getEmail());
		verify(userRepository).findByEmail(email);
	}
}
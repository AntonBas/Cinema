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
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
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

	private UserRegistrationRequest validUserDto;
	private User user;
	private User savedUser;
	private UserProfileResponse userProfileResponse;

	@BeforeEach
	void setUp() {
		validUserDto = UserRegistrationRequest.builder().email("test@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.password("password123").passwordConfirm("password123").build();

		user = User.builder().id(1L).email("test@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.userRole(UserRole.ROLE_USER).enabled(false).build();

		savedUser = User.builder().id(1L).email("test@example.com").firstName("Anton").lastName("Bas")
				.userRole(UserRole.ROLE_USER).enabled(false).build();

		// Створюємо UserProfileResponse без userRole
		userProfileResponse = UserProfileResponse.builder().id(1L).email("test@example.com").firstName("Anton")
				.lastName("Bas").build();
	}

	@Test
	void registerUser_ShouldSaveUser_WhenValidData() {
		when(userRepository.findByEmail(validUserDto.getEmail())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(validUserDto.getPassword())).thenReturn("encodedPassword");
		when(userMapper.toEntityWithPassword(validUserDto, "encodedPassword")).thenReturn(user);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userMapper.toProfileResponse(savedUser)).thenReturn(userProfileResponse);

		UserProfileResponse result = userService.registerUser(validUserDto);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("test@example.com", result.getEmail());

		verify(userRepository).findByEmail(validUserDto.getEmail());
		verify(passwordEncoder).encode(validUserDto.getPassword());
		verify(userMapper).toEntityWithPassword(validUserDto, "encodedPassword");
		verify(userRepository).save(user);
		verify(userMapper).toProfileResponse(savedUser);
	}

	@Test
	void registerUser_ShouldThrowException_WhenEmailExists() {
		when(userRepository.findByEmail(validUserDto.getEmail())).thenReturn(Optional.of(user));

		assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(validUserDto));

		verify(userRepository).findByEmail(validUserDto.getEmail());
		verifyNoInteractions(passwordEncoder, userMapper);
	}

	@Test
	void updateUser_ShouldUpdateAndReturnUserProfile() {
		Long userId = 1L;
		UserUpdateRequest updateRequest = UserUpdateRequest.builder().firstName("UpdatedName")
				.lastName("UpdatedLastName").build();

		User existingUser = User.builder().id(userId).email("test@example.com").firstName("Anton").lastName("Bas")
				.userRole(UserRole.ROLE_USER).enabled(true).build();

		User updatedUser = User.builder().id(userId).email("test@example.com").firstName("UpdatedName")
				.lastName("UpdatedLastName").userRole(UserRole.ROLE_USER).enabled(true).build();

		UserProfileResponse expectedResponse = UserProfileResponse.builder().id(userId).email("test@example.com")
				.firstName("UpdatedName").lastName("UpdatedLastName").build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		doAnswer(invocation -> {
			UserUpdateRequest request = invocation.getArgument(0);
			User userToUpdate = invocation.getArgument(1);
			userToUpdate.setFirstName(request.getFirstName());
			userToUpdate.setLastName(request.getLastName());
			return null;
		}).when(userMapper).updateUserFromDto(updateRequest, existingUser);
		when(userRepository.save(existingUser)).thenReturn(updatedUser);
		when(userMapper.toProfileResponse(updatedUser)).thenReturn(expectedResponse);

		UserProfileResponse result = userService.updateUser(userId, updateRequest);

		assertNotNull(result);
		assertEquals("UpdatedName", result.getFirstName());
		assertEquals("UpdatedLastName", result.getLastName());
		assertEquals(userId, result.getId());

		verify(userRepository).findById(userId);
		verify(userMapper).updateUserFromDto(updateRequest, existingUser);
		verify(userRepository).save(existingUser);
		verify(userMapper).toProfileResponse(updatedUser);
	}

	@Test
	void updateUser_ShouldThrowException_WhenUserNotFound() {
		Long userId = 999L;
		UserUpdateRequest updateRequest = UserUpdateRequest.builder().build();

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updateRequest));

		verify(userRepository).findById(userId);
		verifyNoInteractions(userMapper);
	}

	@Test
	void requestEmailChange_ShouldProceed_WhenNewEmailIsAvailable() {
		Long userId = 1L;
		String newEmail = "new@example.com";

		when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertDoesNotThrow(() -> userService.requestEmailChange(userId, newEmail));

		verify(userRepository).findByEmail(newEmail);
		verify(userRepository).findById(userId);
	}

	@Test
	void requestEmailChange_ShouldThrowException_WhenNewEmailExists() {
		Long userId = 1L;
		String newEmail = "existing@example.com";

		when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(user));

		assertThrows(EmailAlreadyExistsException.class, () -> userService.requestEmailChange(userId, newEmail));

		verify(userRepository).findByEmail(newEmail);
		verify(userRepository, never()).findById(userId);
	}

	@Test
	void updateUserPassword_ShouldUpdatePassword() {
		Long userId = 1L;
		String newPassword = "newPassword123";
		String encodedPassword = "encodedNewPassword";

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
		when(userRepository.save(user)).thenReturn(user);

		userService.updateUserPassword(userId, newPassword);

		assertEquals(encodedPassword, user.getPassword());
		verify(userRepository).findById(userId);
		verify(passwordEncoder).encode(newPassword);
		verify(userRepository).save(user);
	}

	@Test
	void getUserProfileById_ShouldReturnUserProfile() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toProfileResponse(user)).thenReturn(userProfileResponse);

		UserProfileResponse result = userService.getUserProfileById(userId);

		assertNotNull(result);
		assertEquals(userId, result.getId());
		verify(userRepository).findById(userId);
		verify(userMapper).toProfileResponse(user);
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

	@Test
	void confirmEmailChange_ShouldThrowRuntimeException() {
		String token = "test-token";

		RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.confirmEmailChange(token));

		assertEquals("Email change confirmation not implemented yet", exception.getMessage());
	}
}
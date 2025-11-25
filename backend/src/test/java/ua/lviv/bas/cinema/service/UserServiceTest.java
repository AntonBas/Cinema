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
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
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

	@Mock
	private EmailTokenService emailTokenService;

	@Mock
	private EmailTokenGeneratorService emailTokenGeneratorService;

	@InjectMocks
	private UserService userService;

	private UserRegistrationRequest validUserDto;
	private User user;
	private User savedUser;
	private UserProfileResponse userProfileResponse;
	private UserResponse userResponse;

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

		userProfileResponse = UserProfileResponse.builder().id(1L).email("test@example.com").firstName("Anton")
				.lastName("Bas").build();

		userResponse = UserResponse.builder().id(1L).email("test@example.com").firstName("Anton").lastName("Bas")
				.userRole(UserRole.ROLE_USER).enabled(false).build();
	}

	@Test
	void registerUser_ShouldSaveUser_WhenValidData() {
		when(userRepository.existsByEmail(validUserDto.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(validUserDto.getPassword())).thenReturn("encodedPassword");
		when(userMapper.toEntity(validUserDto)).thenReturn(user);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userMapper.toDto(savedUser)).thenReturn(userResponse);

		UserResponse result = userService.registerUser(validUserDto);

		assertNotNull(result);
		assertEquals("test@example.com", result.getEmail());

		verify(userRepository).existsByEmail(validUserDto.getEmail());
		verify(passwordEncoder).encode(validUserDto.getPassword());
		verify(userMapper).toEntity(validUserDto);
		verify(userRepository).save(user);
		verify(userMapper).toDto(savedUser);
		verify(emailTokenGeneratorService).generateVerificationToken(savedUser.getEmail());
	}

	@Test
	void registerUser_ShouldThrowException_WhenEmailExists() {
		when(userRepository.existsByEmail(validUserDto.getEmail())).thenReturn(true);

		assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(validUserDto));

		verify(userRepository).existsByEmail(validUserDto.getEmail());
		verifyNoInteractions(passwordEncoder, userMapper, emailTokenGeneratorService);
	}

	@Test
	void updateUser_ShouldUpdateAndReturnUserProfile() {
		Long userId = 1L;
		UserUpdateRequest updateRequest = UserUpdateRequest.builder().firstName("UpdatedName")
				.lastName("UpdatedLastName").build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		doAnswer(invocation -> {
			UserUpdateRequest request = invocation.getArgument(0);
			User userToUpdate = invocation.getArgument(1);
			userToUpdate.setFirstName(request.getFirstName());
			userToUpdate.setLastName(request.getLastName());
			return null;
		}).when(userMapper).updateUserFromDto(updateRequest, user);
		when(userRepository.save(user)).thenReturn(savedUser);
		when(userMapper.toProfileResponse(savedUser)).thenReturn(userProfileResponse);

		UserProfileResponse result = userService.updateUser(userId, updateRequest);

		assertNotNull(result);
		verify(userRepository).findById(userId);
		verify(userMapper).updateUserFromDto(updateRequest, user);
		verify(userRepository).save(user);
		verify(userMapper).toProfileResponse(savedUser);
	}

	@Test
	void updateUser_ShouldThrowException_WhenUserNotFound() {
		Long userId = 999L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, new UserUpdateRequest()));
		verify(userRepository).findById(userId);
		verifyNoInteractions(userMapper);
	}

	@Test
	void requestEmailChange_ShouldCallEmailTokenService_WhenNewEmailAvailable() {
		Long userId = 1L;
		String newEmail = "new@example.com";

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmail(newEmail)).thenReturn(false);

		assertDoesNotThrow(() -> userService.requestEmailChange(userId, newEmail));

		verify(userRepository).findById(userId);
		verify(userRepository).existsByEmail(newEmail);
		verify(emailTokenGeneratorService).generateEmailChangeToken(user.getEmail(), newEmail);
	}

	@Test
	void requestEmailChange_ShouldThrowException_WhenNewEmailExists() {
		Long userId = 1L;
		String newEmail = "existing@example.com";

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmail(newEmail)).thenReturn(true);

		assertThrows(EmailAlreadyExistsException.class, () -> userService.requestEmailChange(userId, newEmail));

		verify(userRepository).findById(userId);
		verify(userRepository).existsByEmail(newEmail);
		verify(emailTokenGeneratorService, never()).generateEmailChangeToken(anyString(), anyString());
	}

	@Test
	void confirmEmailChange_ShouldReturnUpdatedProfile() {
		String token = "token";
		User updatedUser = User.builder().id(1L).email("new@example.com").build();
		UserProfileResponse response = UserProfileResponse.builder().id(1L).email("new@example.com").build();

		when(emailTokenService.confirmEmailChange(token)).thenReturn(updatedUser);
		when(userMapper.toProfileResponse(updatedUser)).thenReturn(response);

		UserProfileResponse result = userService.confirmEmailChange(token);

		assertEquals("new@example.com", result.getEmail());
		verify(emailTokenService).confirmEmailChange(token);
		verify(userMapper).toProfileResponse(updatedUser);
	}

	@Test
	void updateUserPassword_ShouldUpdatePassword_WhenCurrentPasswordIsCorrectAndNewIsDifferent() {
		String currentPassword = "oldPassword123";
		String newPassword = "newPassword123";
		String encodedNewPassword = "encodedNewPassword";

		user.setPassword("encodedOldPassword");

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(false);
		when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
		when(userRepository.save(user)).thenReturn(user);

		userService.updateUserPassword(user.getId(), currentPassword, newPassword);

		assertEquals(encodedNewPassword, user.getPassword());

		verify(passwordEncoder, times(2)).matches(anyString(), anyString());
		verify(passwordEncoder).encode(newPassword);
		verify(userRepository).save(user);
	}

	@Test
	void updateUserPassword_ShouldThrowException_WhenCurrentPasswordIsIncorrect() {
		String currentPassword = "wrongPassword";
		String newPassword = "newPassword123";

		user.setPassword("encodedOldPassword");

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(false);

		assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
				() -> userService.updateUserPassword(user.getId(), currentPassword, newPassword));

		verify(passwordEncoder, times(1)).matches(anyString(), anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserPassword_ShouldThrowException_WhenNewPasswordIsSameAsOld() {
		String currentPassword = "oldPassword123";
		String newPassword = "oldPassword123";

		user.setPassword("encodedOldPassword");

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(true);

		assertThrows(ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException.class,
				() -> userService.updateUserPassword(user.getId(), currentPassword, newPassword));

		verify(passwordEncoder, times(2)).matches(anyString(), anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

}
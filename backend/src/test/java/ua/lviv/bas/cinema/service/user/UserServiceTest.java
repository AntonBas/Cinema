package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidCurrentPasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.PasswordMismatchException;
import ua.lviv.bas.cinema.exception.domain.auth.PasswordValidationException;
import ua.lviv.bas.cinema.exception.domain.auth.SameEmailException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.notification.EmailTokenGeneratorService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserMapper userMapper;

	@Mock
	private EmailTokenGeneratorService emailTokenGeneratorService;

	@InjectMocks
	private UserService userService;

	@Test
	void registerUser_ShouldRegisterUser() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setEmail("test@example.com");
		request.setPassword("password123");
		request.setPasswordConfirm("password123");

		User user = new User();
		User savedUser = new User();
		UserResponse response = new UserResponse();

		when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
		when(userMapper.toUser(request)).thenReturn(user);
		when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
		when(userRepository.save(user)).thenReturn(savedUser);
		when(userMapper.toUserResponse(savedUser)).thenReturn(response);

		UserResponse result = userService.registerUser(request);

		assertThat(result).isEqualTo(response);
		verify(passwordEncoder).encode(request.getPassword());
		verify(userRepository).save(user);
		verify(emailTokenGeneratorService).generateVerificationToken(savedUser.getEmail());
	}

	@Test
	void registerUser_ShouldThrowWhenPasswordMismatch() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setPassword("password123");
		request.setPasswordConfirm("different");

		assertThatThrownBy(() -> userService.registerUser(request)).isInstanceOf(PasswordMismatchException.class);
	}

	@Test
	void registerUser_ShouldThrowWhenEmailExists() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setEmail("test@example.com");
		request.setPassword("password123");
		request.setPasswordConfirm("password123");

		when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

		assertThatThrownBy(() -> userService.registerUser(request)).isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void updateUser_ShouldUpdateUser() {
		Long userId = 1L;
		UserUpdateRequest request = new UserUpdateRequest();
		User user = new User();
		user.setVerificationStatus(VerificationStatus.VERIFIED);
		UserProfileResponse response = new UserProfileResponse();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toUserProfileResponse(user)).thenReturn(response);

		UserProfileResponse result = userService.updateUser(userId, request);

		assertThat(result).isEqualTo(response);
		verify(userMapper).updateUserFromRequest(request, user);
		verify(userRepository).save(user);
	}

	@Test
	void updateUser_ShouldRevokeVerificationWhenBirthDateChanged() {
		Long userId = 1L;
		UserUpdateRequest request = new UserUpdateRequest();
		request.setDateOfBirth(LocalDate.now());

		User user = new User();
		user.setDateOfBirth(LocalDate.now().minusDays(1));
		user.setVerificationStatus(VerificationStatus.VERIFIED);
		user.setVerifiedAt(LocalDateTime.now());

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toUserProfileResponse(user)).thenReturn(new UserProfileResponse());

		userService.updateUser(userId, request);

		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(user.getVerifiedAt()).isNull();
	}

	@Test
	void updateUser_ShouldThrowWhenUserNotFound() {
		Long userId = 1L;
		UserUpdateRequest request = new UserUpdateRequest();

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updateUser(userId, request)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void requestEmailChange_ShouldGenerateToken() {
		Long userId = 1L;
		String newEmail = "new@example.com";
		User user = new User();
		user.setEmail("old@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmail(newEmail)).thenReturn(false);

		userService.requestEmailChange(userId, newEmail);

		verify(emailTokenGeneratorService).generateEmailChangeToken(user.getEmail(), newEmail);
	}

	@Test
	void requestEmailChange_ShouldThrowWhenSameEmail() {
		Long userId = 1L;
		String sameEmail = "test@example.com";
		User user = new User();
		user.setEmail(sameEmail);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.requestEmailChange(userId, sameEmail))
				.isInstanceOf(SameEmailException.class);
	}

	@Test
	void requestEmailChange_ShouldThrowWhenEmailExists() {
		Long userId = 1L;
		String existingEmail = "existing@example.com";
		User user = new User();
		user.setEmail("old@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

		assertThatThrownBy(() -> userService.requestEmailChange(userId, existingEmail))
				.isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void updateUserPassword_ShouldUpdatePassword() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPass");
		request.setNewPassword("newPass123");
		request.setPasswordConfirm("newPass123");

		User user = new User();
		user.setPassword("encodedOld");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("oldPass", "encodedOld")).thenReturn(true);
		when(passwordEncoder.matches("newPass123", "encodedOld")).thenReturn(false);
		when(passwordEncoder.encode("newPass123")).thenReturn("encodedNew");

		userService.updateUserPassword(userId, request);

		assertThat(user.getPassword()).isEqualTo("encodedNew");
		verify(userRepository).save(user);
	}

	@Test
	void updateUserPassword_ShouldThrowWhenPasswordMismatch() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setNewPassword("pass1");
		request.setPasswordConfirm("pass2");

		User user = new User();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.updateUserPassword(userId, request))
				.isInstanceOf(PasswordMismatchException.class);
	}

	@Test
	void updateUserPassword_ShouldThrowWhenCurrentPasswordInvalid() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("wrong");
		request.setNewPassword("newPass123");
		request.setPasswordConfirm("newPass123");

		User user = new User();
		user.setPassword("encoded");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

		assertThatThrownBy(() -> userService.updateUserPassword(userId, request))
				.isInstanceOf(InvalidCurrentPasswordException.class);
	}

	@Test
	void updateUserPassword_ShouldThrowWhenSamePassword() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("same");
		request.setNewPassword("same");
		request.setPasswordConfirm("same");

		User user = new User();
		user.setPassword("encoded");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("same", "encoded")).thenReturn(true);

		assertThatThrownBy(() -> userService.updateUserPassword(userId, request))
				.isInstanceOf(SamePasswordException.class);
	}

	@Test
	void updateUserPassword_ShouldThrowWhenPasswordTooShort() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("old");
		request.setNewPassword("short");
		request.setPasswordConfirm("short");

		User user = new User();
		user.setPassword("encoded");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("old", "encoded")).thenReturn(true);

		assertThatThrownBy(() -> userService.updateUserPassword(userId, request))
				.isInstanceOf(PasswordValidationException.class);
	}

	@Test
	void getUserById_ShouldReturnUser() {
		Long userId = 1L;
		User user = new User();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		User result = userService.getUserById(userId);

		assertThat(result).isEqualTo(user);
	}

	@Test
	void getUserById_ShouldThrowWhenNotFound() {
		Long userId = 1L;

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getUserById(userId)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void getUserByEmail_ShouldReturnUser() {
		String email = "test@example.com";
		User user = new User();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		User result = userService.getUserByEmail(email);

		assertThat(result).isEqualTo(user);
	}

	@Test
	void getUserProfile_ShouldReturnProfileResponse() {
		Long userId = 1L;
		User user = new User();
		UserProfileResponse response = new UserProfileResponse();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toUserProfileResponse(user)).thenReturn(response);

		UserProfileResponse result = userService.getUserProfile(userId);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void emailExists_ShouldReturnTrue() {
		String email = "test@example.com";

		when(userRepository.existsByEmail(email)).thenReturn(true);

		boolean result = userService.emailExists(email);

		assertThat(result).isTrue();
	}
}
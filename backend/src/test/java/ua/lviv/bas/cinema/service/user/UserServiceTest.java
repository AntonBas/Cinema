package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.PasswordMismatchException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.notification.EmailTokenGeneratorService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

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

	private final Long USER_ID = 1L;
	private final String EMAIL = "test@example.com";

	@Test
	void registerUser_Success() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setEmail(EMAIL);
		request.setPassword("password");
		request.setPasswordConfirm("password");

		User user = new User();
		user.setEmail(EMAIL);

		User savedUser = new User();
		savedUser.setEmail(EMAIL);

		UserResponse response = new UserResponse();
		response.setEmail(EMAIL);

		when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
		when(userMapper.toUser(request)).thenReturn(user);
		when(passwordEncoder.encode("password")).thenReturn("encoded");
		when(userRepository.save(user)).thenReturn(savedUser);
		when(userMapper.toUserResponse(savedUser)).thenReturn(response);

		UserResponse result = userService.registerUser(request);

		assertThat(result).isEqualTo(response);
		verify(userRepository).save(user);
		verify(emailTokenGeneratorService).generateVerificationToken(EMAIL);
	}

	@Test
	void registerUser_ThrowsException_WhenPasswordsDontMatch() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setPassword("pass1");
		request.setPasswordConfirm("pass2");

		assertThatThrownBy(() -> userService.registerUser(request)).isInstanceOf(PasswordMismatchException.class);
	}

	@Test
	void registerUser_ThrowsException_WhenEmailExists() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setEmail(EMAIL);
		request.setPassword("password");
		request.setPasswordConfirm("password");

		when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

		assertThatThrownBy(() -> userService.registerUser(request)).isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void getUserById_Success() {
		User user = new User();
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		User result = userService.getUserById(USER_ID);

		assertThat(result).isEqualTo(user);
	}

	@Test
	void getUserById_ThrowsException_WhenNotFound() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getUserById(USER_ID)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void updateUser_Success() {
		User user = new User();
		user.setId(USER_ID);

		UserUpdateRequest request = new UserUpdateRequest();
		request.setFirstName("John");

		UserProfileResponse profileResponse = new UserProfileResponse();
		profileResponse.setId(USER_ID);

		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toUserProfileResponse(user)).thenReturn(profileResponse);

		UserProfileResponse result = userService.updateUser(USER_ID, request);

		assertThat(result).isEqualTo(profileResponse);
		verify(userMapper).updateUserFromRequest(request, user);
		verify(userRepository).save(user);
	}

	@Test
	void emailExists_ReturnsTrue() {
		when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

		boolean result = userService.emailExists(EMAIL);

		assertThat(result).isTrue();
	}
}
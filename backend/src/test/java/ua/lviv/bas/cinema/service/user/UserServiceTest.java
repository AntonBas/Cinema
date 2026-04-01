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

import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidCurrentPasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.PasswordMismatchException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.user.UserMapper;
import ua.lviv.bas.cinema.repository.user.UserRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
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
	@Mock
	private AuditService auditService;
	@InjectMocks
	private UserService userService;

	private final Long USER_ID = 1L;
	private final String EMAIL = "test@example.com";
	private final String PASSWORD = "password123";
	private final String ENCODED_PASSWORD = "encodedPassword";
	private final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
	private final String CITY = "Kyiv";
	private final String PHONE = "+380501234567";

	@Test
	void registerUser_Success() {
		UserRegistrationRequest request = new UserRegistrationRequest(EMAIL, "John", "Doe", DATE_OF_BIRTH, CITY, PHONE,
				PASSWORD, PASSWORD);

		User user = new User();
		user.setEmail(EMAIL);

		User savedUser = new User();
		savedUser.setId(USER_ID);
		savedUser.setEmail(EMAIL);
		savedUser.setFirstName("John");
		savedUser.setLastName("Doe");
		savedUser.setDateOfBirth(DATE_OF_BIRTH);
		savedUser.setCity(CITY);
		savedUser.setPhoneNumber(PHONE);
		savedUser.setUserRole(UserRole.ROLE_USER);
		savedUser.setEnabled(false);
		savedUser.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		UserResponse response = new UserResponse(USER_ID, EMAIL, "John", "Doe", DATE_OF_BIRTH, CITY, PHONE,
				UserRole.ROLE_USER, false, VerificationStatus.NOT_VERIFIED);

		when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
		when(userMapper.toUser(request)).thenReturn(user);
		when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
		when(userRepository.save(user)).thenReturn(savedUser);
		when(userMapper.toUserResponse(savedUser)).thenReturn(response);

		UserResponse result = userService.registerUser(request);

		assertThat(result).isEqualTo(response);
		verify(userRepository).save(user);
		verify(emailTokenGeneratorService).generateVerificationToken(EMAIL);
	}

	@Test
	void registerUser_ThrowsException_WhenPasswordsDontMatch() {
		UserRegistrationRequest request = new UserRegistrationRequest(EMAIL, "John", "Doe", DATE_OF_BIRTH, CITY, PHONE,
				"pass1", "pass2");

		assertThatThrownBy(() -> userService.registerUser(request)).isInstanceOf(PasswordMismatchException.class);
	}

	@Test
	void registerUser_ThrowsException_WhenEmailExists() {
		UserRegistrationRequest request = new UserRegistrationRequest(EMAIL, "John", "Doe", DATE_OF_BIRTH, CITY, PHONE,
				PASSWORD, PASSWORD);

		when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

		assertThatThrownBy(() -> userService.registerUser(request)).isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void getUserById_Success() {
		User user = new User();
		user.setId(USER_ID);
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
	void getUserByEmail_Success() {
		User user = new User();
		user.setEmail(EMAIL);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

		User result = userService.getUserByEmail(EMAIL);

		assertThat(result).isEqualTo(user);
	}

	@Test
	void getUserByEmail_ThrowsException_WhenNotFound() {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getUserByEmail(EMAIL)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void getUserWithBonusCardById_Success() {
		User user = new User();
		user.setId(USER_ID);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));

		User result = userService.getUserWithBonusCardById(USER_ID);

		assertThat(result).isEqualTo(user);
	}

	@Test
	void getUserWithTicketsById_Success() {
		User user = new User();
		user.setId(USER_ID);
		when(userRepository.findWithTicketsById(USER_ID)).thenReturn(Optional.of(user));

		User result = userService.getUserWithTicketsById(USER_ID);

		assertThat(result).isEqualTo(user);
	}

	@Test
	void updateUser_Success() {
		User user = User.builder().id(USER_ID).firstName("Old").lastName("User").dateOfBirth(DATE_OF_BIRTH)
				.city("Old City").phoneNumber(PHONE).verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		LocalDate newDateOfBirth = LocalDate.of(1995, 5, 5);
		UserUpdateRequest request = new UserUpdateRequest("John", "Doe", newDateOfBirth, "Kyiv", "+380502345678");

		UserProfileResponse profileResponse = new UserProfileResponse(USER_ID, EMAIL, "John", "Doe", newDateOfBirth,
				"Kyiv", "+380502345678", VerificationStatus.NOT_VERIFIED);

		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toUserProfileResponse(user)).thenReturn(profileResponse);

		UserProfileResponse result = userService.updateUser(USER_ID, request);

		assertThat(result).isEqualTo(profileResponse);
		verify(userMapper).updateUserFromRequest(request, user);
		verify(userRepository).save(user);
	}

	@Test
	void updateUser_WhenDateOfBirthChanged_RevokesVerification() {
		User user = User.builder().id(USER_ID).firstName("John").lastName("Doe").dateOfBirth(DATE_OF_BIRTH).city(CITY)
				.phoneNumber(PHONE).verificationStatus(VerificationStatus.VERIFIED).verifiedAt(LocalDateTime.now())
				.build();

		LocalDate newDateOfBirth = LocalDate.of(1995, 5, 5);
		UserUpdateRequest request = new UserUpdateRequest("John", "Doe", newDateOfBirth, CITY, PHONE);

		UserProfileResponse profileResponse = new UserProfileResponse(USER_ID, EMAIL, "John", "Doe", newDateOfBirth,
				CITY, PHONE, VerificationStatus.NOT_VERIFIED);

		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toUserProfileResponse(user)).thenReturn(profileResponse);

		UserProfileResponse result = userService.updateUser(USER_ID, request);

		assertThat(result).isEqualTo(profileResponse);
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(user.getVerifiedAt()).isNull();
	}

	@Test
	void getUserProfile_Success() {
		User user = User.builder().id(USER_ID).email(EMAIL).firstName("John").lastName("Doe").dateOfBirth(DATE_OF_BIRTH)
				.city(CITY).phoneNumber(PHONE).verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		UserProfileResponse profileResponse = new UserProfileResponse(USER_ID, EMAIL, "John", "Doe", DATE_OF_BIRTH,
				CITY, PHONE, VerificationStatus.NOT_VERIFIED);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userMapper.toUserProfileResponse(user)).thenReturn(profileResponse);

		UserProfileResponse result = userService.getUserProfile(USER_ID);

		assertThat(result).isEqualTo(profileResponse);
	}

	@Test
	void getUserResponseById_Success() {
		User user = User.builder().id(USER_ID).email(EMAIL).firstName("John").lastName("Doe").dateOfBirth(DATE_OF_BIRTH)
				.city(CITY).phoneNumber(PHONE).userRole(UserRole.ROLE_USER).enabled(true)
				.verificationStatus(VerificationStatus.VERIFIED).build();

		UserResponse response = new UserResponse(USER_ID, EMAIL, "John", "Doe", DATE_OF_BIRTH, CITY, PHONE,
				UserRole.ROLE_USER, true, VerificationStatus.VERIFIED);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userMapper.toUserResponse(user)).thenReturn(response);

		UserResponse result = userService.getUserResponseById(USER_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getUserResponseByEmail_Success() {
		User user = User.builder().id(USER_ID).email(EMAIL).firstName("John").lastName("Doe").dateOfBirth(DATE_OF_BIRTH)
				.city(CITY).phoneNumber(PHONE).userRole(UserRole.ROLE_USER).enabled(true)
				.verificationStatus(VerificationStatus.VERIFIED).build();

		UserResponse response = new UserResponse(USER_ID, EMAIL, "John", "Doe", DATE_OF_BIRTH, CITY, PHONE,
				UserRole.ROLE_USER, true, VerificationStatus.VERIFIED);

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(userMapper.toUserResponse(user)).thenReturn(response);

		UserResponse result = userService.getUserResponseByEmail(EMAIL);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void emailExists_ReturnsTrue() {
		when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

		boolean result = userService.emailExists(EMAIL);

		assertThat(result).isTrue();
	}

	@Test
	void emailExists_ReturnsFalse() {
		when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

		boolean result = userService.emailExists(EMAIL);

		assertThat(result).isFalse();
	}

	@Test
	void updateUserPassword_Success() {
		User user = User.builder().id(USER_ID).email(EMAIL).password(ENCODED_PASSWORD).build();

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("oldPassword", "newPassword123",
				"newPassword123");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("oldPassword", ENCODED_PASSWORD)).thenReturn(true);
		when(passwordEncoder.matches("newPassword123", ENCODED_PASSWORD)).thenReturn(false);
		when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

		userService.updateUserPassword(USER_ID, request);

		verify(userRepository).save(user);
		assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
	}

	@Test
	void updateUserPassword_ThrowsException_WhenPasswordsDontMatch() {
		User user = User.builder().id(USER_ID).password(ENCODED_PASSWORD).build();

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("oldPassword", "newPassword",
				"differentPassword");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.updateUserPassword(USER_ID, request))
				.isInstanceOf(PasswordMismatchException.class);
	}

	@Test
	void updateUserPassword_ThrowsException_WhenCurrentPasswordInvalid() {
		User user = User.builder().id(USER_ID).password(ENCODED_PASSWORD).build();

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("wrongPassword", "newPassword",
				"newPassword");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

		assertThatThrownBy(() -> userService.updateUserPassword(USER_ID, request))
				.isInstanceOf(InvalidCurrentPasswordException.class);
	}

	@Test
	void updateUserPassword_ThrowsException_WhenNewPasswordSameAsOld() {
		User user = User.builder().id(USER_ID).password(ENCODED_PASSWORD).build();

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("oldPassword", "samePassword",
				"samePassword");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("oldPassword", ENCODED_PASSWORD)).thenReturn(true);
		when(passwordEncoder.matches("samePassword", ENCODED_PASSWORD)).thenReturn(true);

		assertThatThrownBy(() -> userService.updateUserPassword(USER_ID, request))
				.isInstanceOf(SamePasswordException.class);
	}
}
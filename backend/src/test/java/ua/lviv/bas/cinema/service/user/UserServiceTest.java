package ua.lviv.bas.cinema.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import ua.lviv.bas.cinema.service.notification.EmailTokenService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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

	@Captor
	private ArgumentCaptor<User> userCaptor;

	private User testUser;
	private final Long USER_ID = 1L;
	private final String USER_EMAIL = "anton.bas@example.com";
	private final String USER_PHONE = "+3801234567";
	private final String ENCODED_PASSWORD = "encodedPassword123";

	@BeforeEach
	void setUp() {
		testUser = User.builder().id(USER_ID).email(USER_EMAIL).firstName("Anton").lastName("Bas")
				.phoneNumber(USER_PHONE).dateOfBirth(LocalDate.of(1990, 1, 1)).password(ENCODED_PASSWORD)
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(LocalDateTime.now()).city("Kyiv").build();
	}

	@Test
	void registerUser_Success() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setEmail("new.user@example.com");
		request.setPhoneNumber("+3807654321");
		request.setFirstName("Anton");
		request.setLastName("Bas");
		request.setPassword("password123");
		request.setPasswordConfirm("password123");
		request.setCity("Lviv");
		request.setDateOfBirth(LocalDate.of(1995, 5, 15));

		User newUser = User.builder().email(request.getEmail()).phoneNumber(request.getPhoneNumber())
				.firstName(request.getFirstName()).lastName(request.getLastName()).city(request.getCity())
				.dateOfBirth(request.getDateOfBirth()).build();

		User savedUser = User.builder().id(2L).email(request.getEmail()).phoneNumber(request.getPhoneNumber())
				.firstName(request.getFirstName()).lastName(request.getLastName()).city(request.getCity())
				.dateOfBirth(request.getDateOfBirth()).password(ENCODED_PASSWORD).build();

		UserResponse expectedResponse = new UserResponse();

		when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
		when(userMapper.toUser(request)).thenReturn(newUser);
		when(passwordEncoder.encode(request.getPassword())).thenReturn(ENCODED_PASSWORD);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userMapper.toUserResponse(savedUser)).thenReturn(expectedResponse);

		UserResponse result = userService.registerUser(request);

		assertNotNull(result);
		verify(passwordEncoder).encode(request.getPassword());
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();
		assertEquals(ENCODED_PASSWORD, capturedUser.getPassword());
		verify(emailTokenGeneratorService).generateVerificationToken(savedUser.getEmail());
	}

	@Test
	void registerUser_PasswordMismatch() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setEmail("anton.bas@example.com");
		request.setPassword("password123");
		request.setPasswordConfirm("differentPassword");

		assertThrows(PasswordMismatchException.class, () -> userService.registerUser(request));
	}

	@Test
	void registerUser_EmailAlreadyExists() {
		UserRegistrationRequest request = new UserRegistrationRequest();
		request.setEmail("existing@example.com");
		request.setPassword("password123");
		request.setPasswordConfirm("password123");

		when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

		assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(request));
	}

	@Test
	void updateUser_Success() {
		UserUpdateRequest request = new UserUpdateRequest();
		request.setFirstName("Anton");
		request.setLastName("Bas Updated");
		request.setDateOfBirth(LocalDate.of(1995, 5, 15));

		User updatedUser = User.builder().id(USER_ID).email(USER_EMAIL).phoneNumber(USER_PHONE).firstName("Anton")
				.lastName("Bas Updated").dateOfBirth(LocalDate.of(1995, 5, 15)).password(ENCODED_PASSWORD)
				.verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null).city("Kyiv").build();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);
		when(userMapper.toUserProfileResponse(updatedUser)).thenReturn(new UserProfileResponse());

		UserProfileResponse result = userService.updateUser(USER_ID, request);

		assertNotNull(result);
		verify(userMapper).updateUserFromRequest(request, testUser);
		verify(userRepository).save(any(User.class));
	}

	@Test
	void updateUser_BirthDateChanged_VerificationRevoked() {
		UserUpdateRequest request = new UserUpdateRequest();
		request.setDateOfBirth(LocalDate.of(1995, 5, 15));

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(any(User.class))).thenReturn(testUser);
		when(userMapper.toUserProfileResponse(testUser)).thenReturn(new UserProfileResponse());

		userService.updateUser(USER_ID, request);

		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();
		assertEquals(VerificationStatus.NOT_VERIFIED, capturedUser.getVerificationStatus());
		assertNull(capturedUser.getVerifiedAt());
	}

	@Test
	void requestEmailChange_Success() {
		String newEmail = "anton.new@example.com";

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.existsByEmail(newEmail)).thenReturn(false);

		userService.requestEmailChange(USER_ID, newEmail);

		verify(emailTokenGeneratorService).generateEmailChangeToken(USER_EMAIL, newEmail);
	}

	@Test
	void requestEmailChange_SameEmail() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

		assertThrows(SameEmailException.class, () -> userService.requestEmailChange(USER_ID, USER_EMAIL));
	}

	@Test
	void requestEmailChange_EmailAlreadyExists() {
		String newEmail = "existing@example.com";

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.existsByEmail(newEmail)).thenReturn(true);

		assertThrows(EmailAlreadyExistsException.class, () -> userService.requestEmailChange(USER_ID, newEmail));
	}

	@Test
	void updateUserPassword_Success() {
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPassword123");
		request.setNewPassword("newPassword123");
		request.setPasswordConfirm("newPassword123");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(passwordEncoder.matches(request.getCurrentPassword(), ENCODED_PASSWORD)).thenReturn(true);
		when(passwordEncoder.matches(request.getNewPassword(), ENCODED_PASSWORD)).thenReturn(false);
		when(passwordEncoder.encode(request.getNewPassword())).thenReturn("newEncodedPassword");

		userService.updateUserPassword(USER_ID, request);

		verify(passwordEncoder).encode(request.getNewPassword());
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();
		assertEquals("newEncodedPassword", capturedUser.getPassword());
	}

	@Test
	void updateUserPassword_PasswordMismatch() {
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setNewPassword("password123");
		request.setPasswordConfirm("differentPassword");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

		assertThrows(PasswordMismatchException.class, () -> userService.updateUserPassword(USER_ID, request));
	}

	@Test
	void updateUserPassword_InvalidCurrentPassword() {
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("wrongPassword");
		request.setNewPassword("newPassword123");
		request.setPasswordConfirm("newPassword123");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(passwordEncoder.matches(request.getCurrentPassword(), ENCODED_PASSWORD)).thenReturn(false);

		assertThrows(InvalidCurrentPasswordException.class, () -> userService.updateUserPassword(USER_ID, request));
	}

	@Test
	void updateUserPassword_SamePassword() {
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPassword123");
		request.setNewPassword("samePassword");
		request.setPasswordConfirm("samePassword");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(passwordEncoder.matches(request.getCurrentPassword(), ENCODED_PASSWORD)).thenReturn(true);
		when(passwordEncoder.matches(request.getNewPassword(), ENCODED_PASSWORD)).thenReturn(true);

		assertThrows(SamePasswordException.class, () -> userService.updateUserPassword(USER_ID, request));
	}

	@Test
	void updateUserPassword_TooShortPassword() {
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPassword123");
		request.setNewPassword("short");
		request.setPasswordConfirm("short");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(passwordEncoder.matches(request.getCurrentPassword(), ENCODED_PASSWORD)).thenReturn(true);

		assertThrows(PasswordValidationException.class, () -> userService.updateUserPassword(USER_ID, request));
	}

	@Test
	void confirmRegistration_Success() {
		String token = "verificationToken";
		String expectedMessage = "Email verified successfully";

		when(emailTokenService.confirmEmail(token)).thenReturn(expectedMessage);

		String result = userService.confirmRegistration(token);

		assertEquals(expectedMessage, result);
		verify(emailTokenService).confirmEmail(token);
	}

	@Test
	void confirmEmailChange_Success() {
		String token = "emailChangeToken";
		UserProfileResponse expectedResponse = new UserProfileResponse();

		when(emailTokenService.confirmEmailChange(token)).thenReturn(testUser);
		when(userMapper.toUserProfileResponse(testUser)).thenReturn(expectedResponse);

		UserProfileResponse result = userService.confirmEmailChange(token);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(emailTokenService).confirmEmailChange(token);
		verify(userMapper).toUserProfileResponse(testUser);
	}

	@Test
	void getUserProfile_Success() {
		UserProfileResponse expectedResponse = new UserProfileResponse();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userMapper.toUserProfileResponse(testUser)).thenReturn(expectedResponse);

		UserProfileResponse result = userService.getUserProfile(USER_ID);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(userMapper).toUserProfileResponse(testUser);
	}

	@Test
	void getUserProfile_UserNotFound() {
		Long nonExistentId = 999L;
		when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.getUserProfile(nonExistentId));
	}

	@Test
	void getUserById_Success() {
		UserResponse expectedResponse = new UserResponse();

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userMapper.toUserResponse(testUser)).thenReturn(expectedResponse);

		UserResponse result = userService.getUserById(USER_ID);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(userMapper).toUserResponse(testUser);
	}

	@Test
	void getById_Success() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

		User result = userService.getById(USER_ID);

		assertEquals(testUser, result);
	}

	@Test
	void getById_UserNotFound() {
		Long nonExistentId = 999L;
		when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.getById(nonExistentId));
	}

	@Test
	void getByEmail_Success() {
		when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));

		User result = userService.getByEmail(USER_EMAIL);

		assertEquals(testUser, result);
	}

	@Test
	void getByEmail_UserNotFound() {
		String nonExistentEmail = "nonexistent@example.com";
		when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.getByEmail(nonExistentEmail));
	}

	@Test
	void existsByEmail_True() {
		when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

		boolean result = userService.existsByEmail(USER_EMAIL);

		assertTrue(result);
	}

	@Test
	void existsById_True() {
		when(userRepository.existsById(USER_ID)).thenReturn(true);

		boolean result = userService.existsById(USER_ID);

		assertTrue(result);
	}

	@Test
	void updateUser_BirthDateNotChanged_VerificationNotRevoked() {
		UserUpdateRequest request = new UserUpdateRequest();
		request.setFirstName("Updated");
		request.setDateOfBirth(LocalDate.of(1990, 1, 1));

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(any(User.class))).thenReturn(testUser);
		when(userMapper.toUserProfileResponse(testUser)).thenReturn(new UserProfileResponse());

		userService.updateUser(USER_ID, request);

		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();
		assertEquals(VerificationStatus.VERIFIED, capturedUser.getVerificationStatus());
		assertNotNull(capturedUser.getVerifiedAt());
	}

	@Test
	void updateUser_NoBirthDateChange() {
		UserUpdateRequest request = new UserUpdateRequest();
		request.setFirstName("Updated");
		request.setDateOfBirth(null);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(any(User.class))).thenReturn(testUser);
		when(userMapper.toUserProfileResponse(testUser)).thenReturn(new UserProfileResponse());

		userService.updateUser(USER_ID, request);

		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();
		assertEquals(VerificationStatus.VERIFIED, capturedUser.getVerificationStatus());
		assertNotNull(capturedUser.getVerifiedAt());
	}

	@Test
	void updateUser_NullBirthDateWhenOriginalNull() {
		User userWithNullBirthDate = User.builder().id(USER_ID).email(USER_EMAIL).firstName("Anton").lastName("Bas")
				.phoneNumber(USER_PHONE).dateOfBirth(null).password(ENCODED_PASSWORD)
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(LocalDateTime.now()).city("Kyiv").build();

		UserUpdateRequest request = new UserUpdateRequest();
		request.setFirstName("Updated");
		request.setDateOfBirth(null);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userWithNullBirthDate));
		when(userRepository.save(any(User.class))).thenReturn(userWithNullBirthDate);
		when(userMapper.toUserProfileResponse(userWithNullBirthDate)).thenReturn(new UserProfileResponse());

		userService.updateUser(USER_ID, request);

		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();
		assertEquals(VerificationStatus.VERIFIED, capturedUser.getVerificationStatus());
		assertNotNull(capturedUser.getVerifiedAt());
	}

	@Test
	void updateUserPassword_ValidatesPasswordLength() {
		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPassword123");
		request.setNewPassword("validPassword123");
		request.setPasswordConfirm("validPassword123");

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(passwordEncoder.matches(request.getCurrentPassword(), ENCODED_PASSWORD)).thenReturn(true);
		when(passwordEncoder.matches(request.getNewPassword(), ENCODED_PASSWORD)).thenReturn(false);
		when(passwordEncoder.encode(request.getNewPassword())).thenReturn("newEncodedPassword");

		userService.updateUserPassword(USER_ID, request);

		verify(passwordEncoder).encode(request.getNewPassword());
		verify(userRepository).save(any(User.class));
	}

	@Test
	void updateUser_NotVerifiedUser_BirthDateChanged() {
		User notVerifiedUser = User.builder().id(USER_ID).email(USER_EMAIL).firstName("Anton").lastName("Bas")
				.phoneNumber(USER_PHONE).dateOfBirth(LocalDate.of(1990, 1, 1)).password(ENCODED_PASSWORD)
				.verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null).city("Kyiv").build();

		UserUpdateRequest request = new UserUpdateRequest();
		request.setDateOfBirth(LocalDate.of(1995, 5, 15));

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(notVerifiedUser));
		when(userRepository.save(any(User.class))).thenReturn(notVerifiedUser);
		when(userMapper.toUserProfileResponse(notVerifiedUser)).thenReturn(new UserProfileResponse());

		userService.updateUser(USER_ID, request);

		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();
		assertEquals(VerificationStatus.NOT_VERIFIED, capturedUser.getVerificationStatus());
		assertNull(capturedUser.getVerifiedAt());
	}
}
package ua.lviv.bas.cinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidCurrentPasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.PasswordMismatchException;
import ua.lviv.bas.cinema.exception.domain.auth.SameEmailException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.user.LastAdminException;
import ua.lviv.bas.cinema.exception.domain.user.SelfBlockException;
import ua.lviv.bas.cinema.exception.domain.user.SelfRoleChangeException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.common.EmailTokenGeneratorService;
import ua.lviv.bas.cinema.service.common.EmailTokenService;
import ua.lviv.bas.cinema.service.query.UserQueryService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserQueryService userQueryService;

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
				.userRole(UserRole.ROLE_USER).verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null)
				.enabled(false).password("encodedPassword").build();

		savedUser = User.builder().id(1L).email("test@example.com").firstName("Anton").lastName("Bas")
				.userRole(UserRole.ROLE_USER).verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null)
				.enabled(false).build();

		userProfileResponse = UserProfileResponse.builder().id(1L).email("test@example.com").firstName("Anton")
				.lastName("Bas").verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		userResponse = UserResponse.builder().id(1L).email("test@example.com").firstName("Anton").lastName("Bas")
				.userRole(UserRole.ROLE_USER).verificationStatus(VerificationStatus.NOT_VERIFIED).enabled(false)
				.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void registerUser_ShouldSaveUserAndSendVerificationEmail_WhenValidData() {
		when(userQueryService.existsByEmail(validUserDto.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(validUserDto.getPassword())).thenReturn("encodedPassword");
		when(userMapper.toEntity(validUserDto)).thenReturn(user);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userMapper.toDto(savedUser)).thenReturn(userResponse);
		doAnswer(invocation -> {
			return null;
		}).when(emailTokenGeneratorService).generateVerificationToken(anyString());

		UserResponse result = userService.registerUser(validUserDto);

		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo("test@example.com");
		assertThat(result.getUserRole()).isEqualTo(UserRole.ROLE_USER);
		assertThat(result.isEnabled()).isFalse();

		verify(userQueryService).existsByEmail(validUserDto.getEmail());
		verify(passwordEncoder).encode(validUserDto.getPassword());
		verify(userMapper).toEntity(validUserDto);
		verify(userRepository).save(user);
		verify(userMapper).toDto(savedUser);
		verify(emailTokenGeneratorService).generateVerificationToken(savedUser.getEmail());
	}

	@Test
	void registerUser_ShouldThrowEmailAlreadyExistsException_WhenEmailExists() {
		when(userQueryService.existsByEmail(validUserDto.getEmail())).thenReturn(true);

		assertThatThrownBy(() -> userService.registerUser(validUserDto)).isInstanceOf(EmailAlreadyExistsException.class)
				.hasMessageContaining(validUserDto.getEmail());

		verify(userQueryService).existsByEmail(validUserDto.getEmail());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userMapper, never()).toEntity(any());
		verify(userRepository, never()).save(any());
		verify(emailTokenGeneratorService, never()).generateVerificationToken(anyString());
	}

	@Test
	void registerUser_ShouldThrowPasswordMismatchException_WhenPasswordsDoNotMatch() {
		UserRegistrationRequest requestWithMismatch = UserRegistrationRequest.builder().email("test@example.com")
				.password("password123").passwordConfirm("differentPassword").build();

		assertThatThrownBy(() -> userService.registerUser(requestWithMismatch))
				.isInstanceOf(PasswordMismatchException.class);

		verify(userQueryService, never()).existsByEmail(anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userMapper, never()).toEntity(any());
		verify(userRepository, never()).save(any());
		verify(emailTokenGeneratorService, never()).generateVerificationToken(anyString());
	}

	@Test
	void updateUser_ShouldUpdateUserDetails_WhenValidRequest() {
		Long userId = 1L;
		UserUpdateRequest updateRequest = UserUpdateRequest.builder().firstName("UpdatedName")
				.lastName("UpdatedLastName").city("Kyiv").phoneNumber("+380987654321").build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		doAnswer(invocation -> {
			UserUpdateRequest request = invocation.getArgument(0);
			User userToUpdate = invocation.getArgument(1);
			userToUpdate.setFirstName(request.getFirstName());
			userToUpdate.setLastName(request.getLastName());
			userToUpdate.setCity(request.getCity());
			userToUpdate.setPhoneNumber(request.getPhoneNumber());
			return null;
		}).when(userMapper).updateUserFromDto(updateRequest, user);

		when(userRepository.save(user)).thenReturn(savedUser);
		when(userMapper.toProfileResponse(savedUser)).thenReturn(userProfileResponse);

		UserProfileResponse result = userService.updateUser(userId, updateRequest);

		assertThat(result).isNotNull();
		verify(userRepository).findById(userId);
		verify(userMapper).updateUserFromDto(updateRequest, user);
		verify(userRepository).save(user);
		verify(userMapper).toProfileResponse(savedUser);
	}

	@Test
	void updateUser_ShouldThrowUserNotFoundException_WhenUserNotFound() {
		Long userId = 999L;
		UserUpdateRequest updateRequest = UserUpdateRequest.builder().build();
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
				.isInstanceOf(UserNotFoundException.class).hasMessageContaining(String.valueOf(userId));

		verify(userRepository).findById(userId);
		verify(userMapper, never()).updateUserFromDto(any(), any());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUser_ShouldRevokeVerification_WhenDateOfBirthChanged() {
		Long userId = 1L;
		LocalDate oldDate = LocalDate.of(2001, 8, 21);
		LocalDate newDate = LocalDate.of(1990, 1, 1);

		User verifiedUser = User.builder().id(userId).email("test@example.com").dateOfBirth(oldDate)
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(LocalDateTime.now()).build();

		UserUpdateRequest updateRequest = UserUpdateRequest.builder().dateOfBirth(newDate).firstName("Anton")
				.lastName("Bas").city("Lviv").phoneNumber("+380123456789").build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(verifiedUser));

		doAnswer(invocation -> {
			UserUpdateRequest request = invocation.getArgument(0);
			User userToUpdate = invocation.getArgument(1);
			userToUpdate.setDateOfBirth(request.getDateOfBirth());
			userToUpdate.setFirstName(request.getFirstName());
			userToUpdate.setLastName(request.getLastName());
			userToUpdate.setCity(request.getCity());
			userToUpdate.setPhoneNumber(request.getPhoneNumber());
			return null;
		}).when(userMapper).updateUserFromDto(updateRequest, verifiedUser);

		when(userRepository.save(verifiedUser)).thenReturn(verifiedUser);
		when(userMapper.toProfileResponse(verifiedUser)).thenReturn(userProfileResponse);

		UserProfileResponse result = userService.updateUser(userId, updateRequest);

		assertThat(result).isNotNull();
		assertThat(verifiedUser.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(verifiedUser.getVerifiedAt()).isNull();
		verify(userRepository).findById(userId);
		verify(userRepository).save(verifiedUser);
	}

	@Test
	void updateUser_ShouldNotRevokeVerification_WhenDateOfBirthNotChanged() {
		Long userId = 1L;
		LocalDate sameDate = LocalDate.of(2001, 8, 21);

		User verifiedUser = User.builder().id(userId).email("test@example.com").dateOfBirth(sameDate)
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(LocalDateTime.now()).build();

		UserUpdateRequest updateRequest = UserUpdateRequest.builder().dateOfBirth(sameDate).firstName("NewName")
				.build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(verifiedUser));

		doAnswer(invocation -> {
			UserUpdateRequest request = invocation.getArgument(0);
			User userToUpdate = invocation.getArgument(1);
			userToUpdate.setDateOfBirth(request.getDateOfBirth());
			userToUpdate.setFirstName(request.getFirstName());
			return null;
		}).when(userMapper).updateUserFromDto(updateRequest, verifiedUser);

		when(userRepository.save(verifiedUser)).thenReturn(verifiedUser);
		when(userMapper.toProfileResponse(verifiedUser)).thenReturn(userProfileResponse);

		UserProfileResponse result = userService.updateUser(userId, updateRequest);

		assertThat(result).isNotNull();
		assertThat(verifiedUser.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		assertThat(verifiedUser.getVerifiedAt()).isNotNull();
		verify(userRepository).findById(userId);
		verify(userRepository).save(verifiedUser);
	}

	@Test
	void requestEmailChange_ShouldGenerateToken_WhenNewEmailIsValid() {
		Long userId = 1L;
		String newEmail = "new@example.com";

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userQueryService.existsByEmail(newEmail)).thenReturn(false);
		doAnswer(invocation -> {
			return null;
		}).when(emailTokenGeneratorService).generateEmailChangeToken(anyString(), anyString());

		userService.requestEmailChange(userId, newEmail);

		verify(userRepository).findById(userId);
		verify(userQueryService).existsByEmail(newEmail);
		verify(emailTokenGeneratorService).generateEmailChangeToken(user.getEmail(), newEmail);
	}

	@Test
	void requestEmailChange_ShouldThrowSameEmailException_WhenNewEmailIsSame() {
		Long userId = 1L;
		String sameEmail = "test@example.com";

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.requestEmailChange(userId, sameEmail))
				.isInstanceOf(SameEmailException.class);

		verify(userRepository).findById(userId);
		verify(userQueryService, never()).existsByEmail(anyString());
		verify(emailTokenGeneratorService, never()).generateEmailChangeToken(anyString(), anyString());
	}

	@Test
	void requestEmailChange_ShouldThrowEmailAlreadyExistsException_WhenNewEmailExists() {
		Long userId = 1L;
		String existingEmail = "existing@example.com";

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userQueryService.existsByEmail(existingEmail)).thenReturn(true);

		assertThatThrownBy(() -> userService.requestEmailChange(userId, existingEmail))
				.isInstanceOf(EmailAlreadyExistsException.class);

		verify(userRepository).findById(userId);
		verify(userQueryService).existsByEmail(existingEmail);
		verify(emailTokenGeneratorService, never()).generateEmailChangeToken(anyString(), anyString());
	}

	@Test
	void updateUserPassword_ShouldUpdatePassword_WhenAllValidationsPass() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder().currentPassword("oldPassword123")
				.newPassword("newPassword123").passwordConfirm("newPassword123").build();

		user.setPassword("encodedOldPassword");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).thenReturn(false);
		when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword123");
		when(userRepository.save(user)).thenReturn(user);

		userService.updateUserPassword(userId, request);

		assertThat(user.getPassword()).isEqualTo("encodedNewPassword123");
		verify(passwordEncoder, times(2)).matches(anyString(), anyString());
		verify(passwordEncoder).encode(request.getNewPassword());
		verify(userRepository).save(user);
	}

	@Test
	void updateUserPassword_ShouldThrowPasswordMismatchException_WhenPasswordsDoNotMatch() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder().currentPassword("oldPassword123")
				.newPassword("newPassword123").passwordConfirm("differentPassword").build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.updateUserPassword(userId, request))
				.isInstanceOf(PasswordMismatchException.class);

		verify(userRepository).findById(userId);
		verify(passwordEncoder, never()).matches(anyString(), anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserPassword_ShouldThrowInvalidCurrentPasswordException_WhenCurrentPasswordIsWrong() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder().currentPassword("wrongPassword")
				.newPassword("newPassword123").passwordConfirm("newPassword123").build();

		user.setPassword("encodedOldPassword");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(false);

		assertThatThrownBy(() -> userService.updateUserPassword(userId, request))
				.isInstanceOf(InvalidCurrentPasswordException.class);

		verify(passwordEncoder).matches(request.getCurrentPassword(), user.getPassword());
		verify(passwordEncoder, never()).matches(request.getNewPassword(), user.getPassword());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserPassword_ShouldThrowSamePasswordException_WhenNewPasswordSameAsOld() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder().currentPassword("oldPassword123")
				.newPassword("oldPassword123").passwordConfirm("oldPassword123").build();

		user.setPassword("encodedOldPassword");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).thenReturn(true);

		assertThatThrownBy(() -> userService.updateUserPassword(userId, request))
				.isInstanceOf(SamePasswordException.class);

		verify(passwordEncoder, times(2)).matches(anyString(), anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserPassword_ShouldThrowIllegalArgumentException_WhenPasswordTooShort() {
		Long userId = 1L;
		UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder().currentPassword("oldPassword123")
				.newPassword("short").passwordConfirm("short").build();

		user.setPassword("encodedOldPassword");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).thenReturn(false);

		assertThatThrownBy(() -> userService.updateUserPassword(userId, request))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 8 characters");

		verify(passwordEncoder, times(2)).matches(anyString(), anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserRole_ShouldUpdateRole_WhenNotChangingSelfAndNotLastAdmin() {
		Long userId = 1L;
		UserRole newRole = UserRole.ROLE_USER;
		User adminUser = User.builder().id(1L).email("admin@example.com").userRole(UserRole.ROLE_ADMIN).build();

		setupSecurityContext("different@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByUserRole(UserRole.ROLE_ADMIN)).thenReturn(2L);
		when(userRepository.save(adminUser)).thenReturn(adminUser);

		userService.updateUserRole(userId, newRole);

		assertThat(adminUser.getUserRole()).isEqualTo(newRole);
		verify(userRepository).findById(userId);
		verify(userRepository).countByUserRole(UserRole.ROLE_ADMIN);
		verify(userRepository).save(adminUser);
	}

	@Test
	void updateUserRole_ShouldThrowSelfRoleChangeException_WhenChangingSelfRole() {
		Long userId = 1L;
		User currentUser = User.builder().id(1L).email("current@example.com").userRole(UserRole.ROLE_ADMIN).build();

		setupSecurityContext("current@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));

		assertThatThrownBy(() -> userService.updateUserRole(userId, UserRole.ROLE_USER))
				.isInstanceOf(SelfRoleChangeException.class);

		verify(userRepository).findById(userId);
		verify(userRepository, never()).countByUserRole(any());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserRole_ShouldThrowLastAdminException_WhenRemovingLastAdmin() {
		Long userId = 1L;
		User adminUser = User.builder().id(1L).email("admin@example.com").userRole(UserRole.ROLE_ADMIN).build();

		setupSecurityContext("different@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByUserRole(UserRole.ROLE_ADMIN)).thenReturn(1L);

		assertThatThrownBy(() -> userService.updateUserRole(userId, UserRole.ROLE_USER))
				.isInstanceOf(LastAdminException.class);

		verify(userRepository).findById(userId);
		verify(userRepository).countByUserRole(UserRole.ROLE_ADMIN);
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserStatus_ShouldUpdateStatus_WhenNotBlockingSelf() {
		Long userId = 1L;
		boolean enabled = true;

		setupSecurityContext("different@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		userService.updateUserStatus(userId, enabled);

		assertThat(user.isEnabled()).isEqualTo(enabled);
		verify(userRepository).findById(userId);
		verify(userRepository).save(user);
	}

	@Test
	void updateUserStatus_ShouldThrowSelfBlockException_WhenBlockingSelf() {
		Long userId = 1L;
		boolean enabled = false;

		setupSecurityContext("test@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.updateUserStatus(userId, enabled)).isInstanceOf(SelfBlockException.class);

		verify(userRepository).findById(userId);
		verify(userRepository, never()).save(any());
	}

	@Test
	void getUserProfile_ShouldReturnUserProfile() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toProfileResponse(user)).thenReturn(userProfileResponse);

		UserProfileResponse result = userService.getUserProfile(userId);

		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo("test@example.com");
		verify(userRepository).findById(userId);
		verify(userMapper).toProfileResponse(user);
	}

	@Test
	void getUserById_ShouldReturnUser() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toDto(user)).thenReturn(userResponse);

		UserResponse result = userService.getUserById(userId);

		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo("test@example.com");
		verify(userRepository).findById(userId);
		verify(userMapper).toDto(user);
	}

	@Test
	void findById_ShouldReturnUser_WhenUserExists() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		User result = userService.findById(userId);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(userId);
		verify(userRepository).findById(userId);
	}

	@Test
	void findById_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
		Long userId = 999L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.findById(userId)).isInstanceOf(UserNotFoundException.class);

		verify(userRepository).findById(userId);
	}

	@Test
	void findByEmail_ShouldReturnUser_WhenUserExists() {
		String email = "test@example.com";
		when(userQueryService.findByEmail(email)).thenReturn(Optional.of(user));

		User result = userService.findByEmail(email);

		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo(email);
		verify(userQueryService).findByEmail(email);
	}

	@Test
	void findByEmail_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
		String email = "nonexistent@example.com";
		when(userQueryService.findByEmail(email)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.findByEmail(email)).isInstanceOf(UserNotFoundException.class);

		verify(userQueryService).findByEmail(email);
	}

	@Test
	void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
		String email = "existing@example.com";
		when(userQueryService.existsByEmail(email)).thenReturn(true);

		boolean result = userService.existsByEmail(email);

		assertThat(result).isTrue();
		verify(userQueryService).existsByEmail(email);
	}

	@Test
	void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
		String email = "nonexistent@example.com";
		when(userQueryService.existsByEmail(email)).thenReturn(false);

		boolean result = userService.existsByEmail(email);

		assertThat(result).isFalse();
		verify(userQueryService).existsByEmail(email);
	}

	@Test
	void findAllForAdmin_ShouldReturnFilteredUsers() {
		String search = "test";
		UserRole role = UserRole.ROLE_USER;
		Boolean enabled = true;
		Pageable pageable = PageRequest.of(0, 10);
		Page<User> userPage = new PageImpl<>(List.of(user));
		AdminUserListResponse adminResponse = AdminUserListResponse.builder().id(1L).email("test@example.com")
				.verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		when(userQueryService.findFilteredUsers(search, role, enabled, pageable)).thenReturn(userPage);
		when(userMapper.toAdminListDto(user)).thenReturn(adminResponse);

		Page<AdminUserListResponse> result = userService.findAllForAdmin(search, role, enabled, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
		verify(userQueryService).findFilteredUsers(search, role, enabled, pageable);
		verify(userMapper).toAdminListDto(user);
	}

	@Test
	void confirmRegistration_ShouldCallEmailTokenService() {
		String token = "verification-token";
		String expectedResponse = "Email verified successfully";
		when(emailTokenService.confirmEmail(token)).thenReturn(expectedResponse);

		String result = userService.confirmRegistration(token);

		assertThat(result).isEqualTo(expectedResponse);
		verify(emailTokenService).confirmEmail(token);
	}

	@Test
	void confirmEmailChange_ShouldReturnUpdatedProfile() {
		String token = "email-change-token";
		User updatedUser = User.builder().id(1L).email("new@example.com").build();
		UserProfileResponse response = UserProfileResponse.builder().id(1L).email("new@example.com").build();

		when(emailTokenService.confirmEmailChange(token)).thenReturn(updatedUser);
		when(userMapper.toProfileResponse(updatedUser)).thenReturn(response);

		UserProfileResponse result = userService.confirmEmailChange(token);

		assertThat(result.getEmail()).isEqualTo("new@example.com");
		verify(emailTokenService).confirmEmailChange(token);
		verify(userMapper).toProfileResponse(updatedUser);
	}

	@Test
	void findAllActiveAdmins_ShouldReturnActiveAdmins() {
		List<User> admins = List.of(user);
		when(userQueryService.findAllActiveByRole(UserRole.ROLE_ADMIN)).thenReturn(admins);

		List<User> result = userService.findAllActiveAdmins();

		assertThat(result).hasSize(1);
		verify(userQueryService).findAllActiveByRole(UserRole.ROLE_ADMIN);
	}

	@Test
	void findAllActiveUsers_ShouldReturnActiveUsers() {
		List<User> users = List.of(user);
		when(userQueryService.findAllActiveUsers()).thenReturn(users);

		List<User> result = userService.findAllActiveUsers();

		assertThat(result).hasSize(1);
		verify(userQueryService).findAllActiveUsers();
	}

	@Test
	void existsById_ShouldReturnTrue_WhenUserExists() {
		Long userId = 1L;
		when(userRepository.existsById(userId)).thenReturn(true);

		boolean result = userService.existsById(userId);

		assertThat(result).isTrue();
		verify(userRepository).existsById(userId);
	}

	@Test
	void existsById_ShouldReturnFalse_WhenUserDoesNotExist() {
		Long userId = 999L;
		when(userRepository.existsById(userId)).thenReturn(false);

		boolean result = userService.existsById(userId);

		assertThat(result).isFalse();
		verify(userRepository).existsById(userId);
	}

	@Test
	void updateBirthDateVerification_ShouldVerifyBirthDate_WhenStatusIsVERIFIED() {
		Long userId = 1L;
		VerificationBirthDateRequest request = VerificationBirthDateRequest.builder()
				.verificationStatus(VerificationStatus.VERIFIED).build();

		User unverifiedUser = User.builder().id(userId).email("test@example.com")
				.verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null).build();

		User verifiedUser = User.builder().id(userId).email("test@example.com")
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(LocalDateTime.now()).build();

		UserResponse expectedResponse = UserResponse.builder().id(userId).email("test@example.com")
				.verificationStatus(VerificationStatus.VERIFIED).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(unverifiedUser));
		when(userRepository.save(any(User.class))).thenReturn(verifiedUser);
		when(userMapper.toDto(verifiedUser)).thenReturn(expectedResponse);

		UserResponse result = userService.updateBirthDateVerification(userId, request);

		assertThat(result).isNotNull();
		assertThat(result.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		verify(userRepository).findById(userId);
		verify(userRepository).save(unverifiedUser);
		verify(userMapper).toDto(verifiedUser);

		assertThat(unverifiedUser.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		assertThat(unverifiedUser.getVerifiedAt()).isNotNull();
	}

	@Test
	void updateBirthDateVerification_ShouldUnverifyBirthDate_WhenStatusIsNOT_VERIFIED() {
		Long userId = 1L;
		LocalDateTime oldVerifiedAt = LocalDateTime.of(2024, 1, 1, 10, 0);

		VerificationBirthDateRequest request = VerificationBirthDateRequest.builder()
				.verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		User verifiedUser = User.builder().id(userId).email("test@example.com")
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(oldVerifiedAt).build();

		User unverifiedUser = User.builder().id(userId).email("test@example.com")
				.verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null).build();

		UserResponse expectedResponse = UserResponse.builder().id(userId).email("test@example.com")
				.verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(verifiedUser));
		when(userRepository.save(any(User.class))).thenReturn(unverifiedUser);
		when(userMapper.toDto(unverifiedUser)).thenReturn(expectedResponse);

		UserResponse result = userService.updateBirthDateVerification(userId, request);

		assertThat(result).isNotNull();
		assertThat(result.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		verify(userRepository).findById(userId);
		verify(userRepository).save(verifiedUser);
		verify(userMapper).toDto(unverifiedUser);

		assertThat(verifiedUser.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(verifiedUser.getVerifiedAt()).isNull();
	}

	@Test
	void updateBirthDateVerification_ShouldKeepVerifiedAt_WhenAlreadyVerified() {
		Long userId = 1L;
		LocalDateTime existingVerifiedAt = LocalDateTime.of(2024, 1, 1, 10, 0);

		VerificationBirthDateRequest request = VerificationBirthDateRequest.builder()
				.verificationStatus(VerificationStatus.VERIFIED).build();

		User alreadyVerifiedUser = User.builder().id(userId).email("test@example.com")
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(existingVerifiedAt).build();

		UserResponse expectedResponse = UserResponse.builder().id(userId).email("test@example.com")
				.verificationStatus(VerificationStatus.VERIFIED).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(alreadyVerifiedUser));
		when(userRepository.save(any(User.class))).thenReturn(alreadyVerifiedUser);
		when(userMapper.toDto(alreadyVerifiedUser)).thenReturn(expectedResponse);

		UserResponse result = userService.updateBirthDateVerification(userId, request);

		assertThat(result).isNotNull();
		assertThat(result.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		verify(userRepository).findById(userId);
		verify(userRepository).save(alreadyVerifiedUser);
		verify(userMapper).toDto(alreadyVerifiedUser);

		assertThat(alreadyVerifiedUser.getVerifiedAt()).isEqualTo(existingVerifiedAt);
	}

	@Test
	void updateBirthDateVerification_ShouldThrowUserNotFoundException_WhenUserNotFound() {
		Long userId = 999L;
		VerificationBirthDateRequest request = VerificationBirthDateRequest.builder()
				.verificationStatus(VerificationStatus.VERIFIED).build();

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.updateBirthDateVerification(userId, request))
				.isInstanceOf(UserNotFoundException.class).hasMessageContaining(String.valueOf(userId));

		verify(userRepository).findById(userId);
		verify(userRepository, never()).save(any());
		verify(userMapper, never()).toDto(any());
	}

	private void setupSecurityContext(String email) {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null);
		SecurityContext context = new SecurityContextImpl();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}
}
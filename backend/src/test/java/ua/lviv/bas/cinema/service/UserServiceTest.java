package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
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
	void requestEmailChange_ShouldThrowException_WhenSameEmail() {
		Long userId = 1L;
		String sameEmail = "test@example.com";

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThrows(SameEmailException.class, () -> userService.requestEmailChange(userId, sameEmail));

		verify(userRepository).findById(userId);
		verify(userRepository, never()).existsByEmail(anyString());
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
		String passwordConfirm = "newPassword123";
		String encodedNewPassword = "encodedNewPassword";

		user.setPassword("encodedOldPassword");

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(false);
		when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
		when(userRepository.save(user)).thenReturn(user);

		assertDoesNotThrow(
				() -> userService.updateUserPassword(user.getId(), currentPassword, newPassword, passwordConfirm));

		assertEquals(encodedNewPassword, user.getPassword());

		verify(passwordEncoder, times(2)).matches(anyString(), anyString());
		verify(passwordEncoder).encode(newPassword);
		verify(userRepository).save(user);
	}

	@Test
	void updateUserPassword_ShouldThrowException_WhenCurrentPasswordIsIncorrect() {
		String currentPassword = "wrongPassword";
		String newPassword = "newPassword123";
		String passwordConfirm = "newPassword123";

		user.setPassword("encodedOldPassword");

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(false);

		assertThrows(InvalidCurrentPasswordException.class,
				() -> userService.updateUserPassword(user.getId(), currentPassword, newPassword, passwordConfirm));

		verify(passwordEncoder, times(1)).matches(anyString(), anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserPassword_ShouldThrowException_WhenNewPasswordIsSameAsOld() {
		String currentPassword = "oldPassword123";
		String newPassword = "oldPassword123";
		String passwordConfirm = "oldPassword123";

		user.setPassword("encodedOldPassword");

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(true);

		assertThrows(SamePasswordException.class,
				() -> userService.updateUserPassword(user.getId(), currentPassword, newPassword, passwordConfirm));

		verify(passwordEncoder, times(2)).matches(anyString(), anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserPassword_ShouldThrowException_WhenPasswordsDoNotMatch() {
		String currentPassword = "oldPassword123";
		String newPassword = "newPassword123";
		String passwordConfirm = "differentPassword";

		user.setPassword("encodedOldPassword");

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		assertThrows(PasswordMismatchException.class,
				() -> userService.updateUserPassword(user.getId(), currentPassword, newPassword, passwordConfirm));

		verify(passwordEncoder, never()).matches(anyString(), anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserPassword_ShouldThrowException_WhenPasswordTooShort() {
		String currentPassword = "oldPassword123";
		String newPassword = "short";
		String passwordConfirm = "short";

		user.setPassword("encodedOldPassword");

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(false);

		assertThrows(IllegalArgumentException.class,
				() -> userService.updateUserPassword(user.getId(), currentPassword, newPassword, passwordConfirm));

		verify(passwordEncoder, times(2)).matches(anyString(), anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
	}

	@Test
	void getUserProfile_ShouldReturnUserProfile() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toProfileResponse(user)).thenReturn(userProfileResponse);

		UserProfileResponse result = userService.getUserProfile(userId);

		assertNotNull(result);
		assertEquals("test@example.com", result.getEmail());
		verify(userRepository).findById(userId);
		verify(userMapper).toProfileResponse(user);
	}

	@Test
	void getUserById_ShouldReturnUser() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toDto(user)).thenReturn(userResponse);

		UserResponse result = userService.getUserById(userId);

		assertNotNull(result);
		assertEquals("test@example.com", result.getEmail());
		verify(userRepository).findById(userId);
		verify(userMapper).toDto(user);
	}

	@Test
	void findByEmail_ShouldReturnUser() {
		String email = "test@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		User result = userService.findByEmail(email);

		assertNotNull(result);
		assertEquals(email, result.getEmail());
		verify(userRepository).findByEmail(email);
	}

	@Test
	void findByEmail_ShouldThrowException_WhenUserNotFound() {
		String email = "nonexistent@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.findByEmail(email));
		verify(userRepository).findByEmail(email);
	}

	@Test
	void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
		String email = "existing@example.com";
		when(userRepository.existsByEmail(email)).thenReturn(true);

		boolean result = userService.existsByEmail(email);

		assertTrue(result);
		verify(userRepository).existsByEmail(email);
	}

	@Test
	void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
		String email = "nonexistent@example.com";
		when(userRepository.existsByEmail(email)).thenReturn(false);

		boolean result = userService.existsByEmail(email);

		assertFalse(result);
		verify(userRepository).existsByEmail(email);
	}

	@Test
	void existsById_ShouldReturnTrue_WhenIdExists() {
		Long userId = 1L;
		when(userRepository.existsById(userId)).thenReturn(true);

		boolean result = userService.existsById(userId);

		assertTrue(result);
		verify(userRepository).existsById(userId);
	}

	@Test
	void existsById_ShouldReturnFalse_WhenIdNotExists() {
		Long userId = 999L;
		when(userRepository.existsById(userId)).thenReturn(false);

		boolean result = userService.existsById(userId);

		assertFalse(result);
		verify(userRepository).existsById(userId);
	}

	@Test
	void confirmRegistration_ShouldCallEmailTokenService() {
		String token = "verification-token";
		when(emailTokenService.confirmEmail(token)).thenReturn("Email verified successfully");

		String result = userService.confirmRegistration(token);

		assertNotNull(result);
		verify(emailTokenService).confirmEmail(token);
	}

	@Test
	void updateUserRole_ShouldUpdateRole_WhenValidChange() {
		Long userId = 1L;
		User adminUser = User.builder().id(1L).userRole(UserRole.ROLE_ADMIN).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByUserRole(UserRole.ROLE_ADMIN)).thenReturn(2L);
		when(userRepository.save(adminUser)).thenReturn(adminUser);

		userService.updateUserRole(userId, UserRole.ROLE_USER);

		verify(userRepository).findById(userId);
		verify(userRepository).countByUserRole(UserRole.ROLE_ADMIN);
		verify(userRepository).save(adminUser);
	}

	@Test
	void updateUserRole_ShouldThrowLastAdminException_WhenRemovingLastAdmin() {
		Long userId = 1L;
		UserRole newRole = UserRole.ROLE_USER;
		User adminUser = User.builder().id(1L).userRole(UserRole.ROLE_ADMIN).build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByUserRole(UserRole.ROLE_ADMIN)).thenReturn(1L);

		assertThrows(LastAdminException.class, () -> userService.updateUserRole(userId, newRole));

		verify(userRepository).findById(userId);
		verify(userRepository).countByUserRole(UserRole.ROLE_ADMIN);
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserStatus_ShouldUpdateStatus_WhenValidUser() {
		Long userId = 1L;
		boolean enabled = true;

		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("different@example.com");
		SecurityContextHolder.setContext(securityContext);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		assertDoesNotThrow(() -> userService.updateUserStatus(userId, enabled));

		assertEquals(enabled, user.isEnabled());
		verify(userRepository).findById(userId);
		verify(userRepository).save(user);

		SecurityContextHolder.clearContext();
	}

	@Test
	void updateUserStatus_ShouldThrowSelfBlockException_WhenBlockingSelf() {
		Long userId = 1L;
		boolean enabled = false;

		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("test@example.com");
		SecurityContextHolder.setContext(securityContext);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThrows(SelfBlockException.class, () -> userService.updateUserStatus(userId, enabled));

		verify(userRepository).findById(userId);
		verify(userRepository, never()).save(any());

		SecurityContextHolder.clearContext();
	}

	@Test
	void updateUserStatus_ShouldHandleNullAuthentication() {
		Long userId = 1L;
		boolean enabled = true;

		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("different@example.com");
		SecurityContextHolder.setContext(securityContext);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		userService.updateUserStatus(userId, enabled);

		assertEquals(enabled, user.isEnabled());
		verify(userRepository).findById(userId);
		verify(userRepository).save(user);

		SecurityContextHolder.clearContext();
	}

	@Test
	void findAllForAdmin_ShouldReturnFilteredUsers() {
		String search = "test";
		UserRole role = UserRole.ROLE_USER;
		Boolean enabled = true;
		Pageable pageable = PageRequest.of(0, 10);
		Page<User> userPage = new PageImpl<>(List.of(user));
		AdminUserListResponse adminResponse = AdminUserListResponse.builder().id(1L).email("test@example.com").build();

		when(userRepository.findByFilters(search, role, enabled, pageable)).thenReturn(userPage);
		when(userMapper.toAdminListDto(user)).thenReturn(adminResponse);

		Page<AdminUserListResponse> result = userService.findAllForAdmin(search, role, enabled, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		verify(userRepository).findByFilters(search, role, enabled, pageable);
		verify(userMapper).toAdminListDto(user);
	}

	@Test
	void findAllForAdmin_ShouldReturnAllUsers_WhenNoFilters() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<User> userPage = new PageImpl<>(List.of(user));
		AdminUserListResponse adminResponse = AdminUserListResponse.builder().id(1L).email("test@example.com").build();

		when(userRepository.findByFilters(null, null, null, pageable)).thenReturn(userPage);
		when(userMapper.toAdminListDto(user)).thenReturn(adminResponse);

		Page<AdminUserListResponse> result = userService.findAllForAdmin(null, null, null, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		verify(userRepository).findByFilters(null, null, null, pageable);
	}

	@Test
	void findAllForAdmin_ShouldReturnDisabledUsers_WhenEnabledFalse() {
		Boolean enabled = false;
		Pageable pageable = PageRequest.of(0, 10);
		Page<User> userPage = new PageImpl<>(List.of(user));
		AdminUserListResponse adminResponse = AdminUserListResponse.builder().id(1L).email("test@example.com").build();

		when(userRepository.findByFilters(null, null, enabled, pageable)).thenReturn(userPage);
		when(userMapper.toAdminListDto(user)).thenReturn(adminResponse);

		Page<AdminUserListResponse> result = userService.findAllForAdmin(null, null, enabled, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		verify(userRepository).findByFilters(null, null, enabled, pageable);
	}

	@Test
	void findAllForAdmin_ShouldReturnAdmins_WhenRoleAdmin() {
		UserRole role = UserRole.ROLE_ADMIN;
		Pageable pageable = PageRequest.of(0, 10);
		Page<User> userPage = new PageImpl<>(List.of(user));
		AdminUserListResponse adminResponse = AdminUserListResponse.builder().id(1L).email("test@example.com").build();

		when(userRepository.findByFilters(null, role, null, pageable)).thenReturn(userPage);
		when(userMapper.toAdminListDto(user)).thenReturn(adminResponse);

		Page<AdminUserListResponse> result = userService.findAllForAdmin(null, role, null, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		verify(userRepository).findByFilters(null, role, null, pageable);
	}
}
package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserEmailChangeRequest;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.user.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private UserController userController;

	private CustomUserDetails createMockUserDetails(Long userId) {
		User user = User.builder().id(userId).email("user@example.com").firstName("John").lastName("Doe")
				.dateOfBirth(LocalDate.of(1990, 1, 1)).city("Kyiv").phoneNumber("+380123456789").password("password")
				.userRole(UserRole.ROLE_USER).enabled(true).build();

		return new CustomUserDetails(user);
	}

	@Test
	void getProfile_ShouldReturnUserProfile() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserProfileResponse profileResponse = UserProfileResponse.builder().id(userId).email("user@example.com")
				.firstName("John").lastName("Doe").dateOfBirth(LocalDate.of(1990, 1, 1)).city("Kyiv")
				.phoneNumber("+380123456789").verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		when(userService.getUserProfile(userId)).thenReturn(profileResponse);

		ResponseEntity<UserProfileResponse> response = userController.getProfile(userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		UserProfileResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(userId, responseBody.getId());
		assertEquals("John", responseBody.getFirstName());
		assertEquals("Doe", responseBody.getLastName());

		verify(userService).getUserProfile(userId);
	}

	@Test
	void getProfile_WhenUserDetailsIsNull_ShouldReturnUnauthorized() {
		ResponseEntity<UserProfileResponse> response = userController.getProfile(null);

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	void getProfile_WhenUserNotFound_ShouldThrowException() {
		Long userId = 999L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		when(userService.getUserProfile(userId)).thenThrow(new UserNotFoundException(userId));

		assertThrows(UserNotFoundException.class, () -> userController.getProfile(userDetails));
		verify(userService).getUserProfile(userId);
	}

	@Test
	void updateProfile_ShouldUpdateProfileSuccessfully() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserUpdateRequest request = UserUpdateRequest.builder().firstName("Updated").lastName("Name")
				.phoneNumber("+987654321").dateOfBirth(LocalDate.of(1995, 5, 5)).city("Lviv").build();

		UserProfileResponse profileResponse = UserProfileResponse.builder().id(userId).email("user@example.com")
				.firstName("Updated").lastName("Name").dateOfBirth(LocalDate.of(1995, 5, 5)).city("Lviv")
				.phoneNumber("+987654321").verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		when(userService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(profileResponse);

		ResponseEntity<UserProfileResponse> response = userController.updateProfile(userDetails, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		UserProfileResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals("Updated", responseBody.getFirstName());
		assertEquals("Name", responseBody.getLastName());
		assertEquals("+987654321", responseBody.getPhoneNumber());
		assertEquals("Lviv", responseBody.getCity());

		verify(userService).updateUser(userId, request);
	}

	@Test
	void updateProfile_WhenUserNotFound_ShouldThrowException() {
		Long userId = 999L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserUpdateRequest request = UserUpdateRequest.builder().firstName("Updated").lastName("Name").build();

		when(userService.updateUser(eq(userId), any(UserUpdateRequest.class)))
				.thenThrow(new UserNotFoundException(userId));

		assertThrows(UserNotFoundException.class, () -> userController.updateProfile(userDetails, request));
		verify(userService).updateUser(userId, request);
	}

	@Test
	void requestEmailChange_ShouldSendEmailChangeRequest() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserEmailChangeRequest request = new UserEmailChangeRequest();
		request.setPassword("password");
		request.setNewEmail("new.email@example.com");

		ResponseEntity<Map<String, String>> response = userController.requestEmailChange(userDetails, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Map<String, String> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals("Confirmation email sent to your new address", responseBody.get("message"));

		verify(userService).requestEmailChange(userId, request.getPassword(), request.getNewEmail());
	}

	@Test
	void requestEmailChange_WhenPasswordIncorrect_ShouldThrowException() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserEmailChangeRequest request = new UserEmailChangeRequest();
		request.setPassword("wrongpassword");
		request.setNewEmail("new.email@example.com");

		doThrow(new BadCredentialsException("Invalid password")).when(userService).requestEmailChange(userId,
				request.getPassword(), request.getNewEmail());

		assertThrows(BadCredentialsException.class, () -> userController.requestEmailChange(userDetails, request));
		verify(userService).requestEmailChange(userId, request.getPassword(), request.getNewEmail());
	}

	@Test
	void requestEmailChange_WhenEmailAlreadyInUse_ShouldThrowException() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserEmailChangeRequest request = new UserEmailChangeRequest();
		request.setPassword("password");
		request.setNewEmail("existing@example.com");

		doThrow(new IllegalArgumentException("Email already in use")).when(userService).requestEmailChange(userId,
				request.getPassword(), request.getNewEmail());

		assertThrows(IllegalArgumentException.class, () -> userController.requestEmailChange(userDetails, request));
		verify(userService).requestEmailChange(userId, request.getPassword(), request.getNewEmail());
	}

	@Test
	void updatePassword_ShouldUpdatePasswordSuccessfully() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPassword123");
		request.setNewPassword("newPassword123");
		request.setPasswordConfirm("newPassword123");

		ResponseEntity<Map<String, String>> response = userController.updatePassword(userDetails, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		Map<String, String> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals("Password updated successfully", responseBody.get("message"));

		verify(userService).updateUserPassword(userId, request);
	}

	@Test
	void updatePassword_WhenCurrentPasswordIncorrect_ShouldThrowException() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("wrongPassword");
		request.setNewPassword("newPassword123");
		request.setPasswordConfirm("newPassword123");

		doThrow(new BadCredentialsException("Invalid current password")).when(userService).updateUserPassword(userId,
				request);

		assertThrows(BadCredentialsException.class, () -> userController.updatePassword(userDetails, request));
		verify(userService).updateUserPassword(userId, request);
	}

	@Test
	void updatePassword_WhenPasswordsDoNotMatch_ShouldThrowException() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPassword123");
		request.setNewPassword("newPassword123");
		request.setPasswordConfirm("differentPassword");

		doThrow(new IllegalArgumentException("Passwords do not match")).when(userService).updateUserPassword(userId,
				request);

		assertThrows(IllegalArgumentException.class, () -> userController.updatePassword(userDetails, request));
		verify(userService).updateUserPassword(userId, request);
	}

	@Test
	void updatePassword_WhenUserNotFound_ShouldThrowException() {
		Long userId = 999L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPassword123");
		request.setNewPassword("newPassword123");
		request.setPasswordConfirm("newPassword123");

		doThrow(new UserNotFoundException(userId)).when(userService).updateUserPassword(userId, request);

		assertThrows(UserNotFoundException.class, () -> userController.updatePassword(userDetails, request));
		verify(userService).updateUserPassword(userId, request);
	}
}
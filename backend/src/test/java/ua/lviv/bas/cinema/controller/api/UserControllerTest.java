package ua.lviv.bas.cinema.controller.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserEmailChangeRequest;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.service.user.UserService;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
	void getProfileShouldReturnUserProfile() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserProfileResponse profileResponse = new UserProfileResponse(userId, "user@example.com", "John", "Doe",
				LocalDate.of(1990, 1, 1), "Kyiv", "+380123456789", VerificationStatus.NOT_VERIFIED);

		when(userService.getProfile(userId)).thenReturn(profileResponse);

		UserProfileResponse response = userController.getProfile(userDetails);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(userId);
		assertThat(response.firstName()).isEqualTo("John");
		assertThat(response.lastName()).isEqualTo("Doe");

		verify(userService).getProfile(userId);
	}

	@Test
	void getProfileWhenUserNotFoundShouldThrowException() {
		Long userId = 999L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		when(userService.getProfile(userId)).thenThrow(new UserNotFoundException(userId));

		assertThrows(UserNotFoundException.class, () -> userController.getProfile(userDetails));
		verify(userService).getProfile(userId);
	}

	@Test
	void updateProfileShouldUpdateProfileSuccessfully() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserUpdateRequest request = new UserUpdateRequest("Updated", "Name", LocalDate.of(1995, 5, 5), "Lviv",
				"+987654321");

		UserProfileResponse profileResponse = new UserProfileResponse(userId, "user@example.com", "Updated", "Name",
				LocalDate.of(1995, 5, 5), "Lviv", "+987654321", VerificationStatus.NOT_VERIFIED);

		when(userService.update(eq(userId), any(UserUpdateRequest.class))).thenReturn(profileResponse);

		UserProfileResponse response = userController.updateProfile(userDetails, request);

		assertThat(response).isNotNull();
		assertThat(response.firstName()).isEqualTo("Updated");
		assertThat(response.lastName()).isEqualTo("Name");
		assertThat(response.phoneNumber()).isEqualTo("+987654321");
		assertThat(response.city()).isEqualTo("Lviv");

		verify(userService).update(userId, request);
	}

	@Test
	void updateProfileWhenUserNotFoundShouldThrowException() {
		Long userId = 999L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserUpdateRequest request = new UserUpdateRequest("Updated", "Name", LocalDate.of(1995, 5, 5), "Lviv",
				"+987654321");

		when(userService.update(eq(userId), any(UserUpdateRequest.class))).thenThrow(new UserNotFoundException(userId));

		assertThrows(UserNotFoundException.class, () -> userController.updateProfile(userDetails, request));
		verify(userService).update(userId, request);
	}

	@Test
	void requestEmailChangeShouldSendEmailChangeRequest() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserEmailChangeRequest request = new UserEmailChangeRequest("new.email@example.com", "password");

		Map<String, String> response = userController.requestEmailChange(userDetails, request);

		assertThat(response).isNotNull();
		assertThat(response.get("message")).isEqualTo("Confirmation email sent to your new address");

		verify(userService).requestEmailChange(userId, request.password(), request.newEmail());
	}

	@Test
	void requestEmailChangeWhenPasswordIncorrectShouldThrowException() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserEmailChangeRequest request = new UserEmailChangeRequest("new.email@example.com", "wrong password");

		doThrow(new BadCredentialsException("Invalid password")).when(userService).requestEmailChange(userId,
				request.password(), request.newEmail());

		assertThrows(BadCredentialsException.class, () -> userController.requestEmailChange(userDetails, request));
		verify(userService).requestEmailChange(userId, request.password(), request.newEmail());
	}

	@Test
	void updatePasswordShouldUpdatePasswordSuccessfully() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("oldPassword123", "newPassword123",
				"newPassword123");

		Map<String, String> response = userController.updatePassword(userDetails, request);

		assertThat(response).isNotNull();
		assertThat(response.get("message")).isEqualTo("Password updated successfully");

		verify(userService).updatePassword(userId, request);
	}

	@Test
	void updatePasswordWhenCurrentPasswordIncorrectShouldThrowException() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("wrongPassword", "newPassword123",
				"newPassword123");

		doThrow(new BadCredentialsException("Invalid current password")).when(userService).updatePassword(userId,
				request);

		assertThrows(BadCredentialsException.class, () -> userController.updatePassword(userDetails, request));
		verify(userService).updatePassword(userId, request);
	}

	@Test
	void updatePasswordWhenPasswordsDoNotMatchShouldThrowException() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("oldPassword123", "newPassword123",
				"differentPassword");

		doThrow(new IllegalArgumentException("Passwords do not match")).when(userService).updatePassword(userId,
				request);

		assertThrows(IllegalArgumentException.class, () -> userController.updatePassword(userDetails, request));
		verify(userService).updatePassword(userId, request);
	}

	@Test
	void updatePasswordWhenUserNotFoundShouldThrowException() {
		Long userId = 999L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("oldPassword123", "newPassword123",
				"newPassword123");

		doThrow(new UserNotFoundException(userId)).when(userService).updatePassword(userId, request);

		assertThrows(UserNotFoundException.class, () -> userController.updatePassword(userDetails, request));
		verify(userService).updatePassword(userId, request);
	}
}
package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.user.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

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
				.phoneNumber("+380123456789").verificationStatus(VerificationStatus.VERIFIED).build();

		when(userService.getUserProfile(userId)).thenReturn(profileResponse);

		var response = userController.getProfile(userDetails);

		assertNotNull(response);
		assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

		UserProfileResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(userId, responseBody.getId());
		verify(userService).getUserProfile(userId);
	}

	@Test
	void updateProfile_ShouldUpdateProfileSuccessfully() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserUpdateRequest request = UserUpdateRequest.builder().firstName("Updated").lastName("Name")
				.phoneNumber("+987654321").dateOfBirth(LocalDate.of(1995, 5, 5)).build();

		UserProfileResponse profileResponse = UserProfileResponse.builder().id(userId).email("user@example.com")
				.firstName("Updated").lastName("Name").dateOfBirth(LocalDate.of(1995, 5, 5)).city("Kyiv")
				.phoneNumber("+987654321").verificationStatus(VerificationStatus.VERIFIED).build();

		when(userService.updateUser(eq(userId), any())).thenReturn(profileResponse);

		var response = userController.updateProfile(userDetails, request);

		assertNotNull(response);
		assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

		UserProfileResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals("Updated", responseBody.getFirstName());
		verify(userService).updateUser(userId, request);
	}

	@Test
	void requestEmailChange_ShouldSendEmailChangeRequest() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		String newEmail = "new.email@example.com";

		var response = userController.requestEmailChange(userDetails, newEmail);

		assertNotNull(response);
		assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

		Map<String, String> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals("Confirmation email sent to your new address", responseBody.get("message"));
		verify(userService).requestEmailChange(userId, newEmail);
	}

	@Test
	void confirmEmailChange_ShouldConfirmEmailChange() {
		String token = "test-token";

		UserProfileResponse profileResponse = UserProfileResponse.builder().id(1L).email("new.email@example.com")
				.firstName("John").lastName("Doe").dateOfBirth(LocalDate.of(1990, 1, 1)).city("Kyiv")
				.phoneNumber("+380123456789").verificationStatus(VerificationStatus.VERIFIED).build();

		when(userService.confirmEmailChange(token)).thenReturn(profileResponse);

		var response = userController.confirmEmailChange(token);

		assertNotNull(response);
		assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

		UserProfileResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals("new.email@example.com", responseBody.getEmail());
		verify(userService).confirmEmailChange(token);
	}

	@Test
	void updatePassword_ShouldUpdatePasswordSuccessfully() {
		Long userId = 1L;
		CustomUserDetails userDetails = createMockUserDetails(userId);

		UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
		request.setCurrentPassword("oldPassword123");
		request.setNewPassword("newPassword123");

		var response = userController.updatePassword(userDetails, request);

		assertNotNull(response);
		assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

		Map<String, String> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals("Password updated successfully", responseBody.get("message"));
		verify(userService).updateUserPassword(userId, request);
	}
}
package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

	private UserMapper userMapper = new UserMapperImpl();

	@Test
	void toEntity_ShouldMapAllFieldsCorrectly() {
		UserRegistrationRequest dto = UserRegistrationRequest.builder().email("anton@example.com").firstName("Anton")
				.lastName("Bas").dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.password("rawPassword").passwordConfirm("rawPassword").build();

		User user = userMapper.toEntity(dto);

		assertNotNull(user);
		assertEquals("anton@example.com", user.getEmail());
		assertEquals("Anton", user.getFirstName());
		assertEquals("Bas", user.getLastName());
		assertEquals(LocalDate.of(2001, 8, 21), user.getDateOfBirth());
		assertEquals("Lviv", user.getCity());
		assertEquals("+380123456789", user.getPhoneNumber());
		assertEquals(UserRole.ROLE_USER, user.getUserRole());
		assertEquals(VerificationStatus.NOT_VERIFIED, user.getVerificationStatus());
		assertNull(user.getVerifiedAt());
		assertFalse(user.isEnabled());
		assertNull(user.getPassword());
		assertNull(user.getId());
		assertNotNull(user.getTickets());
		assertTrue(user.getTickets().isEmpty());
		assertNull(user.getBonusCard());
		assertNotNull(user.getBookings());
		assertTrue(user.getBookings().isEmpty());
	}

	@Test
	void toEntity_ShouldReturnNull_WhenDtoIsNull() {
		User user = userMapper.toEntity(null);
		assertNull(user);
	}

	@Test
	void toDto_ShouldMapAllFields() {
		LocalDateTime verifiedAt = LocalDateTime.of(2024, 1, 15, 10, 30);

		User user = User.builder().id(1L).email("anton@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.userRole(UserRole.ROLE_USER).verificationStatus(VerificationStatus.VERIFIED).verifiedAt(verifiedAt)
				.enabled(true).createdAt(LocalDateTime.of(2024, 1, 1, 10, 0)).build();

		UserResponse dto = userMapper.toDto(user);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("anton@example.com", dto.getEmail());
		assertEquals("Anton", dto.getFirstName());
		assertEquals("Bas", dto.getLastName());
		assertEquals(LocalDate.of(2001, 8, 21), dto.getDateOfBirth());
		assertEquals("Lviv", dto.getCity());
		assertEquals("+380123456789", dto.getPhoneNumber());
		assertEquals(UserRole.ROLE_USER, dto.getUserRole());
		assertEquals(VerificationStatus.VERIFIED, dto.getVerificationStatus());
		assertTrue(dto.isEnabled());
		assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getCreatedAt());
	}

	@Test
	void toDto_ShouldReturnNull_WhenUserIsNull() {
		UserResponse dto = userMapper.toDto(null);
		assertNull(dto);
	}

	@Test
	void toProfileResponse_ShouldMapProfileFields() {
		LocalDateTime verifiedAt = LocalDateTime.of(2024, 1, 15, 10, 30);

		User user = User.builder().id(1L).email("anton@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(verifiedAt).build();

		UserProfileResponse dto = userMapper.toProfileResponse(user);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("anton@example.com", dto.getEmail());
		assertEquals("Anton", dto.getFirstName());
		assertEquals("Bas", dto.getLastName());
		assertEquals(LocalDate.of(2001, 8, 21), dto.getDateOfBirth());
		assertEquals("Lviv", dto.getCity());
		assertEquals("+380123456789", dto.getPhoneNumber());
		assertEquals(VerificationStatus.VERIFIED, dto.getVerificationStatus());
	}

	@Test
	void toProfileResponse_ShouldReturnNull_WhenUserIsNull() {
		UserProfileResponse dto = userMapper.toProfileResponse(null);
		assertNull(dto);
	}

	@Test
	void toAdminListDto_ShouldMapAllAdminListFields() {
		LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
		LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 14, 30);
		LocalDateTime verifiedAt = LocalDateTime.of(2024, 1, 10, 12, 0);

		User user = User.builder().id(1L).email("admin@example.com").firstName("Admin").lastName("User")
				.userRole(UserRole.ROLE_ADMIN).verificationStatus(VerificationStatus.VERIFIED).verifiedAt(verifiedAt)
				.enabled(true).createdAt(createdAt).updatedAt(updatedAt).tickets(new ArrayList<>()).build();

		AdminUserListResponse dto = userMapper.toAdminListDto(user);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("admin@example.com", dto.getEmail());
		assertEquals("Admin", dto.getFirstName());
		assertEquals("User", dto.getLastName());
		assertEquals(UserRole.ROLE_ADMIN, dto.getUserRole());
		assertEquals(VerificationStatus.VERIFIED, dto.getVerificationStatus());
		assertEquals(verifiedAt, dto.getVerifiedAt());
		assertTrue(dto.isEnabled());
		assertEquals(createdAt, dto.getCreatedAt());
		assertEquals(updatedAt, dto.getUpdatedAt());
		assertEquals(0, dto.getTicketsCount());
		assertEquals(updatedAt, dto.getLastActivity());
	}

	@Test
	void toAdminListDto_ShouldCalculateTicketsCount() {
		User user = User.builder().id(1L).email("user@example.com").firstName("Test").lastName("User")
				.userRole(UserRole.ROLE_USER).verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null)
				.enabled(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).tickets(new ArrayList<>())
				.build();

		user.getTickets().add(null);
		user.getTickets().add(null);

		AdminUserListResponse dto = userMapper.toAdminListDto(user);

		assertNotNull(dto);
		assertEquals(2, dto.getTicketsCount());
		assertEquals(VerificationStatus.NOT_VERIFIED, dto.getVerificationStatus());
		assertNull(dto.getVerifiedAt());
	}

	@Test
	void toAdminListDto_ShouldHandleDisabledUser() {
		User user = User.builder().id(1L).email("disabled@example.com").firstName("Disabled").lastName("User")
				.userRole(UserRole.ROLE_USER).verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null)
				.enabled(false).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).tickets(new ArrayList<>())
				.build();

		AdminUserListResponse dto = userMapper.toAdminListDto(user);

		assertNotNull(dto);
		assertFalse(dto.isEnabled());
		assertEquals("Disabled", dto.getFirstName());
		assertEquals("User", dto.getLastName());
		assertEquals(VerificationStatus.NOT_VERIFIED, dto.getVerificationStatus());
		assertNull(dto.getVerifiedAt());
	}

	@Test
	void updateUserFromDto_ShouldUpdateAllowedFieldsAndIgnoreEmail() {
		LocalDateTime verifiedAt = LocalDateTime.of(2023, 12, 1, 10, 0);

		User existingUser = User.builder().id(1L).email("old@example.com").firstName("OldFirstName")
				.lastName("OldLastName").dateOfBirth(LocalDate.of(1990, 1, 1)).city("OldCity")
				.phoneNumber("+380000000000").password("oldPassword").userRole(UserRole.ROLE_ADMIN)
				.verificationStatus(VerificationStatus.VERIFIED).verifiedAt(verifiedAt).enabled(true)
				.createdAt(LocalDateTime.of(2023, 1, 1, 10, 0)).updatedAt(LocalDateTime.of(2023, 1, 1, 10, 0))
				.tickets(new ArrayList<>()).bonusCard(null).bookings(new ArrayList<>()).build();

		UserUpdateRequest updateDto = UserUpdateRequest.builder().firstName("NewFirstName").lastName("NewLastName")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("NewCity").phoneNumber("+380123456789").build();

		userMapper.updateUserFromDto(updateDto, existingUser);

		assertEquals("NewFirstName", existingUser.getFirstName());
		assertEquals("NewLastName", existingUser.getLastName());
		assertEquals(LocalDate.of(2001, 8, 21), existingUser.getDateOfBirth());
		assertEquals("NewCity", existingUser.getCity());
		assertEquals("+380123456789", existingUser.getPhoneNumber());
		assertEquals("old@example.com", existingUser.getEmail());
		assertEquals("oldPassword", existingUser.getPassword());
		assertEquals(UserRole.ROLE_ADMIN, existingUser.getUserRole());
		assertEquals(VerificationStatus.VERIFIED, existingUser.getVerificationStatus());
		assertEquals(verifiedAt, existingUser.getVerifiedAt());
		assertTrue(existingUser.isEnabled());
		assertEquals(1L, existingUser.getId());
		assertNotNull(existingUser.getTickets());
		assertNull(existingUser.getBonusCard());
		assertNotNull(existingUser.getBookings());
		assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), existingUser.getCreatedAt());
		assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), existingUser.getUpdatedAt());
	}

	@Test
	void updateUserFromDto_ShouldHandleNullDto() {
		LocalDateTime verifiedAt = LocalDateTime.now();

		User existingUser = User.builder().id(1L).email("test@example.com").firstName("Test").lastName("User")
				.city("City").phoneNumber("+380000000000").verificationStatus(VerificationStatus.VERIFIED)
				.verifiedAt(verifiedAt).tickets(new ArrayList<>()).bonusCard(null).bookings(new ArrayList<>()).build();

		userMapper.updateUserFromDto(null, existingUser);

		assertEquals("test@example.com", existingUser.getEmail());
		assertEquals("Test", existingUser.getFirstName());
		assertEquals("User", existingUser.getLastName());
		assertEquals("City", existingUser.getCity());
		assertEquals("+380000000000", existingUser.getPhoneNumber());
		assertEquals(VerificationStatus.VERIFIED, existingUser.getVerificationStatus());
		assertEquals(verifiedAt, existingUser.getVerifiedAt());
		assertNotNull(existingUser.getTickets());
		assertNull(existingUser.getBonusCard());
		assertNotNull(existingUser.getBookings());
	}

	@Test
	void updateUserFromDto_ShouldUpdateOnlySpecifiedFields() {
		User existingUser = User.builder().id(1L).email("test@example.com").firstName("OldFirstName")
				.lastName("OldLastName").city("OldCity").phoneNumber("+380000000000")
				.dateOfBirth(LocalDate.of(1990, 1, 1)).verificationStatus(VerificationStatus.NOT_VERIFIED)
				.verifiedAt(null).tickets(new ArrayList<>()).bonusCard(null).bookings(new ArrayList<>()).build();

		UserUpdateRequest updateDto = UserUpdateRequest.builder().firstName("NewFirstName").lastName("OldLastName")
				.city("NewCity").phoneNumber("+380000000000").dateOfBirth(LocalDate.of(1990, 1, 1)).build();

		userMapper.updateUserFromDto(updateDto, existingUser);

		assertEquals("NewFirstName", existingUser.getFirstName());
		assertEquals("OldLastName", existingUser.getLastName());
		assertEquals("NewCity", existingUser.getCity());
		assertEquals("+380000000000", existingUser.getPhoneNumber());
		assertEquals(LocalDate.of(1990, 1, 1), existingUser.getDateOfBirth());
		assertEquals("test@example.com", existingUser.getEmail());
		assertEquals(VerificationStatus.NOT_VERIFIED, existingUser.getVerificationStatus());
		assertNull(existingUser.getVerifiedAt());
		assertNotNull(existingUser.getTickets());
		assertNull(existingUser.getBonusCard());
		assertNotNull(existingUser.getBookings());
	}

	@Test
	void updateUserFromDto_ShouldNotChangeUnspecifiedFieldsWhenDtoIsPartial() {
		LocalDateTime originalVerifiedAt = LocalDateTime.of(2024, 1, 10, 12, 0);

		User existingUser = User.builder().id(1L).email("test@example.com").firstName("OldFirstName")
				.lastName("OldLastName").city("OldCity").phoneNumber("+380000000000")
				.dateOfBirth(LocalDate.of(1990, 1, 1)).verificationStatus(VerificationStatus.VERIFIED)
				.verifiedAt(originalVerifiedAt).tickets(new ArrayList<>()).bonusCard(null).bookings(new ArrayList<>())
				.build();

		UserUpdateRequest updateDto = UserUpdateRequest.builder().firstName("NewFirstName").lastName("OldLastName")
				.city("OldCity").phoneNumber("+380000000000").dateOfBirth(LocalDate.of(1990, 1, 1)).build();

		userMapper.updateUserFromDto(updateDto, existingUser);

		assertEquals("NewFirstName", existingUser.getFirstName());
		assertEquals("OldLastName", existingUser.getLastName());
		assertEquals("OldCity", existingUser.getCity());
		assertEquals("+380000000000", existingUser.getPhoneNumber());
		assertEquals(LocalDate.of(1990, 1, 1), existingUser.getDateOfBirth());
		assertEquals("test@example.com", existingUser.getEmail());
		assertEquals(VerificationStatus.VERIFIED, existingUser.getVerificationStatus());
		assertEquals(originalVerifiedAt, existingUser.getVerifiedAt());
		assertNotNull(existingUser.getTickets());
		assertNull(existingUser.getBonusCard());
		assertNotNull(existingUser.getBookings());
	}
}
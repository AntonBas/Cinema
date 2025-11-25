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
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;

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
		assertFalse(user.isEnabled());
		assertNull(user.getPassword());
		assertNull(user.getId());
		assertNotNull(user.getTickets());
		assertTrue(user.getTickets().isEmpty());
	}

	@Test
	void toEntity_ShouldReturnNull_WhenDtoIsNull() {
		User user = userMapper.toEntity(null);
		assertNull(user);
	}

	@Test
	void toDto_ShouldMapAllFields() {
		User user = User.builder().id(1L).email("anton@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.userRole(UserRole.ROLE_USER).enabled(true).build();

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
		assertTrue(dto.isEnabled());
	}

	@Test
	void toDto_ShouldReturnNull_WhenUserIsNull() {
		UserResponse dto = userMapper.toDto(null);
		assertNull(dto);
	}

	@Test
	void toEntityWithPassword_ShouldSetEncodedPassword() {
		UserRegistrationRequest dto = UserRegistrationRequest.builder().email("anton@example.com").firstName("Anton")
				.build();

		User user = userMapper.toEntityWithPassword(dto, "encodedPassword123");

		assertNotNull(user);
		assertEquals("anton@example.com", user.getEmail());
		assertEquals("Anton", user.getFirstName());
		assertEquals("encodedPassword123", user.getPassword());
		assertEquals(UserRole.ROLE_USER, user.getUserRole());
		assertFalse(user.isEnabled());
		assertNotNull(user.getTickets());
		assertTrue(user.getTickets().isEmpty());
	}

	@Test
	void toProfileResponse_ShouldMapProfileFields() {
		User user = User.builder().id(1L).email("anton@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789").build();

		UserProfileResponse dto = userMapper.toProfileResponse(user);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("anton@example.com", dto.getEmail());
		assertEquals("Anton", dto.getFirstName());
		assertEquals("Bas", dto.getLastName());
		assertEquals(LocalDate.of(2001, 8, 21), dto.getDateOfBirth());
		assertEquals("Lviv", dto.getCity());
		assertEquals("+380123456789", dto.getPhoneNumber());
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

		User user = User.builder().id(1L).email("admin@example.com").firstName("Admin").lastName("User")
				.userRole(UserRole.ROLE_ADMIN).enabled(true).createdAt(createdAt).updatedAt(updatedAt)
				.tickets(new ArrayList<>()).build();

		AdminUserListResponse dto = userMapper.toAdminListDto(user);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("admin@example.com", dto.getEmail());
		assertEquals("Admin", dto.getFirstName());
		assertEquals("User", dto.getLastName());
		assertEquals(UserRole.ROLE_ADMIN, dto.getUserRole());
		assertTrue(dto.isEnabled());

		assertEquals(createdAt.toLocalDate(), dto.getCreatedAt());
		assertEquals(updatedAt.toLocalDate(), dto.getUpdatedAt());
		assertEquals(0, dto.getTicketsCount());
		assertEquals(updatedAt.toLocalDate(), dto.getLastActivity());
	}

	@Test
	void toAdminListDto_ShouldCalculateTicketsCount() {
		User user = User.builder().id(1L).email("user@example.com").firstName("Test").lastName("User")
				.userRole(UserRole.ROLE_USER).enabled(true).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).tickets(new ArrayList<>()).build();

		user.getTickets().add(null);
		user.getTickets().add(null);

		AdminUserListResponse dto = userMapper.toAdminListDto(user);

		assertNotNull(dto);
		assertEquals(2, dto.getTicketsCount());
	}

	@Test
	void toAdminListDto_ShouldReturnNull_WhenUserIsNull() {
		AdminUserListResponse dto = userMapper.toAdminListDto(null);
		assertNull(dto);
	}

	@Test
	void toAdminListDto_ShouldHandleDisabledUser() {
		User user = User.builder().id(1L).email("disabled@example.com").firstName("Disabled").lastName("User")
				.userRole(UserRole.ROLE_USER).enabled(false).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).tickets(new ArrayList<>()).build();

		AdminUserListResponse dto = userMapper.toAdminListDto(user);

		assertNotNull(dto);
		assertFalse(dto.isEnabled());
		assertEquals("Disabled", dto.getFirstName());
		assertEquals("User", dto.getLastName());
	}

	@Test
	void toAdminListDto_ShouldMapCashierRole() {
		User user = User.builder().id(1L).email("cashier@example.com").firstName("Cashier").lastName("User")
				.userRole(UserRole.ROLE_CASHIER).enabled(true).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).tickets(new ArrayList<>()).build();

		AdminUserListResponse dto = userMapper.toAdminListDto(user);

		assertNotNull(dto);
		assertEquals(UserRole.ROLE_CASHIER, dto.getUserRole());
		assertEquals("Cashier", dto.getFirstName());
	}
}
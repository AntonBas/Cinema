package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.UserDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

	private UserMapper userMapper = new UserMapperImpl();

	@Test
	void toEntity_ShouldMapAllFieldsCorrectly() {
		UserRegistrationDto dto = UserRegistrationDto.builder().email("anton@example.com").firstName("Anton")
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

		UserDto dto = userMapper.toDto(user);

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
		UserDto dto = userMapper.toDto(null);
		assertNull(dto);
	}

	@Test
	void toEntityWithPassword_ShouldSetEncodedPassword() {
		UserRegistrationDto dto = UserRegistrationDto.builder().email("anton@example.com").firstName("Anton").build();

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
}
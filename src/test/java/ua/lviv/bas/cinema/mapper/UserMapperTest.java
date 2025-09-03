package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.UserLoginDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserMapperImpl userMapper;

	@Test
	void toEntity_ShouldMapAllFieldsCorrectlyAndEncodePassword() {
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");

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
		assertEquals("encodedPassword123", user.getPassword());
		assertEquals(UserRole.ROLE_USER, user.getUserRole());
		assertFalse(user.isEnabled());
		assertNotNull(user.getTickets());
		assertTrue(user.getTickets().isEmpty());
		assertNull(user.getId());

		verify(passwordEncoder).encode("rawPassword");
	}

	@Test
	void toEntity_ShouldHandleNullPassword() {
		UserRegistrationDto dto = UserRegistrationDto.builder().email("anton@example.com").password(null).build();

		User user = userMapper.toEntity(dto);

		assertNotNull(user);
		assertNull(user.getPassword());
	}

	@Test
	void toEntity_ShouldHandleEmptyPassword() {
		when(passwordEncoder.encode(anyString())).thenReturn("encodedEmptyPassword");

		UserRegistrationDto dto = UserRegistrationDto.builder().email("anton@example.com").password("").build();

		User user = userMapper.toEntity(dto);

		assertNotNull(user);
		assertEquals("encodedEmptyPassword", user.getPassword());
		verify(passwordEncoder).encode("");
	}

	@Test
	void toEntity_ShouldReturnNull_WhenDtoIsNull() {
		User user = userMapper.toEntity(null);

		assertNull(user);
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	void toLoginDto_ShouldMapEmailAndNullPassword() {
		User user = User.builder().email("anton@example.com").password("encodedPassword").build();

		UserLoginDto dto = userMapper.toLoginDto(user);

		assertNotNull(dto);
		assertEquals("anton@example.com", dto.getEmail());
		assertNull(dto.getPassword());
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	void toLoginDto_ShouldReturnNull_WhenUserIsNull() {
		UserLoginDto dto = userMapper.toLoginDto(null);

		assertNull(dto);
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	void updateEntity_ShouldUpdateOnlyAllowedFields() {
		User existingUser = User.builder().id(1L).email("old@example.com").password("oldPassword").enabled(true)
				.userRole(UserRole.ROLE_ADMIN).build();

		UserRegistrationDto dto = UserRegistrationDto.builder().email("anton@example.com").firstName("Anton")
				.lastName("Bas").build();

		User updatedUser = userMapper.updateEntity(dto, existingUser);

		assertNotNull(updatedUser);
		assertEquals(1L, updatedUser.getId());
		assertEquals("oldPassword", updatedUser.getPassword());
		assertEquals("anton@example.com", updatedUser.getEmail());
		assertEquals("Anton", updatedUser.getFirstName());
		assertEquals("Bas", updatedUser.getLastName());
		assertTrue(updatedUser.isEnabled());
		assertEquals(UserRole.ROLE_ADMIN, updatedUser.getUserRole());

		verifyNoInteractions(passwordEncoder);
	}
}
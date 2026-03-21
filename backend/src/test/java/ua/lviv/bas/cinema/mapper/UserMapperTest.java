package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;

public class UserMapperTest {

	private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

	@Test
	void toUserShouldMapRegistrationRequest() {
		UserRegistrationRequest request = new UserRegistrationRequest("test@example.com", "John", "Doe",
				LocalDate.of(1990, 1, 1), "Kyiv", "+380501234567", "password", "password");

		User user = userMapper.toUser(request);

		assertThat(user.getEmail()).isEqualTo("test@example.com");
		assertThat(user.getFirstName()).isEqualTo("John");
		assertThat(user.getLastName()).isEqualTo("Doe");
		assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_USER);
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(user.isEnabled()).isFalse();
	}

	@Test
	void toUserResponseShouldMapUser() {
		User user = User.builder().id(1L).email("test@example.com").firstName("John").lastName("Doe")
				.userRole(UserRole.ROLE_USER).build();

		UserResponse response = userMapper.toUserResponse(user);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.email()).isEqualTo("test@example.com");
		assertThat(response.firstName()).isEqualTo("John");
		assertThat(response.lastName()).isEqualTo("Doe");
	}

	@Test
	void updateUserFromRequestShouldUpdateFields() {
		User user = User.builder().firstName("Old").lastName("User").build();

		UserUpdateRequest request = new UserUpdateRequest("New", "Name", LocalDate.of(1990, 1, 1), "Lviv",
				"+380502345678");

		userMapper.updateUserFromRequest(request, user);

		assertThat(user.getFirstName()).isEqualTo("New");
		assertThat(user.getLastName()).isEqualTo("Name");
	}

	@Test
	void nullHandling() {
		assertThat(userMapper.toUser(null)).isNull();
		assertThat(userMapper.toUserResponse(null)).isNull();
		assertThat(userMapper.toUserProfileResponse(null)).isNull();
	}
}
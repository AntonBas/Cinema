package ua.lviv.bas.cinema.service.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private OAuth2UserRequest userRequest;

	@Mock
	private OAuth2User oAuth2User;

	@InjectMocks
	private CustomOAuth2UserService customOAuth2UserService;

	@Captor
	private ArgumentCaptor<User> userCaptor;

	private final String EMAIL = "test@gmail.com";
	private final String FULL_NAME = "John Doe";
	private final String FIRST_NAME = "John";
	private final String LAST_NAME = "Doe";

	@BeforeEach
	void setUp() {
		Map<String, Object> attributes = Map.of("email", EMAIL, "name", FULL_NAME);
		when(oAuth2User.getAttributes()).thenReturn(attributes);
	}

	@Test
	void loadUser_CreatesNewUser_WhenUserDoesNotExist() throws Exception {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Method processMethod = CustomOAuth2UserService.class.getDeclaredMethod("processOAuth2User", OAuth2User.class);
		processMethod.setAccessible(true);
		OAuth2User result = (OAuth2User) processMethod.invoke(customOAuth2UserService, oAuth2User);

		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
		assertThat(savedUser.getFirstName()).isEqualTo(FIRST_NAME);
		assertThat(savedUser.getLastName()).isEqualTo(LAST_NAME);
		assertThat(savedUser.getUserRole()).isEqualTo(UserRole.ROLE_USER);
		assertThat(savedUser.isEnabled()).isTrue();
		assertThat(savedUser.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(savedUser.getVerifiedAt()).isNull();
		assertThat(savedUser.getCity()).isEmpty();
		assertThat(savedUser.getPhoneNumber()).isEmpty();
		assertThat(savedUser.getDateOfBirth()).isNotNull();
		assertThat(savedUser.getPassword()).isNotNull();
		assertThat(savedUser.getPassword()).isNotBlank();
		assertThat(result).isEqualTo(oAuth2User);
	}

	@Test
	void loadUser_EnablesExistingUser_WhenUserIsDisabled() throws Exception {
		User existingUser = User.builder().email(EMAIL).firstName(FIRST_NAME).lastName(LAST_NAME).enabled(false)
				.build();

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

		Method processMethod = CustomOAuth2UserService.class.getDeclaredMethod("processOAuth2User", OAuth2User.class);
		processMethod.setAccessible(true);
		OAuth2User result = (OAuth2User) processMethod.invoke(customOAuth2UserService, oAuth2User);

		assertThat(existingUser.isEnabled()).isTrue();
		verify(userRepository).save(existingUser);
		assertThat(result).isEqualTo(oAuth2User);
	}

	@Test
	void loadUser_DoesNotModifyExistingUser_WhenUserIsEnabled() throws Exception {
		User existingUser = User.builder().email(EMAIL).firstName(FIRST_NAME).lastName(LAST_NAME).enabled(true).build();

		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

		Method processMethod = CustomOAuth2UserService.class.getDeclaredMethod("processOAuth2User", OAuth2User.class);
		processMethod.setAccessible(true);
		OAuth2User result = (OAuth2User) processMethod.invoke(customOAuth2UserService, oAuth2User);

		verify(userRepository, never()).save(any());
		assertThat(result).isEqualTo(oAuth2User);
	}

	@Test
	void loadUser_HandlesNameWithMultipleParts() throws Exception {
		Map<String, Object> attributes = Map.of("email", EMAIL, "name", "John Michael Doe");
		when(oAuth2User.getAttributes()).thenReturn(attributes);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Method processMethod = CustomOAuth2UserService.class.getDeclaredMethod("processOAuth2User", OAuth2User.class);
		processMethod.setAccessible(true);
		processMethod.invoke(customOAuth2UserService, oAuth2User);

		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.getFirstName()).isEqualTo("John");
		assertThat(savedUser.getLastName()).isEqualTo("Michael Doe");
	}

	@Test
	void loadUser_HandlesNameWithSinglePart() throws Exception {
		Map<String, Object> attributes = Map.of("email", EMAIL, "name", "John");
		when(oAuth2User.getAttributes()).thenReturn(attributes);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Method processMethod = CustomOAuth2UserService.class.getDeclaredMethod("processOAuth2User", OAuth2User.class);
		processMethod.setAccessible(true);
		processMethod.invoke(customOAuth2UserService, oAuth2User);

		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.getFirstName()).isEqualTo("John");
		assertThat(savedUser.getLastName()).isEmpty();
	}

	@Test
	void loadUser_GeneratesValidUUIDForPassword() throws Exception {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Method processMethod = CustomOAuth2UserService.class.getDeclaredMethod("processOAuth2User", OAuth2User.class);
		processMethod.setAccessible(true);
		processMethod.invoke(customOAuth2UserService, oAuth2User);

		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.getPassword()).isNotNull();
		assertThat(savedUser.getPassword()).isNotBlank();
		assertThat(UUID.fromString(savedUser.getPassword())).isNotNull();
	}
}
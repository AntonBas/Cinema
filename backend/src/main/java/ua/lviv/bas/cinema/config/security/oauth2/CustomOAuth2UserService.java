package ua.lviv.bas.cinema.config.security.oauth2;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final BonusService bonusService;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		return processOAuth2User(oAuth2User);
	}

	private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
		Map<String, Object> attributes = oAuth2User.getAttributes();

		String email = (String) attributes.get("email");
		String name = (String) attributes.get("name");

		String[] nameParts = name.split(" ", 2);
		String firstName = nameParts[0];
		String lastName = nameParts.length > 1 ? nameParts[1] : "";

		Optional<User> userOptional = userRepository.findByEmail(email);
		User user;

		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!user.isEnabled()) {
				user.setEnabled(true);
				userRepository.save(user);
				log.info("Enabled existing OAuth2 user {}", email);
			}
		} else {
			user = User.builder().email(email).firstName(firstName).lastName(lastName)
					.password(UUID.randomUUID().toString()).userRole(UserRole.ROLE_USER).enabled(true)
					.verificationStatus(VerificationStatus.NOT_VERIFIED).verifiedAt(null).city("").phoneNumber("")
					.dateOfBirth(LocalDate.now().minusYears(18)).build();

			user = userRepository.save(user);
			log.info("Created new OAuth2 user: {}", email);

			bonusService.getOrCreateCard(user);
			bonusService.awardWelcomeBonus(user);
			log.info("Created bonus card and awarded welcome bonus for OAuth2 user: {}", email);
		}

		return oAuth2User;
	}
}
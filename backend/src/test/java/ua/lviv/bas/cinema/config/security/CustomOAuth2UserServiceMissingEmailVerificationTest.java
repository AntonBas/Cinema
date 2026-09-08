package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.config.TestcontainersConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CustomOAuth2UserServiceMissingEmailVerificationTest {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void processOAuth2UserShouldThrowOAuth2AuthenticationExceptionWhenProviderOmitsEmail() throws Exception {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "No Email User");
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        Method processMethod = CustomOAuth2UserService.class.getDeclaredMethod("processOAuth2User", OAuth2User.class);
        processMethod.setAccessible(true);

        assertThatThrownBy(() -> {
            try {
                processMethod.invoke(customOAuth2UserService, oAuth2User);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }).isInstanceOf(OAuth2AuthenticationException.class);
    }
}

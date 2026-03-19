package ua.lviv.bas.cinema.config.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.security.CustomUserDetailsService;
import ua.lviv.bas.cinema.service.oauth2.CustomOAuth2UserService;
import ua.lviv.bas.cinema.service.oauth2.OAuth2AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService userDetailsService;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration
				.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Cache-Control"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health", "/actuator/info").permitAll()
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**",
								"/swagger-resources/**", "/swagger-resources", "/configuration/ui",
								"/configuration/security", "/webjars/**", "/error")
						.permitAll()
						.requestMatchers("/api/auth/**", "/api/verify/**", "/api/registration/**", "/api/tokens/**")
						.permitAll().requestMatchers("/api/movies/**").permitAll()
						.requestMatchers("/api/cinema-halls/**").permitAll().requestMatchers("/api/genres/**")
						.permitAll().requestMatchers("/api/sessions/**").permitAll()
						.requestMatchers("/api/ticket-types/dropdown").permitAll()
						.requestMatchers("/api/promotions/available").permitAll().requestMatchers("/api/seats/**")
						.permitAll().requestMatchers("/api/liqpay/callback").permitAll().requestMatchers("/oauth2/**")
						.permitAll().requestMatchers("/api/bonus/**").authenticated()
						.requestMatchers("/api/bookings/**").authenticated().requestMatchers("/api/payments/**")
						.authenticated().requestMatchers("/api/refunds/**").authenticated()
						.requestMatchers("/api/tickets/**").authenticated().requestMatchers("/api/users/**")
						.authenticated().requestMatchers("/api/promotions/**").authenticated()
						.requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
				.oauth2Login(oauth2 -> oauth2.authorizationEndpoint(endpoint -> endpoint.baseUri("/oauth2/authorize"))
						.redirectionEndpoint(endpoint -> endpoint.baseUri("/oauth2/callback/*"))
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
						.successHandler(oAuth2AuthenticationSuccessHandler))
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				.formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
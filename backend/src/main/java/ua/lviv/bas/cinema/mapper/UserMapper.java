package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.UserLoginDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "enabled", constant = "false")
	@Mapping(target = "userRole", expression = "java(ua.lviv.bas.cinema.domain.enums.UserRole.ROLE_USER)")
	@Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
	public abstract User toEntity(UserRegistrationDto dto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "enabled", ignore = true)
	@Mapping(target = "userRole", ignore = true)
	public abstract User updateEntity(UserRegistrationDto dto, @MappingTarget User user);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "enabled", constant = "false")
	@Mapping(target = "userRole", expression = "java(ua.lviv.bas.cinema.domain.enums.UserRole.ROLE_USER)")
	@Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
	public abstract User toEntityWithPassword(UserRegistrationDto dto, String password);

	@Named("encodePassword")
	protected String encodePassword(String password) {
		return passwordEncoder.encode(password);
	}

	public UserLoginDto toLoginDto(User user) {
		if (user == null) {
			return null;
		}
		return UserLoginDto.builder().email(user.getEmail()).password(null).build();
	}
}
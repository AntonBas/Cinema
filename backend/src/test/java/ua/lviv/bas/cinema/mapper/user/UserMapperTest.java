package ua.lviv.bas.cinema.mapper.user;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.repository.user.projection.AdminUserProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toUser_ShouldMapRegistrationRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest("test@example.com", "John", "Doe",
                LocalDate.of(1990, 1, 1), "Kyiv", "+380501234567", "password", "password");

        User user = userMapper.toUser(request);

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(user.getCity()).isEqualTo("Kyiv");
        assertThat(user.getPhoneNumber()).isEqualTo("+380501234567");
        assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getId()).isNull();
        assertThat(user.getTickets()).isEmpty();
        assertThat(user.getBonusCard()).isNull();
    }

    @Test
    void toUserResponse_ShouldMapUser() {
        User user = User.builder().id(1L).email("test@example.com").firstName("John").lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1)).city("Kyiv").phoneNumber("+380501234567")
                .userRole(UserRole.ROLE_USER).enabled(true).verificationStatus(VerificationStatus.VERIFIED).build();

        UserResponse response = userMapper.toUserResponse(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(response.city()).isEqualTo("Kyiv");
        assertThat(response.phoneNumber()).isEqualTo("+380501234567");
        assertThat(response.userRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(response.enabled()).isTrue();
        assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    void toUserProfileResponse_ShouldMapUser() {
        User user = User.builder().id(1L).email("test@example.com").firstName("John").lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1)).city("Kyiv").phoneNumber("+380501234567")
                .verificationStatus(VerificationStatus.VERIFIED).build();

        UserProfileResponse response = userMapper.toUserProfileResponse(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(response.city()).isEqualTo("Kyiv");
        assertThat(response.phoneNumber()).isEqualTo("+380501234567");
        assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    void updateUserFromRequest_ShouldUpdateFields() {
        User user = User.builder().firstName("Old").lastName("User").dateOfBirth(LocalDate.of(1990, 1, 1))
                .city("Old City").phoneNumber("+380501234567").build();

        UserUpdateRequest request = new UserUpdateRequest("New", "Name", LocalDate.of(1995, 5, 5), "Lviv",
                "+380502345678");

        userMapper.updateUserFromRequest(request, user);

        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 5));
        assertThat(user.getCity()).isEqualTo("Lviv");
        assertThat(user.getPhoneNumber()).isEqualTo("+380502345678");
    }

    @Test
    void updateUserFromRequest_WithNullValues_ShouldIgnoreNull() {
        User user = User.builder().firstName("Old").lastName("User").dateOfBirth(LocalDate.of(1990, 1, 1))
                .city("Old City").phoneNumber("+380501234567").build();

        UserUpdateRequest request = new UserUpdateRequest(null, null, null, null, null);

        userMapper.updateUserFromRequest(request, user);

        assertThat(user.getFirstName()).isEqualTo("Old");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(user.getCity()).isEqualTo("Old City");
        assertThat(user.getPhoneNumber()).isEqualTo("+380501234567");
    }

    @Test
    void toAdminUserListResponse_FromProjection_ShouldMapAllFields() {
        AdminUserProjection projection = new AdminUserProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getEmail() {
                return "test@example.com";
            }

            @Override
            public String getFirstName() {
                return "John";
            }

            @Override
            public String getLastName() {
                return "Doe";
            }

            @Override
            public UserRole getUserRole() {
                return UserRole.ROLE_USER;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public VerificationStatus getVerificationStatus() {
                return VerificationStatus.VERIFIED;
            }

            @Override
            public LocalDateTime getVerifiedAt() {
                return LocalDateTime.of(2024, 1, 15, 10, 30);
            }

            @Override
            public Long getTicketsCount() {
                return 5L;
            }

            @Override
            public LocalDateTime getLastActivity() {
                return LocalDateTime.of(2024, 1, 20, 14, 0);
            }
        };

        AdminUserListResponse response = userMapper.toAdminUserListResponse(projection);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.userRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(response.enabled()).isTrue();
        assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(response.verifiedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
        assertThat(response.ticketsCount()).isEqualTo(5L);
        assertThat(response.lastActivity()).isEqualTo(LocalDateTime.of(2024, 1, 20, 14, 0));
    }

    @Test
    void toAdminUserListResponse_FromUser_ShouldMapAllFields() {
        User user = User.builder().id(1L).email("test@example.com").firstName("John").lastName("Doe")
                .userRole(UserRole.ROLE_USER).enabled(true).verificationStatus(VerificationStatus.VERIFIED)
                .verifiedAt(LocalDateTime.of(2024, 1, 15, 10, 30)).tickets(new ArrayList<>()).build();

        user.getTickets().add(null);
        user.getTickets().add(null);

        AdminUserListResponse response = userMapper.toAdminUserListResponse(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.userRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(response.enabled()).isTrue();
        assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(response.verifiedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
        assertThat(response.ticketsCount()).isEqualTo(2L);
        assertThat(response.lastActivity()).isNull();
    }

    @Test
    void toAdminUserListResponse_FromUser_WithNullTickets_ShouldReturnZeroCount() {
        User user = User.builder().id(1L).email("test@example.com").firstName("John").lastName("Doe")
                .userRole(UserRole.ROLE_USER).enabled(true).verificationStatus(VerificationStatus.VERIFIED)
                .tickets(null).build();

        AdminUserListResponse response = userMapper.toAdminUserListResponse(user);

        assertThat(response.ticketsCount()).isEqualTo(0L);
    }

    @Test
    void toUser_WithNullRequest_ShouldReturnNull() {
        User user = userMapper.toUser(null);
        assertThat(user).isNull();
    }

    @Test
    void toUserResponse_WithNullUser_ShouldReturnNull() {
        UserResponse response = userMapper.toUserResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toUserProfileResponse_WithNullUser_ShouldReturnNull() {
        UserProfileResponse response = userMapper.toUserProfileResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toAdminUserListResponse_WithNullProjection_ShouldReturnNull() {
        AdminUserListResponse response = userMapper.toAdminUserListResponse((AdminUserProjection) null);
        assertThat(response).isNull();
    }

    @Test
    void toAdminUserListResponse_WithNullUser_ShouldReturnNull() {
        AdminUserListResponse response = userMapper.toAdminUserListResponse((User) null);
        assertThat(response).isNull();
    }
}
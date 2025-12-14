package ua.lviv.bas.cinema.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "tickets", "password" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users", indexes = { @Index(name = "idx_user_email", columnList = "email"),
		@Index(name = "idx_user_name", columnList = "firstName,lastName") })
public class User {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Email
	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@NotBlank
	@Column(nullable = false, name = "first_name", length = 50)
	private String firstName;

	@NotBlank
	@Column(nullable = false, name = "last_name", length = 50)
	private String lastName;

	@Past
	@Column(nullable = false, name = "date_of_birth")
	private LocalDate dateOfBirth;

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status", nullable = false)
	@Builder.Default
	private VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@Column(nullable = false, length = 50)
	private String city;

	@Column(nullable = false, name = "phone_number", length = 20)
	private String phoneNumber;

	@Column(nullable = false, length = 60)
	private String password;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Ticket> tickets = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "user_role", length = 50)
	@Builder.Default
	private UserRole userRole = UserRole.ROLE_USER;

	@Column(nullable = false)
	@Builder.Default
	private boolean enabled = false;
}
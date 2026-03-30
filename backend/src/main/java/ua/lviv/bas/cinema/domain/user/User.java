package ua.lviv.bas.cinema.domain.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToOne;
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
import ua.lviv.bas.cinema.domain.audit.AuditableEntity;
import ua.lviv.bas.cinema.domain.bonus.BonusCard;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.promotion.UserPromotion;
import ua.lviv.bas.cinema.domain.ticket.Ticket;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "tickets", "password", "bonusCard", "bookings" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "users", indexes = { @Index(name = "idx_user_email", columnList = "email"),
		@Index(name = "idx_user_name", columnList = "first_name,last_name"),
		@Index(name = "idx_user_status", columnList = "verification_status"),
		@Index(name = "idx_user_role_status", columnList = "user_role, verification_status, enabled"),
		@Index(name = "idx_user_date_of_birth", columnList = "date_of_birth") })
public class User extends AuditableEntity {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
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

	@NotBlank
	@Column(nullable = false, length = 72)
	private String password;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	@Builder.Default
	private List<Ticket> tickets = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "user_role", length = 50)
	@Builder.Default
	private UserRole userRole = UserRole.ROLE_USER;

	@Column(nullable = false)
	@Builder.Default
	private boolean enabled = false;

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private BonusCard bonusCard;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Builder.Default
	private List<UserPromotion> redeemedPromotions = new ArrayList<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	@Builder.Default
	private List<Booking> bookings = new ArrayList<>();
}
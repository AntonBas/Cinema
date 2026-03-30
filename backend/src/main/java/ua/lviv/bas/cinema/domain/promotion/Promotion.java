package ua.lviv.bas.cinema.domain.promotion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.audit.AuditableEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "promotions", indexes = { @Index(name = "idx_promotion_active", columnList = "active"),
		@Index(name = "idx_promotion_dates", columnList = "start_date, end_date") })
public class Promotion extends AuditableEntity {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 60)
	@Column(nullable = false, length = 100)
	private String title;

	@Size(max = 500)
	@Column(length = 500)
	private String description;

	@NotNull
	@Positive
	@Column(name = "bonus_points", nullable = false)
	private Integer bonusPoints;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
	@Builder.Default
	private List<UserPromotion> userRedemptions = new ArrayList<>();
}
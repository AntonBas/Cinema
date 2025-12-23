package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "ticket_types", indexes = { @Index(name = "idx_ticket_type_active", columnList = "active"),
		@Index(name = "idx_ticket_type_code", columnList = "code") })
public class TicketType {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 20)
	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@NotBlank
	@Size(max = 50)
	@Column(name = "display_name", nullable = false, length = 50)
	private String displayName;

	@NotNull
	@jakarta.validation.constraints.DecimalMin(value = "0.01", inclusive = true)
	@jakarta.validation.constraints.DecimalMax(value = "9.99", inclusive = true)
	@Column(name = "price_multiplier", nullable = false, precision = 3, scale = 2)
	@Builder.Default
	private BigDecimal priceMultiplier = BigDecimal.ONE;

	@Column(name = "min_age")
	@Min(0)
	@Max(150)
	private Integer minAge;

	@Column(name = "max_age")
	@Min(0)
	@Max(150)
	private Integer maxAge;

	@Column(name = "requires_document", nullable = false)
	@Builder.Default
	private boolean requiresDocument = false;

	@Size(max = 500)
	@Column(name = "description")
	private String description;

	@Column(name = "active", nullable = false)
	@Builder.Default
	private boolean active = true;
}
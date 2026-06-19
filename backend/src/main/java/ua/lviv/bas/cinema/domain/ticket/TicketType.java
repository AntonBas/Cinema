package ua.lviv.bas.cinema.domain.ticket;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
import ua.lviv.bas.cinema.domain.audit.AuditableEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "ticket_types", indexes = { @Index(name = "idx_ticket_type_active", columnList = "active"),
		@Index(name = "idx_ticket_type_category", columnList = "category") })
public class TicketType extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	@ToString.Include
	private Long id;

	@NotBlank
	@Size(max = 50)
	@Column(name = "display_name", nullable = false, length = 50)
	private String displayName;

	@NotNull
	@DecimalMin("0.01")
	@DecimalMax("9.99")
	@Column(name = "price_multiplier", nullable = false, precision = 3, scale = 2)
	@Builder.Default
	private BigDecimal priceMultiplier = BigDecimal.ONE;

	@Min(0)
	@Max(100)
	@Column(name = "min_age")
	private Integer minAge;

	@Min(0)
	@Max(100)
	@Column(name = "max_age")
	private Integer maxAge;

	@Column(name = "requires_document", nullable = false)
	@Builder.Default
	private boolean requiresDocument = false;

	@Size(max = 100)
	@Column(name = "document_type", length = 100)
	private String documentType;

	@Column(name = "active", nullable = false)
	@Builder.Default
	private boolean active = true;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", length = 20)
	private TicketTypeCategory category;
}
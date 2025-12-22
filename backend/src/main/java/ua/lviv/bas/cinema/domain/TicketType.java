package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "ticket_types")
public class TicketType {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(name = "display_name", nullable = false, length = 50)
	private String displayName;

	@Column(name = "price_multiplier", nullable = false, precision = 3, scale = 2)
	@Builder.Default
	private BigDecimal priceMultiplier = BigDecimal.ONE;

	@Column(name = "min_age")
	private Integer minAge;

	@Column(name = "max_age")
	private Integer maxAge;

	@Column(name = "requires_document")
	@Builder.Default
	private Boolean requiresDocument = false;

	@Column(name = "description")
	private String description;

	@Column(name = "active")
	@Builder.Default
	private Boolean active = true;
}
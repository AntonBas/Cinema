package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.DiscountType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "user" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "discounts")
public class Discount {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 30)
	private DiscountType type;

	@Column(name = "percent", precision = 5, scale = 2, nullable = false)
	private BigDecimal percent;

	@Column(name = "document_number", length = 50)
	private String documentNumber;

	@Column(name = "expiry_date")
	private LocalDateTime expiryDate;

	@Column(name = "active", nullable = false)
	@Builder.Default
	private Boolean active = true;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@Column(name = "verified_by", length = 100)
	private String verifiedBy;
}
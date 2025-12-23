package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.PaymentMethod;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "booking", "bonusTransactions", "refunds" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "payments", indexes = { @Index(name = "idx_payment_booking", columnList = "booking_id"),
		@Index(name = "idx_payment_status", columnList = "status"),
		@Index(name = "idx_payment_created", columnList = "created_at"),
		@Index(name = "idx_payment_transaction", columnList = "transaction_id") })
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Positive
	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "bonus_points_used")
	@Min(0)
	private Integer bonusPointsUsed;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, length = 20)
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(name = "transaction_id", unique = true, length = 100)
	private String transactionId;

	@Column(name = "payment_time")
	private LocalDateTime paymentTime;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking;

	@OneToMany(mappedBy = "payment", fetch = FetchType.LAZY)
	@BatchSize(size = 20)
	@Builder.Default
	private List<BonusTransaction> bonusTransactions = new ArrayList<>();

	@OneToMany(mappedBy = "payment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchSize(size = 20)
	@Builder.Default
	private List<Refund> refunds = new ArrayList<>();
}
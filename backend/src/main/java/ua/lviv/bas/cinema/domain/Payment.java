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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "booking", "refunds" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "payments", indexes = { @Index(name = "idx_payment_booking", columnList = "booking_id"),
		@Index(name = "idx_payment_status", columnList = "status"),
		@Index(name = "idx_payment_created", columnList = "created_at"),
		@Index(name = "idx_payment_transaction", columnList = "transaction_id"),
		@Index(name = "idx_payment_liqpay_order", columnList = "liqpay_order_id") })
public class Payment {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Positive
	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(name = "liqpay_order_id", unique = true, length = 50)
	private String liqpayOrderId;

	@Column(name = "liqpay_payment_id", length = 50)
	private String liqpayPaymentId;

	@Column(name = "liqpay_transaction_id", length = 100)
	private String liqpayTransactionId;

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

	@OneToMany(mappedBy = "payment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchSize(size = 20)
	@Builder.Default
	private List<Refund> refunds = new ArrayList<>();

	@Column(name = "liqpay_error_code", length = 50)
	private String liqpayErrorCode;

	@Column(name = "liqpay_error_description", length = 500)
	private String liqpayErrorDescription;

	@Column(name = "liqpay_sender_card_mask", length = 20)
	private String liqpaySenderCardMask;
}
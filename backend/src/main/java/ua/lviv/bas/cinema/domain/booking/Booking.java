package ua.lviv.bas.cinema.domain.booking;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ua.lviv.bas.cinema.domain.audit.AuditableEntity;
import ua.lviv.bas.cinema.domain.bonus.BonusTransaction;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "session", "seatReservations", "tickets", "bonusTransactions"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "bookings", indexes = {@Index(name = "idx_booking_user", columnList = "user_id"),
        @Index(name = "idx_booking_session", columnList = "session_id"),
        @Index(name = "idx_booking_status", columnList = "status"),
        @Index(name = "idx_booking_expires", columnList = "expires_at"),
        @Index(name = "idx_booking_final_price", columnList = "final_price")})
public class Booking extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<SeatReservation> seatReservations = new ArrayList<>();

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<BonusTransaction> bonusTransactions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "bonus_points_used")
    @Builder.Default
    private Integer bonusPointsUsed = 0;

    @Column(name = "bonus_discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal bonusDiscountAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @OneToOne(mappedBy = "booking")
    private Payment payment;
}
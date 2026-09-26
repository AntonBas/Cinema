package ua.lviv.bas.cinema.cinema.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

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
import jakarta.persistence.OneToMany;
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
import ua.lviv.bas.cinema.audit.domain.AuditableEntity;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "hall", "seatReservations" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "seats")
public class Seat extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    @NotNull
    @Column(name = "seat_row", nullable = false)
    private Integer row;

    @Positive
    @NotNull
    @Column(nullable = false)
    private Integer number;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatType seatType = SeatType.STANDARD;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private CinemaHall hall;

    @NotNull
    @Column(nullable = false)
    private Integer x;

    @NotNull
    @Column(nullable = false)
    private Integer y;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "seat", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    @Builder.Default
    private List<SeatReservation> seatReservations = new ArrayList<>();
}
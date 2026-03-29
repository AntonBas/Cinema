package ua.lviv.bas.cinema.domain.cinema;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
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
@ToString(exclude = { "sessions", "seats" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "cinema_halls", indexes = @Index(name = "idx_hall_name", columnList = "name"), uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class CinemaHall {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(min = 2, max = 25)
	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "hall", fetch = FetchType.LAZY)
	@BatchSize(size = 10)
	@Builder.Default
	private List<Session> sessions = new ArrayList<>();

	@OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@Builder.Default
	private List<Seat> seats = new ArrayList<>();
}
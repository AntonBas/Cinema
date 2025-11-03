package ua.lviv.bas.cinema.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cinema_halls")
public class CinemaHall {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(min = 2, max = 25)
	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "hall", fetch = FetchType.LAZY)
	@Builder.Default
	private List<Session> sessions = new ArrayList<Session>();

	@OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Seat> seats;

	public int getCapacity() {
		return (seats != null) ? seats.size() : 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CinemaHall)) {
			return false;
		}
		CinemaHall other = (CinemaHall) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "CinemaHall [id=" + id + ", name=" + name + "]";
	}

}

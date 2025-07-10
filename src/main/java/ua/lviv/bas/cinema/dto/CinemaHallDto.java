package ua.lviv.bas.cinema.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CinemaHallDto {

	private Long id;

	@NotBlank(message = "Hall name is required")
	@Size(max = 30, message = "Maximum 30 characters")
	private String name;

	@Min(value = 1, message = "Minimum 1 row")
	@Max(value = 20, message = "Maximum 20 rows")
	private int rows;

	@Min(value = 1, message = "Minimum 1 seat in a row")
	@Max(value = 30, message = "Maximum 30 seat in a row")
	private int seatsPerRow;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getSeatsPerRow() {
		return seatsPerRow;
	}

	public void setSeatsPerRow(int seatsPerRow) {
		this.seatsPerRow = seatsPerRow;
	}
}

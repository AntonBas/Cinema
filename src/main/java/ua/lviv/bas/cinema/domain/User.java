package ua.lviv.bas.cinema.domain;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public record User(
		@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Integer id,
		@Column(nullable = false, unique = true) String email,
		@Column(nullable = false, name = "first_name") String firstName,
		@Column(nullable = false, name = "last_name") String lastName,
		@Column(nullable = false, name = "date_of_birth") Date dateOfBirth,
		@Column(name = "phone_number") String phoneNumber, 
		@Column(nullable = false) String password,
		@Enumerated(EnumType.STRING) @Column(nullable = false) UserRole userRole) {

}
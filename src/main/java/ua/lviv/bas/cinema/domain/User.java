package ua.lviv.bas.cinema.domain;

import java.time.LocalDate;
import java.util.Objects;

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

public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, name = "first_name")
	private String firstName;

	@Column(nullable = false, name = "last_name")
	private String lastName;

	@Column(nullable = false, name = "date_of_birth")
	private LocalDate dateOfBirth;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false, name = "phone_number")
	private String phoneNumber;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, name = "password_confirm")
	private String passwordConfirm;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole userRole;

	public User() {
	}

	public User(User user) {
		this.id = user.id;
		this.email = user.email;
		this.firstName = user.firstName;
		this.lastName = user.lastName;
		this.dateOfBirth = user.dateOfBirth;
		this.city = user.city;
		this.phoneNumber = user.phoneNumber;
		this.password = user.password;
		this.passwordConfirm = user.passwordConfirm;
		this.userRole = user.userRole;
	}

	public User(String email, String firstName, String lastName, LocalDate dateOfBirth, String city, String phoneNumber,
			String password, String passwordConfirm, UserRole userRole) {
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
		this.city = city;
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
		this.userRole = userRole;
	}

	public User(Integer id, String email, String firstName, String lastName, LocalDate dateOfBirth, String city,
			String phoneNumber, String password, String passwordConfirm, UserRole userRole) {
		this.id = id;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
		this.city = city;
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
		this.userRole = userRole;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

	@Override
	public int hashCode() {
		return Objects.hash(city, dateOfBirth, email, firstName, id, lastName, password, passwordConfirm, phoneNumber,
				userRole);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		User other = (User) obj;
		return Objects.equals(city, other.city) && Objects.equals(dateOfBirth, other.dateOfBirth)
				&& Objects.equals(email, other.email) && Objects.equals(firstName, other.firstName)
				&& Objects.equals(id, other.id) && Objects.equals(lastName, other.lastName)
				&& Objects.equals(password, other.password) && Objects.equals(passwordConfirm, other.passwordConfirm)
				&& Objects.equals(phoneNumber, other.phoneNumber) && userRole == other.userRole;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", dateOfBirth=" + dateOfBirth + ", city=" + city + ", phoneNumber=" + phoneNumber + ", password="
				+ password + ", passwordConfirm=" + passwordConfirm + ", userRole=" + userRole + "]";
	}

}
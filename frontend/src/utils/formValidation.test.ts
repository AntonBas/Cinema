import { afterEach, describe, expect, it, vi } from "vitest";
import {
  validateCity,
  validateDateOfBirth,
  validateEmail,
  validateName,
  validatePassword,
  validatePasswordConfirm,
  validatePhoneNumber,
} from "./formValidation";

describe("validateName", () => {
  it.each(["Anna", "Олена", "Mary-Jane", "O'Brien", "Anna Maria"])(
    "accepts %s",
    (value) => {
      expect(validateName(value, "First name")).toBeUndefined();
    },
  );

  it("rejects empty, too short and non-letter names", () => {
    expect(validateName("  ", "First name")).toBe("First name is required");
    expect(validateName("A", "First name")).toBe(
      "First name must be between 2 and 50 characters",
    );
    expect(validateName("Anna1", "First name")).toBe(
      "First name may contain only letters, spaces, hyphens and apostrophes",
    );
  });
});

describe("validatePhoneNumber", () => {
  it("accepts 10 to 15 digits with an optional plus", () => {
    expect(validatePhoneNumber("+380671234567")).toBeUndefined();
    expect(validatePhoneNumber("0671234567")).toBeUndefined();
  });

  it("rejects empty and malformed numbers", () => {
    expect(validatePhoneNumber("")).toBe("Phone number is required");
    expect(validatePhoneNumber("067-123")).toBe("Invalid phone number format");
  });
});

describe("validatePassword", () => {
  it("requires 8 to 32 characters", () => {
    expect(validatePassword("")).toBe("Password is required");
    expect(validatePassword("short")).toBe(
      "Password must be between 8 and 32 characters",
    );
    expect(validatePassword("a".repeat(33))).toBe(
      "Password must be between 8 and 32 characters",
    );
    expect(validatePassword("password1")).toBeUndefined();
  });
});

describe("validatePasswordConfirm", () => {
  it("requires a matching confirmation", () => {
    expect(validatePasswordConfirm("password1", "")).toBe(
      "Password confirmation is required",
    );
    expect(validatePasswordConfirm("password1", "password2")).toBe(
      "Passwords do not match",
    );
    expect(validatePasswordConfirm("password1", "password1")).toBeUndefined();
  });
});

describe("validateEmail", () => {
  it("checks the email format", () => {
    expect(validateEmail("")).toBe("Email is required");
    expect(validateEmail("user@site")).toBe("Invalid email");
    expect(validateEmail(" user@site.com ")).toBeUndefined();
  });
});

describe("validateCity", () => {
  it("requires 2 to 50 characters", () => {
    expect(validateCity("")).toBe("City is required");
    expect(validateCity("L")).toBe(
      "City name must be between 2 and 50 characters",
    );
    expect(validateCity("Lviv")).toBeUndefined();
  });
});

describe("validateDateOfBirth", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("requires a valid past date", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-01-01T12:00:00"));

    expect(validateDateOfBirth("")).toBe("Date of birth is required");
    expect(validateDateOfBirth("1990-13-45")).toBe("Invalid date of birth");
    expect(validateDateOfBirth("2026-06-01")).toBe(
      "Date of birth must be in the past",
    );
    expect(validateDateOfBirth("1990-05-20")).toBeUndefined();
  });
});

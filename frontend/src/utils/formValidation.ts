const PHONE_PATTERN = /^\+?[0-9]{10,15}$/;
const PERSON_NAME_PATTERN = /^\p{L}+(?:[ '’-]\p{L}+)*$/u;

export const validateName = (
  value: string,
  fieldLabel: string,
): string | undefined => {
  const trimmed = value.trim();
  if (!trimmed) return `${fieldLabel} is required`;
  if (trimmed.length < 2 || trimmed.length > 50)
    return `${fieldLabel} must be between 2 and 50 characters`;
  if (!PERSON_NAME_PATTERN.test(trimmed))
    return `${fieldLabel} may contain only letters, spaces, hyphens and apostrophes`;
  return undefined;
};

export const validatePhoneNumber = (value: string): string | undefined => {
  if (!value.trim()) return "Phone number is required";
  if (!PHONE_PATTERN.test(value.trim())) return "Invalid phone number format";
  return undefined;
};

export const validatePassword = (value: string): string | undefined => {
  if (!value) return "Password is required";
  if (value.length < 8 || value.length > 32)
    return "Password must be between 8 and 32 characters";
  return undefined;
};

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const validateEmail = (value: string): string | undefined => {
  if (!value.trim()) return "Email is required";
  if (!EMAIL_PATTERN.test(value.trim())) return "Invalid email";
  return undefined;
};

export const validateCity = (value: string): string | undefined => {
  const trimmed = value.trim();
  if (!trimmed) return "City is required";
  if (trimmed.length < 2 || trimmed.length > 50)
    return "City name must be between 2 and 50 characters";
  return undefined;
};

export const validateDateOfBirth = (value: string): string | undefined => {
  if (!value) return "Date of birth is required";
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return "Invalid date of birth";
  if (date >= new Date()) return "Date of birth must be in the past";
  return undefined;
};

export const validatePasswordConfirm = (
  password: string,
  passwordConfirm: string,
): string | undefined => {
  if (!passwordConfirm) return "Password confirmation is required";
  if (password !== passwordConfirm) return "Passwords do not match";
  return undefined;
};

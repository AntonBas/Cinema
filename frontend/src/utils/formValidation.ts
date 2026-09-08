const PHONE_PATTERN = /^\+?[0-9]{10,15}$/;

export const validateName = (value: string, fieldLabel: string): string | undefined => {
  const trimmed = value.trim();
  if (!trimmed) return `${fieldLabel} is required`;
  if (trimmed.length < 2 || trimmed.length > 50)
    return `${fieldLabel} must be between 2 and 50 characters`;
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

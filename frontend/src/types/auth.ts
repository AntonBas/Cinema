import type { UserResponse } from "./user";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  user: UserResponse;
}

export interface RegisterRequest {
  email: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  city: string;
  phoneNumber: string;
  password: string;
  passwordConfirm: string;
}

export interface ResendVerificationResponse {
  cooldownSeconds: number;
}

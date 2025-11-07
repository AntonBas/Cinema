import type { ApiResponse, ErrorResponse } from './api';

export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    city: string;
    phoneNumber: string;
    userRole: UserRole;
    enabled: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export interface UserSimple {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    userRole: UserRole;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    success: boolean;
    token: string;
    user: User;
    tokenType: string;
    message?: string;
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

export interface RegisterResponse {
    success: boolean;
    message: string;
}

export interface ForgotPasswordRequest {
    email: string;
}

export interface ResetPasswordRequest {
    token: string;
    newPassword: string;
}

export interface EmailVerificationRequest {
    token: string;
}

export interface CheckEmailRequest {
    email: string;
}

export interface CheckEmailResponse {
    exists: boolean;
}

export type { ApiResponse, ErrorResponse };
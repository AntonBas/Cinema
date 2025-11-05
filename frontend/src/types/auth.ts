import type { UserSimple } from "./user";

export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    message: string;
    token: string;
    user: UserSimple;
    tokenType: string;
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

export interface ApiResponse {
    success: boolean;
    message: string;
    data?: any;
    errors?: any;
}

export interface ErrorResponse {
    success: boolean;
    message: string;
    errors?: any;
}
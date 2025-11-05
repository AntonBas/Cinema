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

export interface UserUpdateRequest {
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    city: string;
    phoneNumber: string;
}

export type { ApiResponse, ErrorResponse };
import type { UserRole, UserResponse } from './user';

export interface LoginResponse {
    token: string;
    tokenType: string;
    user: UserResponse;
}

export interface LoginRequest {
    email: string;
    password: string;
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

export interface CheckEmailRequest {
    email: string;
}

export interface CheckEmailResponse {
    exists: boolean;
}

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
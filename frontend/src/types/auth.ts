import type { UserRole } from './user';

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
    token: string;
    tokenType: string;
    user: User;
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
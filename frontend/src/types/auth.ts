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

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    message: string;
    token: string;
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
    message: string;
}

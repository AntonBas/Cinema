import type { UserRole } from './auth';

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
}

export interface UserSimple {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    userRole: UserRole;
}

export interface UserUpdateRequest {
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    city: string;
    phoneNumber: string;
}

export interface UserEmailChangeRequest {
    newEmail: string;
    password: string;
}

export interface UserPasswordChangeRequest {
    currentPassword: string;
    newPassword: string;
    passwordConfirm: string;
}
export type UserRole = 'ROLE_USER' | 'ROLE_CASHIER' | 'ROLE_CONTENT_MANAGER' | 'ROLE_ADMIN';

export interface UserProfile {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    city: string;
    phoneNumber: string;
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

export interface EmailChangeResponse {
    message: string;
}

export interface PasswordUpdateResponse {
    message: string;
}

export interface EmailChangeConfirmationResponse {
    success: boolean;
    id: number;
    email: string;
}

export interface AdminUser {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    userRole: UserRole;
    enabled: boolean;
    createdAt: string;
    updatedAt: string;
    ticketsCount: number;
    lastActivity: string;
}

export interface UserRoleUpdateRequest {
    userRole: UserRole;
}

export interface UserStatusUpdateRequest {
    enabled: boolean;
}

export interface AdminUsersResponse {
    content: AdminUser[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const UserRoleDisplay: Record<UserRole, string> = {
    ROLE_USER: 'User',
    ROLE_CASHIER: 'Cashier',
    ROLE_CONTENT_MANAGER: 'Content Manager',
    ROLE_ADMIN: 'Administrator'
};

export const UserStatusDisplay: Record<string, string> = {
    'true': 'Active',
    'false': 'Blocked'
};
export type UserRole = 'ROLE_USER' | 'ROLE_CASHIER' | 'ROLE_CONTENT_MANAGER' | 'ROLE_ADMIN';
export type VerificationStatus = 'VERIFIED' | 'NOT_VERIFIED';

export interface UserProfile {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    city: string;
    phoneNumber: string;
    verificationStatus: VerificationStatus;
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

export interface UserPasswordUpdateRequest {
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

export interface AdminUserListResponse {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    userRole: UserRole;
    enabled: boolean;
    verificationStatus: VerificationStatus;
    verifiedAt: string | null;
    createdAt: string;
    updatedAt: string;
    ticketsCount: number;
    lastActivity: string;
}

export interface UserProfileResponse {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    city: string;
    phoneNumber: string;
    verificationStatus: VerificationStatus;
}

export interface UserResponse {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    city: string;
    phoneNumber: string;
    userRole: UserRole;
    enabled: boolean;
    verificationStatus: VerificationStatus;
    createdAt: string;
}

export interface UserRoleUpdateRequest {
    userRole: UserRole;
}

export interface UserStatusUpdateRequest {
    enabled: boolean;
}

export interface VerificationBirthDateRequest {
    verificationStatus: VerificationStatus;
}

export interface AdminUsersResponse {
    content: AdminUserListResponse[];
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

export const VerificationStatusDisplay: Record<VerificationStatus, string> = {
    VERIFIED: 'Verified',
    NOT_VERIFIED: 'Not Verified'
};

export type AdminUser = AdminUserListResponse;

export const VerificationStatusColors: Record<VerificationStatus, string> = {
    VERIFIED: 'success',
    NOT_VERIFIED: 'secondary'
};
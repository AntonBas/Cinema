export type UserRole = 'ROLE_ADMIN' | 'ROLE_USER' | 'ROLE_CASHIER' | 'ROLE_CONTENT_MANAGER';
export type VerificationStatus = 'VERIFIED' | 'NOT_VERIFIED';

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
    ticketsCount: number;
    lastActivity: string;
}

export interface UserRegistrationRequest {
    email: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    city: string;
    phoneNumber: string;
    password: string;
    passwordConfirm: string;
}

export interface UserLoginRequest {
    email: string;
    password: string;
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
    ROLE_ADMIN: 'Administrator',
    ROLE_USER: 'User',
    ROLE_CASHIER: 'Cashier',
    ROLE_CONTENT_MANAGER: 'Content Manager'
};

export const VerificationStatusDisplay: Record<VerificationStatus, string> = {
    VERIFIED: 'Verified',
    NOT_VERIFIED: 'Not Verified'
};
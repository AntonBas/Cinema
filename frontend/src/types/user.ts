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
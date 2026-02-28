import { api } from '@/services/api';
import type {
    UserProfileResponse,
    UserUpdateRequest,
    UserPasswordUpdateRequest,
    UserEmailChangeRequest
} from '@/types/user';

const API_URL = '/api/users';

export const userApi = {
    getProfile: () =>
        api.get<UserProfileResponse>(`${API_URL}/profile`),

    updateProfile: (data: UserUpdateRequest) =>
        api.put<UserProfileResponse>(`${API_URL}/profile`, data),

    updatePassword: (data: UserPasswordUpdateRequest) =>
        api.patch<{ message: string }>(`${API_URL}/password`, data),

    requestEmailChange: (data: UserEmailChangeRequest) =>
        api.post<{ message: string }>(`${API_URL}/email/change-request`, data),
};
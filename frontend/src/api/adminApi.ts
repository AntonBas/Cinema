import { api } from '@/services/api';
import type {
    AdminUserListResponse,
    UserRoleUpdateRequest,
    UserStatusUpdateRequest,
    VerificationBirthDateRequest,
    UserRole,
    VerificationStatus
} from '@/types/user';
import type { PageResponse } from '@/types/pagination';

const ADMIN_API_URL = '/api/admin/users';

export const adminApi = {
    getUsers: (params: {
        page?: number;
        size?: number;
        search?: string;
        role?: UserRole;
        verificationStatus?: VerificationStatus;
        enabled?: boolean;
    }) => {
        return api.get<PageResponse<AdminUserListResponse>>(ADMIN_API_URL, { params });
    },

    updateUserRole: (userId: number, roleData: UserRoleUpdateRequest) => {
        return api.patch<AdminUserListResponse>(`${ADMIN_API_URL}/${userId}/role`, roleData);
    },

    updateUserStatus: (userId: number, statusData: UserStatusUpdateRequest) => {
        return api.patch<AdminUserListResponse>(`${ADMIN_API_URL}/${userId}/status`, statusData);
    },

    updateBirthDateVerification: (userId: number, verificationData: VerificationBirthDateRequest) => {
        return api.patch<AdminUserListResponse>(`${ADMIN_API_URL}/${userId}/verification`, verificationData);
    }
};
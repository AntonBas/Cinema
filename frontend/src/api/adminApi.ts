import type {
    AdminUserListResponse,
    UserRoleUpdateRequest,
    UserStatusUpdateRequest,
    VerificationBirthDateRequest,
    UserResponse
} from '@/types/user';
import type { PageResponse } from '@/types/pagination';
import { buildPagedUrl } from '@/utils/paginationUtils';
import { handleApiError } from '@/utils/apiErrorHandler';

const ADMIN_API_URL = '/api/admin/users';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, {
        headers: getAuthHeaders(),
        ...options,
    });

    if (!response.ok) throw await handleApiError(response);

    if (response.status === 204) return undefined as T;

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }

    return undefined as T;
};

export const adminApi = {
    getUsers: (params: {
        page?: number;
        size?: number;
        search?: string;
        role?: string;
        verificationStatus?: string;
        enabled?: boolean;
    }): Promise<PageResponse<AdminUserListResponse>> => {
        const url = buildPagedUrl(ADMIN_API_URL, params, 'admin');
        return fetchApi<PageResponse<AdminUserListResponse>>(url);
    },

    updateUserRole: (userId: number, roleData: UserRoleUpdateRequest): Promise<void> =>
        fetchApi<void>(`${ADMIN_API_URL}/${userId}/role`, {
            method: 'PATCH',
            body: JSON.stringify(roleData),
        }),

    updateUserStatus: (userId: number, statusData: UserStatusUpdateRequest): Promise<void> =>
        fetchApi<void>(`${ADMIN_API_URL}/${userId}/status`, {
            method: 'PATCH',
            body: JSON.stringify(statusData),
        }),

    updateBirthDateVerification: (userId: number, verificationData: VerificationBirthDateRequest): Promise<UserResponse> =>
        fetchApi<UserResponse>(`${ADMIN_API_URL}/${userId}/verification`, {
            method: 'PATCH',
            body: JSON.stringify(verificationData),
        })
};
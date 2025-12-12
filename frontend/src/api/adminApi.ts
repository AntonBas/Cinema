import type {
    AdminUsersResponse,
    UserRoleUpdateRequest,
    UserStatusUpdateRequest,
    VerificationBirthDateRequest
} from '@/types/user';
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

    if (response.status === 204) {
        return undefined as T;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }

    return undefined as T;
};

export const adminApi = {
    getUsers: (page: number = 0, size: number = 10, search?: string, role?: string, enabled?: boolean): Promise<AdminUsersResponse> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });
        if (search) params.append('search', search);
        if (role) params.append('role', role);
        if (enabled !== undefined) params.append('enabled', enabled.toString());

        return fetchApi<AdminUsersResponse>(`${ADMIN_API_URL}?${params}`);
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

    updateBirthDateVerification: (userId: number, verificationData: VerificationBirthDateRequest): Promise<void> =>
        fetchApi<void>(`${ADMIN_API_URL}/${userId}/birthdate-verification`, {
            method: 'PATCH',
            body: JSON.stringify(verificationData),
        })
};
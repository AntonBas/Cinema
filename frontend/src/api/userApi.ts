import { api } from "@/services/api";
import type {
  AdminUserListResponse,
  UserEmailChangeRequest,
  UserPasswordUpdateRequest,
  UserProfileResponse,
  UserRole,
  UserRoleUpdateRequest,
  UserStatusUpdateRequest,
  UserUpdateRequest,
  VerificationBirthDateRequest,
  VerificationStatus,
} from "@/types/user";
import type { PageResponse, SearchParams } from "@/types/pagination";

const BASE_URL = "/api/users";
const ADMIN_BASE_URL = "/api/admin/users";

export const userApi = {
  public: {
    getProfile: () => api.get<UserProfileResponse>(`${BASE_URL}/profile`),
    updateProfile: (data: UserUpdateRequest) =>
      api.put<UserProfileResponse>(`${BASE_URL}/profile`, data),
    updatePassword: (data: UserPasswordUpdateRequest) =>
      api.patch<{ message: string }>(`${BASE_URL}/password`, data),
    requestEmailChange: (data: UserEmailChangeRequest) =>
      api.post<{ message: string }>(`${BASE_URL}/email/change-request`, data),
  },
  admin: {
    getAll: (
      params: SearchParams & {
        query?: string;
        role?: UserRole;
        verificationStatus?: VerificationStatus;
        enabled?: boolean;
      },
    ) =>
      api.get<PageResponse<AdminUserListResponse>>(ADMIN_BASE_URL, { params }),
    updateRole: (userId: number, request: UserRoleUpdateRequest) =>
      api.patch<AdminUserListResponse>(`${ADMIN_BASE_URL}/${userId}/role`, request),
    updateStatus: (userId: number, request: UserStatusUpdateRequest) =>
      api.patch<AdminUserListResponse>(`${ADMIN_BASE_URL}/${userId}/status`, request),
    updateBirthDateVerification: (userId: number, request: VerificationBirthDateRequest) =>
      api.patch<AdminUserListResponse>(`${ADMIN_BASE_URL}/${userId}/verification`, request),
  },
};

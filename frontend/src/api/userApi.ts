import { api } from "@/services/api";
import type {
  UserProfileResponse,
  UserUpdateRequest,
  UserPasswordUpdateRequest,
  UserEmailChangeRequest,
} from "@/types/user";

const BASE_URL = "/api/users";

export const userApi = {
  getProfile: () => api.get<UserProfileResponse>(`${BASE_URL}/profile`),

  updateProfile: (data: UserUpdateRequest) =>
    api.put<UserProfileResponse>(`${BASE_URL}/profile`, data),

  updatePassword: (data: UserPasswordUpdateRequest) =>
    api.patch<{ message: string }>(`${BASE_URL}/password`, data),

  requestEmailChange: (data: UserEmailChangeRequest) =>
    api.post<{ message: string }>(`${BASE_URL}/email/change-request`, data),
};

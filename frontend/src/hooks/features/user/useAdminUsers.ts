import { useCallback, useRef } from "react";
import type {
  AdminUserListResponse,
  UserRole,
  VerificationStatus,
  UserRoleUpdateRequest,
  UserStatusUpdateRequest,
  VerificationBirthDateRequest,
} from "@/types/user";
import type { PageResponse, SearchParams } from "@/types/pagination";
import { userApi } from "@/api/userApi";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

interface AdminUsersParams extends SearchParams {
  query?: string;
  role?: UserRole;
  verificationStatus?: VerificationStatus;
  enabled?: boolean;
}

export const useAdminUsers = () => {
  const usersApi = useApi<PageResponse<AdminUserListResponse>>();
  const mutationApi = useApi<AdminUserListResponse>();

  const usersApiRef = useRef(usersApi);
  const mutationApiRef = useRef(mutationApi);

  usersApiRef.current = usersApi;
  mutationApiRef.current = mutationApi;

  const loading = useDelayedLoading(usersApi.loading || mutationApi.loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const getAll = useCallback(async (params?: AdminUsersParams) => {
    return usersApiRef.current.execute(() =>
      userApi.admin.getAll(params || {}),
    );
  }, []);

  const updateRole = useCallback(
    async (userId: number, userRole: UserRole, userName: string) => {
      const roleData: UserRoleUpdateRequest = { userRole };
      return mutationApiRef.current.execute(
        () => userApi.admin.updateRole(userId, roleData),
        {
          successMessage: `${userName} role updated successfully`,
          dedupeKey: `updateRole:${userId}`,
        },
      );
    },
    [],
  );

  const updateStatus = useCallback(
    async (userId: number, enabled: boolean, userName: string) => {
      const statusData: UserStatusUpdateRequest = { enabled };
      return mutationApiRef.current.execute(
        () => userApi.admin.updateStatus(userId, statusData),
        {
          successMessage: enabled
            ? `${userName} activated successfully`
            : `${userName} deactivated successfully`,
          dedupeKey: `updateStatus:${userId}`,
        },
      );
    },
    [],
  );

  const updateBirthDateVerification = useCallback(
    async (
      userId: number,
      verificationStatus: VerificationStatus,
      userName: string,
    ) => {
      const verificationData: VerificationBirthDateRequest = {
        verificationStatus,
      };
      const statusText =
        verificationStatus === "VERIFIED" ? "verified" : "unverified";
      return mutationApiRef.current.execute(
        () =>
          userApi.admin.updateBirthDateVerification(userId, verificationData),
        {
          successMessage: `${userName} birth date ${statusText}`,
          dedupeKey: `updateBirthDateVerification:${userId}`,
        },
      );
    },
    [],
  );

  return {
    users: usersApi.data?.content || [],
    pagination: usersApi.data,
    loading,
    error: usersApi.error || mutationApi.error,
    getAll,
    updateRole,
    updateStatus,
    updateBirthDateVerification,
    reset: usersApi.reset,
  };
};

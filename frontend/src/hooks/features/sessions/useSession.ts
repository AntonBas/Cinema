import { useCallback, useRef } from "react";
import { sessionApi } from "@/api/sessionApi";
import type {
  SessionResponse,
  SessionAdminResponse,
  SessionScheduleResponse,
  SessionRequest,
  CinemaSessionStatus,
} from "@/types/session";
import type { PageResponse, SearchParams } from "@/types/pagination";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

interface AdminSessionParams extends SearchParams {
  hallId?: number;
  movieTitle?: string;
  status?: CinemaSessionStatus;
  dateFrom?: string;
  dateTo?: string;
}

interface PublicSessionParams {
  searchTerm?: string;
  date?: string;
  movieId?: number;
}

export const useSession = () => {
  const adminSessionsApi = useApi<PageResponse<SessionAdminResponse>>();
  const publicSessionsApi = useApi<SessionScheduleResponse[]>();
  const sessionApiHook = useApi<SessionResponse>();
  const mutationApi = useApi<SessionResponse | void>();

  const adminSessionsApiRef = useRef(adminSessionsApi);
  const publicSessionsApiRef = useRef(publicSessionsApi);
  const sessionApiRef = useRef(sessionApiHook);
  const mutationApiRef = useRef(mutationApi);

  adminSessionsApiRef.current = adminSessionsApi;
  publicSessionsApiRef.current = publicSessionsApi;
  sessionApiRef.current = sessionApiHook;
  mutationApiRef.current = mutationApi;

  const loading = useDelayedLoading(
    adminSessionsApi.loading ||
      publicSessionsApi.loading ||
      sessionApiHook.loading ||
      mutationApi.loading,
    { delay: 150, minDisplayTime: 300 },
  );

  const getAdminSessions = useCallback(async (params?: AdminSessionParams) => {
    return adminSessionsApiRef.current.execute(() =>
      sessionApi.admin.getSessions(params),
    );
  }, []);

  const getSchedule = useCallback(async (params?: PublicSessionParams) => {
    return publicSessionsApiRef.current.execute(() =>
      sessionApi.public.getSchedule(params),
    );
  }, []);

  const getById = useCallback(async (id: number) => {
    return sessionApiRef.current.execute(() => sessionApi.admin.getById(id));
  }, []);

  const create = useCallback(async (request: SessionRequest) => {
    return mutationApiRef.current.execute(
      () => sessionApi.admin.create(request),
      { successMessage: "Session created successfully" },
    );
  }, []);

  const update = useCallback(async (id: number, request: SessionRequest) => {
    return mutationApiRef.current.execute(
      () => sessionApi.admin.update(id, request),
      { successMessage: "Session updated successfully" },
    );
  }, []);

  const cancel = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(() => sessionApi.admin.cancel(id), {
      successMessage: "Session cancelled successfully",
    });
  }, []);

  const reactivate = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(
      () => sessionApi.admin.reactivate(id),
      { successMessage: "Session reactivated successfully" },
    );
  }, []);

  const remove = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(() => sessionApi.admin.delete(id), {
      successMessage: "Session deleted successfully",
    });
  }, []);

  return {
    adminSessions: adminSessionsApi.data?.content || [],
    schedule: publicSessionsApi.data || [],
    session: sessionApiHook.data,
    pagination: adminSessionsApi.data,
    loading,
    adminError: adminSessionsApi.error,
    scheduleError: publicSessionsApi.error,
    sessionError: sessionApiHook.error,
    mutationError: mutationApi.error,
    getAdminSessions,
    getSchedule,
    getById,
    create,
    update,
    cancel,
    reactivate,
    remove,
  };
};

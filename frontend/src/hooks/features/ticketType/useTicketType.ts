import { useCallback, useRef } from "react";
import { ticketTypeApi } from "@/api/ticketTypeApi";
import type {
  TicketTypeResponse,
  TicketTypeRequest,
  TicketTypeCategory,
} from "@/types/ticketType";
import type { PageResponse } from "@/types/pagination";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

interface TicketTypeParams {
  active?: boolean;
  category?: TicketTypeCategory;
  query?: string;
  page?: number;
  size?: number;
}

export const useTicketType = () => {
  const ticketTypesApi = useApi<PageResponse<TicketTypeResponse>>();
  const mutationApi = useApi<TicketTypeResponse | void>();

  const ticketTypesApiRef = useRef(ticketTypesApi);
  const mutationApiRef = useRef(mutationApi);

  ticketTypesApiRef.current = ticketTypesApi;
  mutationApiRef.current = mutationApi;

  const loading = useDelayedLoading(
    ticketTypesApi.loading || mutationApi.loading,
    { delay: 150, minDisplayTime: 300 },
  );

  const getAll = useCallback(async (params?: TicketTypeParams) => {
    return ticketTypesApiRef.current.execute(() =>
      ticketTypeApi.admin.getAll(params),
    );
  }, []);

  const create = useCallback(async (request: TicketTypeRequest) => {
    return mutationApiRef.current.execute(
      () => ticketTypeApi.admin.create(request),
      { suppressValidationToast: true },
    );
  }, []);

  const update = useCallback(async (id: number, request: TicketTypeRequest) => {
    return mutationApiRef.current.execute(
      () => ticketTypeApi.admin.update(id, request),
      { suppressValidationToast: true },
    );
  }, []);

  const remove = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(() => ticketTypeApi.admin.delete(id));
  }, []);

  const toggleActive = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(() =>
      ticketTypeApi.admin.toggleActive(id),
    );
  }, []);

  return {
    ticketTypes: ticketTypesApi.data?.content || [],
    pagination: ticketTypesApi.data,
    loading,
    typesError: ticketTypesApi.error,
    mutationError: mutationApi.error,
    getAll,
    create,
    update,
    remove,
    toggleActive,
  };
};

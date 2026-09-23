import { useCallback, useRef } from "react";
import { personApi } from "@/api/personApi";
import type {
  PersonResponse,
  PersonRequest,
  PersonListResponse,
  PersonRole,
} from "@/types/person";
import type { PageResponse, SearchParams } from "@/types/pagination";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

interface PersonParams extends SearchParams {
  query?: string;
  role?: PersonRole;
}

export const usePerson = () => {
  const personsApi = useApi<PageResponse<PersonListResponse>>();
  const mutationApi = useApi<PersonResponse | void>();

  const personsApiRef = useRef(personsApi);
  const mutationApiRef = useRef(mutationApi);

  personsApiRef.current = personsApi;
  mutationApiRef.current = mutationApi;

  const loading = useDelayedLoading(personsApi.loading || mutationApi.loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const getAll = useCallback(async (params?: PersonParams) => {
    return personsApiRef.current.execute(() => personApi.admin.getAll(params));
  }, []);

  const create = useCallback(async (request: PersonRequest) => {
    return mutationApiRef.current.execute(
      () => personApi.admin.create(request),
      { suppressValidationToast: true, dedupeKey: "create" },
    );
  }, []);

  const update = useCallback(async (id: number, request: PersonRequest) => {
    return mutationApiRef.current.execute(
      () => personApi.admin.update(id, request),
      { suppressValidationToast: true, dedupeKey: `update:${id}` },
    );
  }, []);

  const remove = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(() => personApi.admin.delete(id), {
      dedupeKey: `remove:${id}`,
    });
  }, []);

  return {
    persons: personsApi.data?.content || [],
    pagination: personsApi.data,
    loading,
    personsError: personsApi.error,
    mutationError: mutationApi.error,
    getAll,
    create,
    update,
    remove,
  };
};

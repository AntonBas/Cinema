import { useCallback, useRef } from "react";
import { genreApi } from "@/api/genreApi";
import type {
  GenreResponse,
  GenreRequest,
  GenreListResponse,
} from "@/types/genre";
import type { PageResponse } from "@/types/pagination";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

export const useGenre = () => {
  const genresApi = useApi<PageResponse<GenreListResponse>>();
  const mutationApi = useApi<GenreResponse | void>();

  const genresApiRef = useRef(genresApi);
  const mutationApiRef = useRef(mutationApi);

  genresApiRef.current = genresApi;
  mutationApiRef.current = mutationApi;

  const loading = useDelayedLoading(genresApi.loading || mutationApi.loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const getAll = useCallback(
    async (params?: { query?: string; page?: number; size?: number }) => {
      return genresApiRef.current.execute(() => genreApi.admin.getAll(params));
    },
    [],
  );

  const create = useCallback(async (request: GenreRequest) => {
    return mutationApiRef.current.execute(
      () => genreApi.admin.create(request),
      {
        suppressValidationToast: true,
      },
    );
  }, []);

  const update = useCallback(async (id: number, request: GenreRequest) => {
    return mutationApiRef.current.execute(
      () => genreApi.admin.update(id, request),
      { suppressValidationToast: true },
    );
  }, []);

  const remove = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(() => genreApi.admin.delete(id));
  }, []);

  return {
    genres: genresApi.data?.content || [],
    pagination: genresApi.data,
    loading,
    genresError: genresApi.error,
    mutationError: mutationApi.error,
    getAll,
    create,
    update,
    remove,
  };
};

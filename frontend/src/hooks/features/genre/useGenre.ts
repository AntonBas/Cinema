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
  const allGenresApi = useApi<GenreResponse[]>();

  const genresApiRef = useRef(genresApi);
  const allGenresApiRef = useRef(allGenresApi);
  const mutationApiRef = useRef(mutationApi);

  genresApiRef.current = genresApi;
  allGenresApiRef.current = allGenresApi;
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

  const getAllOptions = useCallback(async () => {
    return allGenresApiRef.current.execute(() =>
      genreApi.admin.getAllOptions(),
    );
  }, []);

  const create = useCallback(async (request: GenreRequest) => {
    return mutationApiRef.current.execute(
      () => genreApi.admin.create(request),
      {
        suppressValidationToast: true,
        dedupeKey: "create",
      },
    );
  }, []);

  const update = useCallback(async (id: number, request: GenreRequest) => {
    return mutationApiRef.current.execute(
      () => genreApi.admin.update(id, request),
      { suppressValidationToast: true, dedupeKey: `update:${id}` },
    );
  }, []);

  const remove = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(() => genreApi.admin.delete(id), {
      dedupeKey: `remove:${id}`,
    });
  }, []);

  return {
    genres: genresApi.data?.content || [],
    allGenres: allGenresApi.data || [],
    pagination: genresApi.data,
    loading,
    genresError: genresApi.error,
    mutationError: mutationApi.error,
    getAll,
    getAllOptions,
    create,
    update,
    remove,
  };
};

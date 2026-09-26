import { useCallback, useRef } from "react";
import { cinemaHallApi } from "@/api/cinemaHallApi";
import type {
  CinemaHallRequest,
  CinemaHallListResponse,
  CinemaHallResponse,
  HallLayoutRequest,
  HallLayoutResponse,
} from "@/types/cinemaHall";
import { useApi } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";

export const useCinemaHall = () => {
  const hallsApi = useApi<CinemaHallListResponse[]>();
  const hallApi = useApi<CinemaHallResponse>();
  const layoutApi = useApi<HallLayoutResponse>();
  const mutationApi = useApi<CinemaHallResponse | void>();

  const hallsApiRef = useRef(hallsApi);
  const hallApiRef = useRef(hallApi);
  const layoutApiRef = useRef(layoutApi);
  const mutationApiRef = useRef(mutationApi);

  hallsApiRef.current = hallsApi;
  hallApiRef.current = hallApi;
  layoutApiRef.current = layoutApi;
  mutationApiRef.current = mutationApi;

  const loading = useDelayedLoading(
    hallsApi.loading ||
      hallApi.loading ||
      layoutApi.loading ||
      mutationApi.loading,
    { delay: 150, minDisplayTime: 300 },
  );

  const getAll = useCallback(async () => {
    return hallsApiRef.current.execute(() => cinemaHallApi.admin.getAll());
  }, []);

  const getById = useCallback(async (id: number) => {
    return hallApiRef.current.execute(() => cinemaHallApi.admin.getById(id));
  }, []);

  const getLayout = useCallback(async (id: number) => {
    return layoutApiRef.current.execute(() =>
      cinemaHallApi.admin.getLayout(id),
    );
  }, []);

  const updateLayout = useCallback(
    async (id: number, request: HallLayoutRequest) => {
      return layoutApiRef.current.execute(
        () => cinemaHallApi.admin.updateLayout(id, request),
        {
          successMessage: "Hall layout updated successfully",
          dedupeKey: `updateLayout:${id}`,
        },
      );
    },
    [],
  );

  const create = useCallback(async (request: CinemaHallRequest) => {
    return mutationApiRef.current.execute(
      () => cinemaHallApi.admin.create(request),
      { suppressValidationToast: true, dedupeKey: "create" },
    );
  }, []);

  const update = useCallback(async (id: number, request: CinemaHallRequest) => {
    return mutationApiRef.current.execute(
      () => cinemaHallApi.admin.update(id, request),
      { suppressValidationToast: true, dedupeKey: `update:${id}` },
    );
  }, []);

  const remove = useCallback(async (id: number) => {
    return mutationApiRef.current.execute(
      () => cinemaHallApi.admin.delete(id),
      {
        dedupeKey: `remove:${id}`,
      },
    );
  }, []);

  return {
    halls: hallsApi.data || [],
    selectedHall: hallApi.data,
    layout: layoutApi.data,
    loading,
    hallsError: hallsApi.error,
    hallError: hallApi.error,
    layoutError: layoutApi.error,
    mutationError: mutationApi.error,
    getAll,
    getById,
    getLayout,
    create,
    update,
    updateLayout,
    remove,
  };
};

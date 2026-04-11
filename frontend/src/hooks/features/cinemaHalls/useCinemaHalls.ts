import { useCallback, useRef } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type {
    CinemaHallRequest,
    CinemaHallListResponse,
    CinemaHallResponse,
    HallLayoutResponse
} from '@/types/cinemaHall';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useCinemaHalls = () => {
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
        hallsApi.loading || hallApi.loading || layoutApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getHallName = useCallback((id: number): string => {
        const hall = hallsApi.data?.find(h => h.id === id);
        return hall?.name || String(id);
    }, [hallsApi.data]);

    const getAllHalls = useCallback(async () => {
        return hallsApiRef.current.execute(() => cinemaHallApi.getAll());
    }, []);

    const getHallById = useCallback(async (id: number) => {
        return hallApiRef.current.execute(() => cinemaHallApi.getById(id));
    }, []);

    const getHallLayout = useCallback(async (id: number) => {
        return layoutApiRef.current.execute(() => cinemaHallApi.getLayout(id));
    }, []);

    const createHall = useCallback(async (request: CinemaHallRequest) => {
        return mutationApiRef.current.execute(
            () => cinemaHallApi.create(request),
            { successMessage: `Cinema hall "${request.name}" created successfully` }
        );
    }, []);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        return mutationApiRef.current.execute(
            () => cinemaHallApi.update(id, request),
            { successMessage: `Cinema hall "${getHallName(id)}" updated successfully` }
        );
    }, [getHallName]);

    const deleteHall = useCallback(async (id: number) => {
        return mutationApiRef.current.execute(
            () => cinemaHallApi.delete(id),
            { successMessage: `Cinema hall "${getHallName(id)}" deleted successfully` }
        );
    }, [getHallName]);

    return {
        halls: hallsApi.data || [],
        selectedHall: hallApi.data,
        layout: layoutApi.data,
        loading,
        hallsError: hallsApi.error,
        hallError: hallApi.error,
        layoutError: layoutApi.error,
        mutationError: mutationApi.error,
        getAllHalls,
        getHallById,
        getHallLayout,
        createHall,
        updateHall,
        deleteHall,
    };
};
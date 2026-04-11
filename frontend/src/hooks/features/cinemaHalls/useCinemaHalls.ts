import { useCallback } from 'react';
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

    const loading = useDelayedLoading(
        hallsApi.loading || hallApi.loading || layoutApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getHallName = useCallback((id: number): string => {
        const hall = hallsApi.data?.find(h => h.id === id);
        return hall?.name || String(id);
    }, [hallsApi.data]);

    const getAllHalls = useCallback(async () => {
        return hallsApi.execute(() => cinemaHallApi.getAll());
    }, [hallsApi]);

    const getHallById = useCallback(async (id: number) => {
        return hallApi.execute(() => cinemaHallApi.getById(id));
    }, [hallApi]);

    const getHallLayout = useCallback(async (id: number) => {
        return layoutApi.execute(() => cinemaHallApi.getLayout(id));
    }, [layoutApi]);

    const createHall = useCallback(async (request: CinemaHallRequest) => {
        return mutationApi.execute(
            () => cinemaHallApi.create(request),
            { successMessage: `Cinema hall "${request.name}" created successfully` }
        );
    }, [mutationApi]);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        return mutationApi.execute(
            () => cinemaHallApi.update(id, request),
            { successMessage: `Cinema hall "${getHallName(id)}" updated successfully` }
        );
    }, [mutationApi, getHallName]);

    const deleteHall = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => cinemaHallApi.delete(id),
            { successMessage: `Cinema hall "${getHallName(id)}" deleted successfully` }
        );
    }, [mutationApi, getHallName]);

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
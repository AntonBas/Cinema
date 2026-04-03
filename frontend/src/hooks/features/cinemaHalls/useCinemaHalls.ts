import { useCallback } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type {
    CinemaHallRequest,
    CinemaHallResponse,
    HallLayoutResponse
} from '@/types/cinemaHall';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useCinemaHalls = () => {
    const getAllHallsApi = useApi<CinemaHallResponse[]>();
    const getHallByIdApi = useApi<CinemaHallResponse>();
    const getHallLayoutApi = useApi<HallLayoutResponse>();
    const createHallApi = useApi<CinemaHallResponse>();
    const updateHallApi = useApi<CinemaHallResponse>();
    const deleteHallApi = useApi<void>();

    const rawLoading = getAllHallsApi.loading || getHallByIdApi.loading ||
        getHallLayoutApi.loading || createHallApi.loading || updateHallApi.loading || deleteHallApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getAllHallsApi.error || getHallByIdApi.error ||
        getHallLayoutApi.error || createHallApi.error || updateHallApi.error || deleteHallApi.error);

    const getAllHalls = useCallback(async () => {
        const response = await getAllHallsApi.execute(
            () => cinemaHallApi.getAll(),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getAllHallsApi]);

    const getHallById = useCallback(async (id: number) => {
        const response = await getHallByIdApi.execute(
            () => cinemaHallApi.getById(id),
            { showErrorNotification: true }
        );
        return response || null;
    }, [getHallByIdApi]);

    const createHall = useCallback(async (request: CinemaHallRequest) => {
        const response = await createHallApi.execute(
            () => cinemaHallApi.create(request),
            { successMessage: `Cinema hall "${request.name}" created successfully` }
        );
        return response || null;
    }, [createHallApi]);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest, oldName?: string) => {
        const response = await updateHallApi.execute(
            () => cinemaHallApi.update(id, request),
            { successMessage: `Cinema hall "${oldName || request.name}" updated successfully` }
        );
        return response || null;
    }, [updateHallApi]);

    const deleteHall = useCallback(async (id: number, hallName?: string) => {
        await deleteHallApi.execute(
            () => cinemaHallApi.delete(id),
            { successMessage: `Cinema hall "${hallName || id}" deleted successfully` }
        );
    }, [deleteHallApi]);

    const getHallLayout = useCallback(async (id: number) => {
        const response = await getHallLayoutApi.execute(
            () => cinemaHallApi.getLayout(id),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getHallLayoutApi]);

    const resetAll = useCallback(() => {
        getAllHallsApi.reset();
        getHallByIdApi.reset();
        getHallLayoutApi.reset();
        createHallApi.reset();
        updateHallApi.reset();
        deleteHallApi.reset();
    }, [getAllHallsApi, getHallByIdApi, getHallLayoutApi, createHallApi, updateHallApi, deleteHallApi]);

    return {
        allHalls: getAllHallsApi.data || [],
        selectedHall: getHallByIdApi.data,
        hallLayout: getHallLayoutApi.data,
        loading,
        error,
        getAllHalls,
        getHallById,
        createHall,
        updateHall,
        deleteHall,
        getHallLayout,
        resetAll,
    };
};
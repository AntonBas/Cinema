import { useCallback } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type {
    CinemaHallRequest,
    CinemaHallResponse,
    HallLayoutResponse
} from '@/types/cinemaHall';
import { useApi } from '@/hooks/common/useApi';

export const useCinemaHalls = () => {
    const allHallsApi = useApi<CinemaHallResponse[]>();
    const hallDetailApi = useApi<CinemaHallResponse>();
    const createHallApi = useApi<CinemaHallResponse>();
    const updateHallApi = useApi<CinemaHallResponse>();
    const deleteHallApi = useApi<void>();
    const hallLayoutApi = useApi<HallLayoutResponse>();

    const getAllHalls = useCallback(async () => {
        const response = await allHallsApi.execute(
            () => cinemaHallApi.admin.getAll(),
            {
                cacheKey: 'all_halls',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [allHallsApi]);

    const getHallById = useCallback(async (id: number) => {
        const response = await hallDetailApi.execute(
            () => cinemaHallApi.admin.getById(id),
            {
                cacheKey: `hall_detail_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: true,
            }
        );
        return response?.data || null;
    }, [hallDetailApi]);

    const createHall = useCallback(async (request: CinemaHallRequest) => {
        const response = await createHallApi.execute(
            () => cinemaHallApi.admin.create(request),
            {
                successMessage: 'Cinema hall created successfully',
            }
        );
        allHallsApi.invalidateCache('all_halls');
        await getAllHalls();
        return response?.data || null;
    }, [createHallApi, allHallsApi, getAllHalls]);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        const response = await updateHallApi.execute(
            () => cinemaHallApi.admin.update(id, request),
            {
                successMessage: 'Cinema hall updated successfully',
            }
        );
        allHallsApi.invalidateCache('all_halls');
        hallDetailApi.invalidateCache(`hall_detail_${id}`);
        hallLayoutApi.invalidateCache(`hall_layout_${id}`);
        await getAllHalls();
        return response?.data || null;
    }, [updateHallApi, allHallsApi, hallDetailApi, hallLayoutApi, getAllHalls]);

    const deleteHall = useCallback(async (id: number) => {
        await deleteHallApi.execute(
            () => cinemaHallApi.admin.delete(id),
            {
                successMessage: 'Cinema hall deleted successfully',
            }
        );
        allHallsApi.invalidateCache('all_halls');
        hallDetailApi.invalidateCache(`hall_detail_${id}`);
        hallLayoutApi.invalidateCache(`hall_layout_${id}`);
        await getAllHalls();
    }, [deleteHallApi, allHallsApi, hallDetailApi, hallLayoutApi, getAllHalls]);

    const getHallLayout = useCallback(async (id: number) => {
        const response = await hallLayoutApi.execute(
            () => cinemaHallApi.admin.getLayout(id),
            {
                cacheKey: `hall_layout_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [hallLayoutApi]);

    const loading = allHallsApi.loading || hallDetailApi.loading ||
        createHallApi.loading || updateHallApi.loading ||
        deleteHallApi.loading || hallLayoutApi.loading;

    const error = !!(allHallsApi.error || hallDetailApi.error ||
        createHallApi.error || updateHallApi.error ||
        deleteHallApi.error || hallLayoutApi.error);

    return {
        allHalls: allHallsApi.data || [],
        selectedHall: hallDetailApi.data,
        hallLayout: hallLayoutApi.data,
        loading,
        error,
        getAllHalls,
        getHallById,
        createHall,
        updateHall,
        deleteHall,
        getHallLayout,
        resetAllHalls: allHallsApi.reset,
        resetSelectedHall: hallDetailApi.reset,
        resetHallLayout: hallLayoutApi.reset,
    };
};
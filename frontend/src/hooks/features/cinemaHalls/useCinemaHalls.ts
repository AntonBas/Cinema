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
    const createHallApi = useApi<CinemaHallResponse>();
    const updateHallApi = useApi<CinemaHallResponse>();
    const deleteHallApi = useApi<void>();
    const hallLayoutApi = useApi<HallLayoutResponse>();

    const getAllHalls = useCallback(async () => {
        return allHallsApi.execute(
            () => cinemaHallApi.admin.getAll(),
            {
                cacheKey: 'all_halls',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allHallsApi]);

    const createHall = useCallback(async (request: CinemaHallRequest) => {
        const result = await createHallApi.execute(
            () => cinemaHallApi.admin.create(request),
            {
                successMessage: 'Cinema hall created successfully',
                showErrorNotification: true,
            }
        );
        await allHallsApi.invalidateCache('all_halls');
        await getAllHalls();
        return result;
    }, [createHallApi, allHallsApi, getAllHalls]);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        const result = await updateHallApi.execute(
            () => cinemaHallApi.admin.update(id, request),
            {
                successMessage: 'Cinema hall updated successfully',
                showErrorNotification: true,
            }
        );
        await allHallsApi.invalidateCache('all_halls');
        await getAllHalls();
        return result;
    }, [updateHallApi, allHallsApi, getAllHalls]);

    const deleteHall = useCallback(async (id: number) => {
        const result = await deleteHallApi.execute(
            () => cinemaHallApi.admin.delete(id),
            {
                successMessage: 'Cinema hall deleted successfully',
                showErrorNotification: true,
            }
        );
        await allHallsApi.invalidateCache('all_halls');
        await getAllHalls();
        return result;
    }, [deleteHallApi, allHallsApi, getAllHalls]);

    const getHallLayout = useCallback(async (id: number) => {
        return hallLayoutApi.execute(
            () => cinemaHallApi.admin.getLayout(id),
            {
                cacheKey: `hall_layout_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [hallLayoutApi]);

    const loading = allHallsApi.loading || createHallApi.loading ||
        updateHallApi.loading || deleteHallApi.loading ||
        hallLayoutApi.loading;
    const error = !!(allHallsApi.error || createHallApi.error ||
        updateHallApi.error || deleteHallApi.error ||
        hallLayoutApi.error);

    return {
        allHalls: allHallsApi.data || [],
        loading,
        error,
        getAllHalls,
        createHall,
        updateHall,
        deleteHall,
        getHallLayout,
        hallLayout: hallLayoutApi.data,
        resetAllHalls: allHallsApi.reset,
        resetCreate: createHallApi.reset,
        resetUpdate: updateHallApi.reset,
        resetDelete: deleteHallApi.reset,
        resetHallLayout: hallLayoutApi.reset,
    };
};
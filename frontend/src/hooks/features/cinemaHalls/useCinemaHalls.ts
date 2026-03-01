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
    const hallsApi = useApi<CinemaHallResponse[]>();
    const hallDetailApi = useApi<CinemaHallResponse>();
    const hallLayoutApi = useApi<HallLayoutResponse>();
    const mutationApi = useApi<CinemaHallResponse | void>();

    const rawLoading = hallsApi.loading || hallDetailApi.loading ||
        hallLayoutApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(hallsApi.error || hallDetailApi.error ||
        hallLayoutApi.error || mutationApi.error);

    const getAllHalls = useCallback(async () => {
        const response = await hallsApi.execute(
            () => cinemaHallApi.admin.getAll(),
            {
                cacheKey: 'all_halls',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [hallsApi]);

    const getHallById = useCallback(async (id: number) => {
        const response = await hallDetailApi.execute(
            () => cinemaHallApi.admin.getById(id),
            {
                cacheKey: `hall_detail_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: true,
            }
        );
        return response || null;
    }, [hallDetailApi]);

    const createHall = useCallback(async (request: CinemaHallRequest) => {
        const response = await mutationApi.execute(
            () => cinemaHallApi.admin.create(request),
            {
                successMessage: 'Cinema hall created successfully',
            }
        );
        hallsApi.invalidateCache('all_halls');
        return response || null;
    }, [mutationApi, hallsApi]);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        const response = await mutationApi.execute(
            () => cinemaHallApi.admin.update(id, request),
            {
                successMessage: 'Cinema hall updated successfully',
            }
        );
        hallsApi.invalidateCache('all_halls');
        hallDetailApi.invalidateCache(`hall_detail_${id}`);
        hallLayoutApi.invalidateCache(`hall_layout_${id}`);
        return response || null;
    }, [mutationApi, hallsApi, hallDetailApi, hallLayoutApi]);

    const deleteHall = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => cinemaHallApi.admin.delete(id),
            {
                successMessage: 'Cinema hall deleted successfully',
            }
        );
        hallsApi.invalidateCache('all_halls');
        hallDetailApi.invalidateCache(`hall_detail_${id}`);
        hallLayoutApi.invalidateCache(`hall_layout_${id}`);
    }, [mutationApi, hallsApi, hallDetailApi, hallLayoutApi]);

    const getHallLayout = useCallback(async (id: number) => {
        const response = await hallLayoutApi.execute(
            () => cinemaHallApi.admin.getLayout(id),
            {
                cacheKey: `hall_layout_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [hallLayoutApi]);

    const clearCache = useCallback(() => {
        hallsApi.invalidateCache();
        hallDetailApi.invalidateCache();
        hallLayoutApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [hallsApi, hallDetailApi, hallLayoutApi, mutationApi]);

    const resetAll = useCallback(() => {
        hallsApi.reset();
        hallDetailApi.reset();
        hallLayoutApi.reset();
        mutationApi.reset();
    }, [hallsApi, hallDetailApi, hallLayoutApi, mutationApi]);

    return {
        allHalls: hallsApi.data || [],
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

        clearCache,
        resetAll,
    };
};
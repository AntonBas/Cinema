import { useCallback } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type {
    CinemaHallRequest,
    CinemaHallResponse,
    CinemaHallWithSeatsResponse,
    HallLayoutResponse
} from '@/types/cinemaHall';
import { useApi } from '@/hooks/common/useApi';

export const useCinemaHalls = () => {
    const allHallsApi = useApi<CinemaHallResponse[]>();
    const hallByIdApi = useApi<CinemaHallResponse>();
    const hallWithSeatsApi = useApi<CinemaHallWithSeatsResponse>();
    const hallLayoutApi = useApi<HallLayoutResponse>();

    const getAllHalls = useCallback(async (name?: string) => {
        return allHallsApi.callApi(
            () => cinemaHallApi.getAll(name),
            {
                cacheKey: `cinema_halls_${name || 'all'}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allHallsApi]);

    const getHallById = useCallback(async (id: number) => {
        return hallByIdApi.callApi(
            () => cinemaHallApi.getById(id),
            {
                cacheKey: `cinema_hall_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [hallByIdApi]);

    const getHallWithSeats = useCallback(async (id: number) => {
        return hallWithSeatsApi.callApi(
            () => cinemaHallApi.getWithSeats(id),
            {
                cacheKey: `cinema_hall_seats_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [hallWithSeatsApi]);

    const getHallLayout = useCallback(async (id: number) => {
        return hallLayoutApi.callApi(
            () => cinemaHallApi.getLayout(id),
            {
                cacheKey: `cinema_hall_layout_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [hallLayoutApi]);

    const createHall = useCallback(async (request: CinemaHallRequest) => {
        const api = useApi<CinemaHallResponse>();
        return api.callApi(
            () => cinemaHallApi.admin.create(request),
            {
                successMessage: 'Cinema hall created successfully',
                onSuccess: () => {
                    allHallsApi.invalidateCache();
                },
            }
        );
    }, [allHallsApi]);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        const api = useApi<CinemaHallResponse>();
        return api.callApi(
            () => cinemaHallApi.admin.update(id, request),
            {
                successMessage: 'Cinema hall updated successfully',
                onSuccess: () => {
                    allHallsApi.invalidateCache();
                    hallByIdApi.invalidateCache(`cinema_hall_${id}`);
                    hallWithSeatsApi.invalidateCache(`cinema_hall_seats_${id}`);
                    hallLayoutApi.invalidateCache(`cinema_hall_layout_${id}`);
                },
            }
        );
    }, [allHallsApi, hallByIdApi, hallWithSeatsApi, hallLayoutApi]);

    const deleteHall = useCallback(async (id: number) => {
        const api = useApi<void>();
        return api.callApi(
            () => cinemaHallApi.admin.delete(id),
            {
                successMessage: 'Cinema hall deleted successfully',
                onSuccess: () => {
                    allHallsApi.invalidateCache();
                },
            }
        );
    }, [allHallsApi]);

    const clearCache = useCallback(() => {
        allHallsApi.invalidateCache();
        hallByIdApi.invalidateCache();
        hallWithSeatsApi.invalidateCache();
        hallLayoutApi.invalidateCache();
    }, [allHallsApi, hallByIdApi, hallWithSeatsApi, hallLayoutApi]);

    return {
        allHalls: allHallsApi.data || [],
        hall: hallByIdApi.data,
        hallWithSeats: hallWithSeatsApi.data,
        hallLayout: hallLayoutApi.data,

        loading: allHallsApi.state.isLoading || hallByIdApi.state.isLoading ||
            hallWithSeatsApi.state.isLoading || hallLayoutApi.state.isLoading,
        error: allHallsApi.state.isError || hallByIdApi.state.isError ||
            hallWithSeatsApi.state.isError || hallLayoutApi.state.isError,

        getAllHalls,
        getHallById,
        getHallWithSeats,
        getHallLayout,
        createHall,
        updateHall,
        deleteHall,
        clearCache,

        resetAllHalls: allHallsApi.reset,
        resetHall: hallByIdApi.reset,
        resetHallWithSeats: hallWithSeatsApi.reset,
        resetHallLayout: hallLayoutApi.reset,
        refetchAllHalls: allHallsApi.refetch,
        refetchHall: hallByIdApi.refetch,
    };
};
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
    const createHallApi = useApi<CinemaHallResponse>();
    const updateHallApi = useApi<CinemaHallResponse>();
    const deleteHallApi = useApi<void>();

    const getAllHalls = useCallback(async (name?: string) => {
        return allHallsApi.execute(
            () => cinemaHallApi.getAll(name),
            {
                showErrorNotification: true,
            }
        );
    }, [allHallsApi]);

    const getHallById = useCallback(async (id: number) => {
        return hallByIdApi.execute(
            () => cinemaHallApi.getById(id),
            {
                showErrorNotification: true,
            }
        );
    }, [hallByIdApi]);

    const getHallWithSeats = useCallback(async (id: number) => {
        return hallWithSeatsApi.execute(
            () => cinemaHallApi.getWithSeats(id),
            {
                showErrorNotification: true,
            }
        );
    }, [hallWithSeatsApi]);

    const getHallLayout = useCallback(async (id: number) => {
        return hallLayoutApi.execute(
            () => cinemaHallApi.getLayout(id),
            {
                showErrorNotification: true,
            }
        );
    }, [hallLayoutApi]);

    const createHall = useCallback(async (request: CinemaHallRequest) => {
        return createHallApi.execute(
            () => cinemaHallApi.admin.create(request),
            {
                successMessage: 'Cinema hall created successfully',
                showErrorNotification: true,
                onSuccess: () => {
                    allHallsApi.invalidateCache();
                },
            }
        );
    }, [createHallApi, allHallsApi]);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        return updateHallApi.execute(
            () => cinemaHallApi.admin.update(id, request),
            {
                successMessage: 'Cinema hall updated successfully',
                showErrorNotification: true,
                onSuccess: () => {
                    allHallsApi.invalidateCache();
                    hallByIdApi.invalidateCache(`cinema_hall_${id}`);
                    hallWithSeatsApi.invalidateCache(`cinema_hall_seats_${id}`);
                    hallLayoutApi.invalidateCache(`cinema_hall_layout_${id}`);
                },
            }
        );
    }, [updateHallApi, allHallsApi, hallByIdApi, hallWithSeatsApi, hallLayoutApi]);

    const deleteHall = useCallback(async (id: number) => {
        return deleteHallApi.execute(
            () => cinemaHallApi.admin.delete(id),
            {
                successMessage: 'Cinema hall deleted successfully',
                showErrorNotification: true,
                onSuccess: () => {
                    allHallsApi.invalidateCache();
                },
            }
        );
    }, [deleteHallApi, allHallsApi]);

    const clearCache = useCallback(() => {
        allHallsApi.invalidateCache();
        hallByIdApi.invalidateCache();
        hallWithSeatsApi.invalidateCache();
        hallLayoutApi.invalidateCache();
        createHallApi.invalidateCache();
        updateHallApi.invalidateCache();
        deleteHallApi.invalidateCache();
    }, [allHallsApi, hallByIdApi, hallWithSeatsApi, hallLayoutApi,
        createHallApi, updateHallApi, deleteHallApi]);

    const loading = allHallsApi.loading || hallByIdApi.loading ||
        hallWithSeatsApi.loading || hallLayoutApi.loading ||
        createHallApi.loading || updateHallApi.loading ||
        deleteHallApi.loading;

    const error = !!(allHallsApi.error || hallByIdApi.error ||
        hallWithSeatsApi.error || hallLayoutApi.error ||
        createHallApi.error || updateHallApi.error ||
        deleteHallApi.error);

    return {
        allHalls: allHallsApi.data || [],
        hall: hallByIdApi.data,
        hallWithSeats: hallWithSeatsApi.data,
        hallLayout: hallLayoutApi.data,

        loading,
        error,

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
    };
};
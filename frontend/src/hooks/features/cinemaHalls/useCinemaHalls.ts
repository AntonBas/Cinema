import { useState, useCallback, useRef } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallRequest, CinemaHallResponse, CinemaHallWithSeatsResponse, HallLayoutResponse } from '@/types/cinemaHall';
import { useApi } from '@/hooks/common/useApi';

export const useCinemaHalls = () => {
    const [allHalls, setAllHalls] = useState<CinemaHallResponse[]>([]);
    const [hallWithSeats, setHallWithSeats] = useState<CinemaHallWithSeatsResponse | null>(null);
    const [hallLayout, setHallLayout] = useState<HallLayoutResponse | null>(null);

    const apiHookRef = useRef(useApi<CinemaHallResponse[]>());
    const apiHook = apiHookRef.current;

    const getHallByIdHook = useApi<CinemaHallResponse>();
    const getHallWithSeatsHook = useApi<CinemaHallWithSeatsResponse>();
    const getHallLayoutHook = useApi<HallLayoutResponse>();
    const createHallHook = useApi<CinemaHallResponse>();
    const updateHallHook = useApi<CinemaHallResponse>();
    const deleteHallHook = useApi<void>();

    const getAllHalls = useCallback(async (): Promise<CinemaHallResponse[]> => {
        return apiHook.callApi(async () => {
            const data = await cinemaHallApi.getAll();
            setAllHalls(data);
            return data;
        });
    }, [apiHook]);

    const getHallById = useCallback(async (id: number): Promise<CinemaHallResponse> => {
        return getHallByIdHook.callApi(async () => {
            return await cinemaHallApi.getById(id);
        });
    }, [getHallByIdHook]);

    const getHallWithSeats = useCallback(async (id: number): Promise<CinemaHallWithSeatsResponse> => {
        return getHallWithSeatsHook.callApi(async () => {
            const data = await cinemaHallApi.getWithSeats(id);
            setHallWithSeats(data);
            return data;
        });
    }, [getHallWithSeatsHook]);

    const getHallLayout = useCallback(async (id: number): Promise<HallLayoutResponse> => {
        return getHallLayoutHook.callApi(async () => {
            const data = await cinemaHallApi.getLayout(id);
            setHallLayout(data);
            return data;
        });
    }, [getHallLayoutHook]);

    const searchHalls = useCallback(async (name?: string): Promise<CinemaHallResponse[]> => {
        return apiHook.callApi(async () => {
            return await cinemaHallApi.search(name);
        });
    }, [apiHook]);

    const createHall = useCallback(async (request: CinemaHallRequest): Promise<CinemaHallResponse> => {
        return createHallHook.callApi(async () => {
            const hall = await cinemaHallApi.admin.create(request);
            setAllHalls(prev => [...prev, hall]);
            return hall;
        });
    }, [createHallHook]);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest): Promise<CinemaHallResponse> => {
        return updateHallHook.callApi(async () => {
            const hall = await cinemaHallApi.admin.update(id, request);
            setAllHalls(prevHalls => prevHalls.map(h => h.id === id ? hall : h));
            return hall;
        });
    }, [updateHallHook]);

    const deleteHall = useCallback(async (id: number): Promise<void> => {
        return deleteHallHook.callApi(async () => {
            await cinemaHallApi.admin.delete(id);
            setAllHalls(prevHalls => prevHalls.filter(h => h.id !== id));
        });
    }, [deleteHallHook]);

    const refreshHalls = useCallback(() => {
        getAllHalls();
    }, [getAllHalls]);

    return {
        allHalls,
        hallWithSeats,
        hallLayout,
        loading: apiHook.loading || getHallByIdHook.loading || getHallWithSeatsHook.loading ||
            getHallLayoutHook.loading || createHallHook.loading ||
            updateHallHook.loading || deleteHallHook.loading,
        getAllHalls,
        getHallById,
        getHallWithSeats,
        getHallLayout,
        searchHalls,
        createHall,
        updateHall,
        deleteHall,
        refreshHalls,
    };
};
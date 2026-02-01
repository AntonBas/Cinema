import { useState, useCallback, useRef } from 'react';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import type {
    TicketTypeResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest,
    TicketTypeCategory,
    TicketTypeSimpleResponse
} from '@/types/ticketType';
import { useApi } from '@/hooks/common/useApi';

export const useTicketType = () => {
    const [ticketTypes, setTicketTypes] = useState<TicketTypeResponse[]>([]);
    const [dropdownTypes, setDropdownTypes] = useState<TicketTypeSimpleResponse[]>([]);
    const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');
    const [categoryFilter, setCategoryFilter] = useState<TicketTypeCategory | 'all'>('all');
    const [searchQuery, setSearchQuery] = useState<string>('');

    const apiHookRef = useRef(useApi<TicketTypeResponse[]>());
    const apiHook = apiHookRef.current;

    const getByIdHook = useApi<TicketTypeResponse>();
    const getByCodeHook = useApi<TicketTypeResponse>();
    const createHook = useApi<TicketTypeResponse>();
    const updateHook = useApi<TicketTypeResponse>();
    const deleteHook = useApi<void>();
    const toggleActiveHook = useApi<TicketTypeResponse>();
    const getActiveForDropdownHook = useApi<TicketTypeSimpleResponse[]>();
    const getDropdownTypesHook = useApi<TicketTypeSimpleResponse[]>();

    const getAll = useCallback(async (params?: { active?: boolean; category?: string; search?: string }): Promise<TicketTypeResponse[]> => {
        return apiHook.callApi(async () => {
            const data = await ticketTypeApi.admin.getAll(params);
            setTicketTypes(data);
            return data;
        }, { showErrorNotification: false });
    }, [apiHook]);

    const getById = useCallback(async (id: number): Promise<TicketTypeResponse> => {
        return getByIdHook.callApi(async () => {
            return await ticketTypeApi.admin.getById(id);
        }, { showErrorNotification: false });
    }, [getByIdHook]);

    const getByCode = useCallback(async (code: string): Promise<TicketTypeResponse> => {
        return getByCodeHook.callApi(async () => {
            return await ticketTypeApi.admin.getByCode(code);
        }, { showErrorNotification: false });
    }, [getByCodeHook]);

    const create = useCallback(async (request: TicketTypeCreateRequest): Promise<TicketTypeResponse> => {
        return createHook.callApi(async () => {
            const response = await ticketTypeApi.admin.create(request);
            setTicketTypes(prev => [...prev, response]);
            return response;
        }, { showErrorNotification: false });
    }, [createHook]);

    const update = useCallback(async (id: number, request: TicketTypeUpdateRequest): Promise<TicketTypeResponse> => {
        return updateHook.callApi(async () => {
            const response = await ticketTypeApi.admin.update(id, request);
            setTicketTypes(prev => prev.map(tt => tt.id === id ? response : tt));
            return response;
        }, { showErrorNotification: false });
    }, [updateHook]);

    const remove = useCallback(async (id: number): Promise<void> => {
        return deleteHook.callApi(async () => {
            await ticketTypeApi.admin.delete(id);
            setTicketTypes(prev => prev.filter(tt => tt.id !== id));
        }, { showErrorNotification: false });
    }, [deleteHook]);

    const toggleActive = useCallback(async (id: number): Promise<TicketTypeResponse> => {
        return toggleActiveHook.callApi(async () => {
            const response = await ticketTypeApi.admin.toggleActive(id);
            setTicketTypes(prev => prev.map(tt => tt.id === id ? response : tt));
            return response;
        }, { showErrorNotification: false });
    }, [toggleActiveHook]);

    const getActiveForDropdown = useCallback(async (): Promise<TicketTypeSimpleResponse[]> => {
        return getActiveForDropdownHook.callApi(async () => {
            const data = await ticketTypeApi.admin.getActiveForDropdown();
            setDropdownTypes(data);
            return data;
        }, { showErrorNotification: false });
    }, [getActiveForDropdownHook]);

    const getDropdownTypes = useCallback(async (): Promise<TicketTypeSimpleResponse[]> => {
        return getDropdownTypesHook.callApi(async () => {
            const data = await ticketTypeApi.public.getDropdownTypes();
            setDropdownTypes(data);
            return data;
        }, { showErrorNotification: false });
    }, [getDropdownTypesHook]);

    const fetchTicketTypes = useCallback(async (options?: {
        statusFilter?: 'all' | 'active' | 'inactive';
        categoryFilter?: TicketTypeCategory | 'all';
        searchQuery?: string;
    }) => {
        const params: { active?: boolean; category?: string; search?: string } = {};

        if (options?.statusFilter === 'active') {
            params.active = true;
        } else if (options?.statusFilter === 'inactive') {
            params.active = false;
        }

        if (options?.categoryFilter && options.categoryFilter !== 'all') {
            params.category = options.categoryFilter;
        }

        if (options?.searchQuery?.trim()) {
            params.search = options.searchQuery;
        }

        return apiHook.callApi(async () => {
            const data = await ticketTypeApi.admin.getAll(params);
            setTicketTypes(data);
            return data;
        }, { showErrorNotification: false });
    }, [apiHook]);

    const addTicketType = useCallback((ticketType: TicketTypeResponse) => {
        setTicketTypes(prev => [...prev, ticketType]);
    }, []);

    const updateTicketType = useCallback((updatedTicketType: TicketTypeResponse) => {
        setTicketTypes(prev => prev.map(tt => tt.id === updatedTicketType.id ? updatedTicketType : tt));
    }, []);

    const removeTicketType = useCallback((id: number) => {
        setTicketTypes(prev => prev.filter(tt => tt.id !== id));
    }, []);

    const getDefaultValues = useCallback((): TicketTypeCreateRequest => ({
        code: '',
        displayName: '',
        priceMultiplier: '1.0',
        category: 'STANDARD',
        minAge: null,
        maxAge: null,
        requiresDocument: false,
        documentType: null,
        active: true
    }), []);

    const getCategoryOptions = useCallback((): Array<{ value: TicketTypeCategory; label: string }> => [
        { value: 'STANDARD', label: 'Standard' },
        { value: 'CHILD', label: 'Child' },
        { value: 'STUDENT', label: 'Student' },
        { value: 'DISABLED', label: 'Disabled' },
        { value: 'MILITARY', label: 'Military' },
        { value: 'SENIOR', label: 'Senior' },
        { value: 'SPECIAL', label: 'Special' }
    ], []);

    const isAgeRestricted = useCallback((age: number, ticketType: { minAge?: number | null; maxAge?: number | null }): boolean => {
        const minAge = ticketType.minAge ?? null;
        const maxAge = ticketType.maxAge ?? null;

        if (minAge !== null && age < minAge) return true;
        if (maxAge !== null && age > maxAge) return true;
        return false;
    }, []);

    return {
        ticketTypes,
        dropdownTypes,
        statusFilter,
        categoryFilter,
        searchQuery,
        loading: apiHook.loading || getByIdHook.loading || getByCodeHook.loading ||
            createHook.loading || updateHook.loading || deleteHook.loading ||
            toggleActiveHook.loading || getActiveForDropdownHook.loading || getDropdownTypesHook.loading,
        getAll,
        getById,
        getByCode,
        create,
        update,
        remove,
        toggleActive,
        getActiveForDropdown,
        getDropdownTypes,
        fetchTicketTypes,
        addTicketType,
        updateTicketType,
        removeTicketType,
        setStatusFilter,
        setCategoryFilter,
        setSearchQuery,
        getDefaultValues,
        getCategoryOptions,
        isAgeRestricted,
        isEmpty: ticketTypes.length === 0,
        isDropdownEmpty: dropdownTypes.length === 0,
    };
};
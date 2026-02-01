import { useState, useCallback, useRef } from 'react';
import { personApi } from '@/api/personApi';
import type { PersonResponse, PersonRequest, QuickCreatePersonRequest, PersonRole } from '@/types/person';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const usePerson = () => {
    const [persons, setPersons] = useState<PersonResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<PersonResponse> | null>(null);
    const [allSelectedPersons, setAllSelectedPersons] = useState<PersonResponse[]>([]);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const abortControllerRef = useRef<AbortController | null>(null);

    const apiHookRef = useRef(useApi<PageResponse<PersonResponse>>());
    const apiHook = apiHookRef.current;

    const getByRoleHook = useApi<PageResponse<PersonResponse>>();
    const createHook = useApi<PersonResponse>();
    const updateHook = useApi<PersonResponse>();
    const deleteHook = useApi<void>();
    const quickCreateHook = useApi<PersonResponse>();
    const getByIdHook = useApi<PersonResponse>();

    const searchPersons = useCallback(async (
        params: SearchParams & { role?: PersonRole } = {}
    ): Promise<PageResponse<PersonResponse>> => {
        return apiHook.callApi(async () => {
            const response = await personApi.public.search(params);
            setPersons(response.content);
            setPagination(response);
            return response;
        }, { showErrorNotification: false });
    }, [apiHook]);

    const getByRole = useCallback(async (
        role: PersonRole,
        params: SearchParams = {}
    ): Promise<PageResponse<PersonResponse>> => {
        return getByRoleHook.callApi(async () => {
            const response = await personApi.public.getByRole(role, params);
            setPersons(response.content);
            setPagination(response);
            return response;
        }, { showErrorNotification: false });
    }, [getByRoleHook]);

    const createPerson = useCallback(async (personData: PersonRequest): Promise<PersonResponse> => {
        return createHook.callApi(async () => {
            return await personApi.admin.create(personData);
        }, { showErrorNotification: false });
    }, [createHook]);

    const updatePerson = useCallback(async (id: number, personData: PersonRequest): Promise<PersonResponse> => {
        return updateHook.callApi(async () => {
            return await personApi.admin.update(id, personData);
        }, { showErrorNotification: false });
    }, [updateHook]);

    const deletePerson = useCallback(async (id: number): Promise<void> => {
        return deleteHook.callApi(async () => {
            await personApi.admin.delete(id);
        }, { showErrorNotification: false });
    }, [deleteHook]);

    const quickCreatePerson = useCallback(async (personData: QuickCreatePersonRequest): Promise<PersonResponse> => {
        return quickCreateHook.callApi(async () => {
            return await personApi.admin.quickCreate(personData);
        }, { showErrorNotification: false });
    }, [quickCreateHook]);

    const getPersonById = useCallback(async (id: number): Promise<PersonResponse> => {
        return getByIdHook.callApi(async () => {
            return await personApi.public.getById(id);
        }, { showErrorNotification: false });
    }, [getByIdHook]);

    const fetchPage = useCallback(async (page: number = 0, size: number = 12): Promise<PageResponse<PersonResponse>> => {
        return apiHook.callApi(async () => {
            const response = await personApi.public.search({ page, size });
            setPersons(response.content);
            setPagination(response);
            return response;
        }, { showErrorNotification: false });
    }, [apiHook]);

    const nextPage = useCallback(async (): Promise<PageResponse<PersonResponse> | null> => {
        if (!pagination || pagination.last) return null;
        return fetchPage(pagination.number + 1, pagination.size);
    }, [pagination, fetchPage]);

    const prevPage = useCallback(async (): Promise<PageResponse<PersonResponse> | null> => {
        if (!pagination || pagination.first) return null;
        return fetchPage(pagination.number - 1, pagination.size);
    }, [pagination, fetchPage]);

    const loadSelectedPersons = useCallback(async (selectedIds: number[]) => {
        if (selectedIds.length === 0) {
            setAllSelectedPersons([]);
            return;
        }

        try {
            const personsData = await Promise.all(
                selectedIds.map(id => personApi.public.getById(id))
            );
            setAllSelectedPersons(personsData);
        } catch {
            setAllSelectedPersons([]);
        }
    }, []);

    const handlePersonSearch = useCallback(async (
        searchQuery: string,
        role: PersonRole
    ) => {
        if (searchQuery.length < 2) {
            setPersons([]);
            setIsDropdownOpen(false);
            return [];
        }

        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }

        abortControllerRef.current = new AbortController();

        try {
            const result = await personApi.public.search({
                query: searchQuery,
                role,
                page: 0,
                size: 10
            });

            if (!abortControllerRef.current.signal.aborted) {
                setPersons(result.content);
                setIsDropdownOpen(true);
                return result.content;
            }
            return [];
        } catch {
            return [];
        }
    }, []);

    const handleAddNewPerson = useCallback(async (
        searchQuery: string,
        role: PersonRole
    ): Promise<number | null> => {
        if (!searchQuery.trim()) return null;

        try {
            const newPerson = await personApi.admin.quickCreate({
                name: searchQuery.trim(),
                role: role
            });

            setAllSelectedPersons(prev => [...prev, newPerson]);
            return newPerson.id;
        } catch {
            return null;
        }
    }, []);

    const resetDropdown = useCallback(() => {
        setPersons([]);
        setIsDropdownOpen(false);
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
            abortControllerRef.current = null;
        }
    }, []);

    return {
        persons,
        pagination,
        allSelectedPersons,
        isDropdownOpen,
        dropdownRef,
        loading: apiHook.loading || getByRoleHook.loading || createHook.loading ||
            updateHook.loading || deleteHook.loading || quickCreateHook.loading ||
            getByIdHook.loading,
        searchPersons,
        getByRole,
        createPerson,
        updatePerson,
        deletePerson,
        quickCreatePerson,
        getPersonById,
        fetchPage,
        nextPage,
        prevPage,
        loadSelectedPersons,
        handlePersonSearch,
        handleAddNewPerson,
        setIsDropdownOpen,
        resetDropdown,
        currentPage: pagination?.number || 0,
        totalPages: pagination?.totalPages || 0,
        totalElements: pagination?.totalElements || 0,
        pageSize: pagination?.size || 0,
        isEmpty: pagination?.empty || false,
        isFirstPage: pagination?.first || true,
        isLastPage: pagination?.last || true,
    };
};
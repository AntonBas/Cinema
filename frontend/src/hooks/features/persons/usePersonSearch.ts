import { useState, useCallback } from 'react';
import { personApi } from '@/api/personApi';
import type { PersonResponse, PersonRole } from '@/types/person';
import type { PageResponse, SearchParams } from '@/types/pagination';

export const usePersonSearch = () => {
    const [persons, setPersons] = useState<PersonResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<PersonResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchPersons = useCallback(
        async (params: SearchParams & { role?: PersonRole } = {}): Promise<PageResponse<PersonResponse>> => {
            setLoading(true);
            setError(null);
            try {
                const response = await personApi.public.search(params);

                if (params.size && params.size > 1) {
                    setPersons(response.content);
                    setPagination(response);
                } else if (!params.size) {
                    setPersons(response.content);
                    setPagination(response);
                } else {
                    setPagination(response);
                }

                return response;
            } catch (err) {
                const message = err instanceof Error ? err.message : 'Failed to search persons';
                setError(message);
                throw err;
            } finally {
                setLoading(false);
            }
        },
        []
    );

    const getByRole = useCallback(
        async (role: PersonRole, params: SearchParams = {}): Promise<PageResponse<PersonResponse>> => {
            setLoading(true);
            setError(null);
            try {
                const response = await personApi.public.getByRole(role, params);

                if (params.size && params.size > 1) {
                    setPersons(response.content);
                    setPagination(response);
                } else if (!params.size) {
                    setPersons(response.content);
                    setPagination(response);
                } else {
                    setPagination(response);
                }

                return response;
            } catch (err) {
                const message = err instanceof Error ? err.message : 'Failed to get persons by role';
                setError(message);
                throw err;
            } finally {
                setLoading(false);
            }
        },
        []
    );

    const clearError = () => {
        setError(null);
    };

    return {
        persons,
        pagination,
        loading,
        error,
        searchPersons,
        getByRole,
        clearError
    };
};
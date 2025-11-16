import { useState, useCallback } from 'react';
import { personApi } from '@/api/personApi';
import type { PersonResponse, PersonRole } from '@/types/person';
import type { PageResponse } from '@/types/pagination';

export const usePersonSearch = () => {
    const [persons, setPersons] = useState<PersonResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<PersonResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchPersons = useCallback(
        async (params: {
            query?: string;
            role?: PersonRole;
            page?: number;
            size?: number;
        } = {}): Promise<PageResponse<PersonResponse>> => {
            setLoading(true);
            setError(null);
            try {
                const response = await personApi.search(params);
                setPersons(response.content);
                setPagination(response);
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

    const clearError = () => {
        setError(null);
    };

    return {
        persons,
        pagination,
        loading,
        error,
        searchPersons,
        clearError
    };
};
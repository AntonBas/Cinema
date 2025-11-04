import { useState, useCallback } from 'react';
import { personApi } from '@/api/personApi';
import type { PersonDto, PersonRole } from '@/types/person';
import type { PageResponse } from '@/types/pagination';

export const usePersonSearch = () => {
    const [persons, setPersons] = useState<PersonDto[]>([]);
    const [pagination, setPagination] = useState<PageResponse<PersonDto> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchPersons = useCallback(
        async (params: {
            query?: string;
            role?: PersonRole;
            page?: number;
            size?: number;
        } = {}): Promise<PageResponse<PersonDto>> => {
            setLoading(true);
            setError(null);
            try {
                const response = await personApi.search(params);
                setPersons(response.content);
                setPagination(response);
                return response;
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Failed to search persons');
                throw err;
            } finally {
                setLoading(false);
            }
        },
        []
    );

    return {
        persons,
        pagination,
        loading,
        error,
        searchPersons,
    };
};

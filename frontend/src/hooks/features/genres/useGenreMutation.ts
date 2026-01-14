import { useState } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest } from '@/types/genre';

export const useGenreMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const createGenre = async (genreData: GenreRequest): Promise<GenreResponse> => {
        setLoading(true);
        setError(null);
        try {
            const genre = await genreApi.admin.create(genreData);
            return genre;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to create genre';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const updateGenre = async (id: number, genreData: GenreRequest): Promise<GenreResponse> => {
        setLoading(true);
        setError(null);
        try {
            const genre = await genreApi.admin.update(id, genreData);
            return genre;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update genre';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const deleteGenre = async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await genreApi.admin.delete(id);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to delete genre';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const clearError = () => {
        setError(null);
    };

    return {
        loading,
        error,
        createGenre,
        updateGenre,
        deleteGenre,
        clearError
    };
};
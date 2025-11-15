import { useState } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreDto, GenreRequest } from '@/types/genre';

export const useGenreMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const createGenre = async (genreData: GenreRequest): Promise<GenreDto> => {
        setLoading(true);
        setError(null);
        try {
            const genre = await genreApi.create(genreData);
            return genre;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to create genre';
            setError(message);
            throw new Error(getUserFriendlyError(message));
        } finally {
            setLoading(false);
        }
    };

    const updateGenre = async (id: number, genreData: GenreRequest): Promise<GenreDto> => {
        setLoading(true);
        setError(null);
        try {
            const genre = await genreApi.update(id, genreData);
            return genre;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update genre';
            setError(message);
            throw new Error(getUserFriendlyError(message));
        } finally {
            setLoading(false);
        }
    };

    const deleteGenre = async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await genreApi.delete(id);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to delete genre';
            setError(message);
            throw new Error(getUserFriendlyError(message));
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

const getUserFriendlyError = (errorMessage: string): string => {
    if (errorMessage.includes('already exists')) {
        return errorMessage;
    }
    if (errorMessage.includes('Failed to create genre')) {
        return 'Unable to create genre. Please try again.';
    }
    if (errorMessage.includes('Failed to update genre')) {
        return 'Unable to update genre. Please try again.';
    }
    if (errorMessage.includes('Failed to delete genre')) {
        return 'Unable to delete genre. Please try again.';
    }
    if (errorMessage.includes('Network Error') || errorMessage.includes('Failed to fetch')) {
        return 'Network connection error. Please check your internet connection.';
    }
    return 'An unexpected error occurred. Please try again.';
};
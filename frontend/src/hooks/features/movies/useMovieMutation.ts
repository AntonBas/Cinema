import { useState } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieCreateRequest, MovieUpdateRequest, MovieDetailResponse } from '@/types/movie';

export const useMovieMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const createMovie = async (movieData: MovieCreateRequest, posterFile: File): Promise<MovieDetailResponse> => {
        setLoading(true);
        setError(null);
        try {
            const movie = await movieApi.createMovie(movieData, posterFile);
            return movie;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to create movie';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const updateMovie = async (id: number, movieData: MovieUpdateRequest, posterFile?: File): Promise<MovieDetailResponse> => {
        setLoading(true);
        setError(null);
        try {
            const movie = await movieApi.updateMovie(id, movieData, posterFile);
            return movie;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update movie';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const deleteMovie = async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await movieApi.deleteMovie(id);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to delete movie';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return {
        loading,
        error,
        createMovie,
        updateMovie,
        deleteMovie
    };
};
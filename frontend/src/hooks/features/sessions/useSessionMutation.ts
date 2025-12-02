import { useState, useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionAdminResponse, SessionRequest } from '@/types/session';

interface UseSessionMutationReturn {
    createSession: (request: SessionRequest) => Promise<SessionAdminResponse>;
    updateSession: (id: number, request: SessionRequest) => Promise<SessionAdminResponse>;
    deleteSession: (id: number) => Promise<void>;
    checkTimeConflict: (
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ) => Promise<boolean>;
    loading: boolean;
    error: string | null;
}

export const useSessionMutation = (): UseSessionMutationReturn => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const createSession = useCallback(async (request: SessionRequest): Promise<SessionAdminResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await sessionApi.createSession(request);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to create session';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const updateSession = useCallback(async (id: number, request: SessionRequest): Promise<SessionAdminResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await sessionApi.updateSession(id, request);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update session';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const deleteSession = useCallback(async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await sessionApi.deleteSession(id);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to delete session';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const checkTimeConflict = useCallback(async (
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ): Promise<boolean> => {
        try {
            return await sessionApi.checkTimeConflict(hallId, startTime, durationMinutes, excludeSessionId);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to check time conflict';
            setError(message);
            throw err;
        }
    }, []);

    return {
        createSession,
        updateSession,
        deleteSession,
        checkTimeConflict,
        loading,
        error
    };
};
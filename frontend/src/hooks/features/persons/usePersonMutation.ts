import { useState } from 'react';
import { personApi } from '@/api/personApi';
import type { PersonResponse, PersonRequest, QuickCreatePersonRequest } from '@/types/person';

export const usePersonMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const createPerson = async (personData: PersonRequest): Promise<PersonResponse> => {
        setLoading(true);
        setError(null);
        try {
            const person = await personApi.create(personData);
            return person;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to create person';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const updatePerson = async (id: number, personData: PersonRequest): Promise<PersonResponse> => {
        setLoading(true);
        setError(null);
        try {
            const person = await personApi.update(id, personData);
            return person;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update person';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const deletePerson = async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await personApi.delete(id);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to delete person';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const quickCreatePerson = async (personData: QuickCreatePersonRequest): Promise<PersonResponse> => {
        setLoading(true);
        setError(null);
        try {
            const person = await personApi.quickCreate(personData);
            return person;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to quick create person';
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
        createPerson,
        updatePerson,
        deletePerson,
        quickCreatePerson,
        clearError
    };
};
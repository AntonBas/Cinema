import { useState } from "react";

export const useApi = <T>() => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const callApi = async (apiCall: () => Promise<T>): Promise<T> => {
        setLoading(true);
        setError(null);
        try {
            return await apiCall();
        } catch (err) {
            const message = err instanceof Error ? err.message : 'API error';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return { loading, error, callApi };
};
import { useState } from "react";
import { useNotification } from './useNotification';

export const useApi = <T>() => {
    const [loading, setLoading] = useState(false);
    const { showNotification } = useNotification();

    const callApi = async (apiCall: () => Promise<T>, options?: {
        showErrorNotification?: boolean;
        successMessage?: string;
    }): Promise<T> => {
        const {
            showErrorNotification = true,
            successMessage
        } = options || {};

        setLoading(true);
        try {
            const result = await apiCall();

            if (successMessage) {
                showNotification(successMessage, 'success');
            }

            return result;
        } catch (err) {
            if (showErrorNotification) {
                const message = err instanceof Error ? err.message : 'Operation failed';
                showNotification(message, 'error');
            }
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return { loading, callApi };
};
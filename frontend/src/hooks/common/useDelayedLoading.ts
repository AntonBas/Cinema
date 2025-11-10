import { useState, useEffect } from 'react';

export const useDelayedLoading = (isLoading: boolean, delay: number = 300) => {
    const [showLoading, setShowLoading] = useState(false);

    useEffect(() => {
        let timer: NodeJS.Timeout;

        if (isLoading) {
            timer = setTimeout(() => {
                setShowLoading(true);
            }, delay);
        } else {
            setShowLoading(false);
        }

        return () => {
            clearTimeout(timer);
        };
    }, [isLoading, delay]);

    return showLoading;
};
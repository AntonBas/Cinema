import { useState, useEffect, useRef } from 'react';

interface UseDelayedLoadingOptions {
    delay?: number;
    minDisplayTime?: number;
}

export const useDelayedLoading = (
    isLoading: boolean,
    options: UseDelayedLoadingOptions = {}
) => {
    const { delay = 150, minDisplayTime = 300 } = options;
    const [showLoading, setShowLoading] = useState(false);

    const showTimerRef = useRef<number | null>(null);
    const hideTimerRef = useRef<number | null>(null);
    const startTimeRef = useRef<number | null>(null);

    useEffect(() => {
        if (showTimerRef.current) clearTimeout(showTimerRef.current);
        if (hideTimerRef.current) clearTimeout(hideTimerRef.current);

        if (isLoading) {
            showTimerRef.current = setTimeout(() => {
                setShowLoading(true);
                startTimeRef.current = Date.now();
            }, delay);
        } else {
            if (showLoading && startTimeRef.current) {
                const elapsed = Date.now() - startTimeRef.current;
                const remaining = minDisplayTime - elapsed;

                if (remaining > 0) {
                    hideTimerRef.current = setTimeout(() => {
                        setShowLoading(false);
                        startTimeRef.current = null;
                    }, remaining);
                } else {
                    setShowLoading(false);
                    startTimeRef.current = null;
                }
            } else {
                setShowLoading(false);
                startTimeRef.current = null;
            }
        }

        return () => {
            if (showTimerRef.current) clearTimeout(showTimerRef.current);
            if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
        };
    }, [isLoading, delay, minDisplayTime, showLoading]);

    return showLoading;
};
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
    const startTimeRef = useRef<number | null>(null);
    const timerRef = useRef<NodeJS.Timeout | null>(null);
    const hideTimerRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        if (isLoading) {
            if (hideTimerRef.current) {
                clearTimeout(hideTimerRef.current);
                hideTimerRef.current = null;
            }

            timerRef.current = setTimeout(() => {
                setShowLoading(true);
                startTimeRef.current = Date.now();
            }, delay);
        } else {
            if (timerRef.current) {
                clearTimeout(timerRef.current);
                timerRef.current = null;
            }

            if (showLoading && startTimeRef.current) {
                const elapsed = Date.now() - startTimeRef.current;
                const remaining = minDisplayTime - elapsed;

                if (remaining > 0) {
                    hideTimerRef.current = setTimeout(() => {
                        setShowLoading(false);
                        startTimeRef.current = null;
                        hideTimerRef.current = null;
                    }, remaining);
                } else {
                    setShowLoading(false);
                    startTimeRef.current = null;
                }
            } else {
                setShowLoading(false);
            }
        }

        return () => {
            if (timerRef.current) clearTimeout(timerRef.current);
            if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
        };
    }, [isLoading, delay, minDisplayTime, showLoading]);

    return showLoading;
};
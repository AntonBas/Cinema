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
    const showTimerRef = useRef<NodeJS.Timeout | null>(null);
    const hideTimerRef = useRef<NodeJS.Timeout | null>(null);
    const hasLoadedRef = useRef(false);
    const prevIsLoadingRef = useRef(isLoading);

    useEffect(() => {
        if (prevIsLoadingRef.current === isLoading) {
            return;
        }

        prevIsLoadingRef.current = isLoading;

        if (showTimerRef.current) {
            clearTimeout(showTimerRef.current);
            showTimerRef.current = null;
        }
        if (hideTimerRef.current) {
            clearTimeout(hideTimerRef.current);
            hideTimerRef.current = null;
        }

        if (isLoading) {
            if (!hasLoadedRef.current) {
                setShowLoading(true);
                startTimeRef.current = Date.now();
            } else {
                showTimerRef.current = setTimeout(() => {
                    setShowLoading(true);
                    startTimeRef.current = Date.now();
                    showTimerRef.current = null;
                }, delay);
            }
        } else {
            if (!showLoading) {
                return;
            }

            const elapsed = startTimeRef.current ? Date.now() - startTimeRef.current : 0;
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

            if (!hasLoadedRef.current) {
                hasLoadedRef.current = true;
            }
        }

        return () => {
            if (showTimerRef.current) {
                clearTimeout(showTimerRef.current);
            }
            if (hideTimerRef.current) {
                clearTimeout(hideTimerRef.current);
            }
        };
    }, [isLoading, delay, minDisplayTime, showLoading]);

    return showLoading;
};
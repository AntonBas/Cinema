import { useState, useEffect, useRef } from "react";

interface UseDelayedLoadingOptions {
  delay?: number;
  minDisplayTime?: number;
}

export const useDelayedLoading = (
  isLoading: boolean,
  options: UseDelayedLoadingOptions = {},
) => {
  const { delay = 150, minDisplayTime = 300 } = options;
  const [showLoading, setShowLoading] = useState(false);

  const showTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const hideTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const startTimeRef = useRef<number | null>(null);
  const isShownRef = useRef(false);

  useEffect(() => {
    if (showTimerRef.current) clearTimeout(showTimerRef.current);
    if (hideTimerRef.current) clearTimeout(hideTimerRef.current);

    if (isLoading) {
      showTimerRef.current = setTimeout(() => {
        isShownRef.current = true;
        startTimeRef.current = Date.now();
        setShowLoading(true);
      }, delay);
    } else if (isShownRef.current && startTimeRef.current) {
      const elapsed = Date.now() - startTimeRef.current;
      const remaining = minDisplayTime - elapsed;

      if (remaining > 0) {
        hideTimerRef.current = setTimeout(() => {
          isShownRef.current = false;
          startTimeRef.current = null;
          setShowLoading(false);
        }, remaining);
      } else {
        isShownRef.current = false;
        startTimeRef.current = null;
        setShowLoading(false);
      }
    } else {
      isShownRef.current = false;
      startTimeRef.current = null;
      setShowLoading(false);
    }

    return () => {
      if (showTimerRef.current) clearTimeout(showTimerRef.current);
      if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
    };
  }, [isLoading, delay, minDisplayTime]);

  return showLoading;
};

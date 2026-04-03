import { useState, useCallback, useEffect, useRef } from 'react';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

interface NotificationItem {
  id: string;
  message: string;
  type: NotificationType;
  isVisible: boolean;
  duration?: number;
}

export const useNotification = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const timeoutsRef = useRef<Map<string, number>>(new Map());

  const removeNotification = useCallback((id: string) => {
    const timeout = timeoutsRef.current.get(id);
    if (timeout) {
      clearTimeout(timeout);
      timeoutsRef.current.delete(id);
    }

    setNotifications(prev => prev.filter(notif => notif.id !== id));
  }, []);

  const showNotification = useCallback((
    message: string,
    type: NotificationType = 'info',
    duration: number = 5000
  ) => {
    const id = Math.random().toString(36).substring(2, 11);
    const newNotification: NotificationItem = {
      id,
      message,
      type,
      isVisible: true,
      duration
    };

    setNotifications(prev => [...prev, newNotification]);

    if (duration > 0) {
      const timeout = setTimeout(() => {
        removeNotification(id);
      }, duration);
      timeoutsRef.current.set(id, timeout);
    }

    return id;
  }, [removeNotification]);

  const hideNotification = useCallback((id: string) => {
    setNotifications(prev =>
      prev.map(notif =>
        notif.id === id ? { ...notif, isVisible: false } : notif
      )
    );

    setTimeout(() => {
      removeNotification(id);
    }, 300);
  }, [removeNotification]);

  const hideAllNotifications = useCallback(() => {
    timeoutsRef.current.forEach(timeout => clearTimeout(timeout));
    timeoutsRef.current.clear();

    setNotifications(prev =>
      prev.map(notif => ({ ...notif, isVisible: false }))
    );

    setTimeout(() => {
      setNotifications([]);
    }, 300);
  }, []);

  useEffect(() => {
    return () => {
      timeoutsRef.current.forEach(timeout => clearTimeout(timeout));
      timeoutsRef.current.clear();
    };
  }, []);

  return {
    notifications,
    showNotification,
    hideNotification,
    hideAllNotifications
  };
};
import { useState, useCallback } from 'react';

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

  const showNotification = useCallback((
    message: string,
    type: NotificationType = 'info',
    duration?: number
  ) => {
    const id = Math.random().toString(36).substr(2, 9);
    const newNotification: NotificationItem = {
      id,
      message,
      type,
      isVisible: true,
      duration
    };

    setNotifications(prev => [...prev, newNotification]);
    return id;
  }, []);

  const hideNotification = useCallback((id: string) => {
    setNotifications(prev =>
      prev.map(notif =>
        notif.id === id ? { ...notif, isVisible: false } : notif
      )
    );

    setTimeout(() => {
      setNotifications(prev => prev.filter(notif => notif.id !== id));
    }, 300);
  }, []);

  const hideAllNotifications = useCallback(() => {
    setNotifications(prev =>
      prev.map(notif => ({ ...notif, isVisible: false }))
    );
    setTimeout(() => {
      setNotifications([]);
    }, 300);
  }, []);

  return {
    notifications,
    showNotification,
    hideNotification,
    hideAllNotifications
  };
};
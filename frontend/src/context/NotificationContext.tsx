import React, { createContext, useContext, useState, useCallback, useRef, useEffect } from 'react';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

interface NotificationItem {
    id: string;
    message: string;
    type: NotificationType;
    isVisible: boolean;
    duration?: number;
}

interface NotificationContextType {
    notifications: NotificationItem[];
    showNotification: (message: string, type?: NotificationType, duration?: number) => string;
    hideNotification: (id: string) => void;
}

const NotificationContext = createContext<NotificationContextType | null>(null);

export const useNotification = () => {
    const context = useContext(NotificationContext);
    if (!context) throw new Error('useNotification must be used within NotificationProvider');
    return context;
};

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [notifications, setNotifications] = useState<NotificationItem[]>([]);
    const timeoutsRef = useRef<Map<string, number>>(new Map());

    const removeNotification = useCallback((id: string) => {
        const timeout = timeoutsRef.current.get(id);
        if (timeout) {
            clearTimeout(timeout);
            timeoutsRef.current.delete(id);
        }
        setNotifications(prev => prev.filter(n => n.id !== id));
    }, []);

    const showNotification = useCallback((
        message: string,
        type: NotificationType = 'info',
        duration: number = 5000
    ) => {
        const id = Math.random().toString(36).substring(2, 11);

        setNotifications(prev => [...prev, { id, message, type, isVisible: true, duration }]);

        if (duration > 0) {
            const timeout = window.setTimeout(() => removeNotification(id), duration);
            timeoutsRef.current.set(id, timeout);
        }

        return id;
    }, [removeNotification]);

    const hideNotification = useCallback((id: string) => {
        setNotifications(prev => prev.map(n => n.id === id ? { ...n, isVisible: false } : n));
        setTimeout(() => removeNotification(id), 300);
    }, [removeNotification]);

    useEffect(() => {
        return () => timeoutsRef.current.forEach(timeout => clearTimeout(timeout));
    }, []);

    return (
        <NotificationContext.Provider value={{ notifications, showNotification, hideNotification }}>
            {children}
        </NotificationContext.Provider>
    );
};
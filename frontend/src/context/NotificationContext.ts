import { createContext, useContext } from 'react';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface NotificationItem {
    id: string;
    message: string;
    type: NotificationType;
    isVisible: boolean;
    duration?: number;
}

export interface NotificationContextType {
    notifications: NotificationItem[];
    showNotification: (message: string, type?: NotificationType, duration?: number) => string;
    hideNotification: (id: string) => void;
}

export const NotificationContext = createContext<NotificationContextType | null>(null);

export const useNotification = () => {
    const context = useContext(NotificationContext);
    if (!context) throw new Error('useNotification must be used within NotificationProvider');
    return context;
};

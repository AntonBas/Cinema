import React from 'react';
import { useNotification } from '@/context/NotificationContext';
import { Notification } from '@/components/ui/Notification/Notification';

export const NotificationContainer: React.FC = () => {
    const { notifications, hideNotification } = useNotification();

    return (
        <>
            {notifications.map((notification, index) => (
                <Notification
                    key={notification.id}
                    id={notification.id}
                    message={notification.message}
                    type={notification.type}
                    isVisible={notification.isVisible}
                    onClose={hideNotification}
                    duration={notification.duration}
                    position={index}
                />
            ))}
        </>
    );
};
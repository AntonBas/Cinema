import React, { useEffect } from "react";
import { createPortal } from "react-dom";
import {
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Info,
  Lightbulb,
} from "lucide-react";
import styles from "./Notification.module.css";
import clsx from "clsx";

export type NotificationType = "success" | "error" | "warning" | "info";

export interface NotificationProps {
  id: string;
  message: string;
  type: NotificationType;
  isVisible: boolean;
  onClose: (id: string) => void;
  duration?: number;
  position?: number;
  isStatic?: boolean;
}

export const Notification: React.FC<NotificationProps> = ({
  id,
  message,
  type,
  isVisible,
  onClose,
  duration = 3000,
  position = 0,
  isStatic = false,
}) => {
  useEffect(() => {
    if (isVisible && !isStatic) {
      const timer = setTimeout(() => onClose(id), duration);
      return () => clearTimeout(timer);
    }
  }, [isVisible, duration, onClose, id, isStatic]);

  const getIcon = () => {
    switch (type) {
      case "success":
        return <CheckCircle2 size={20} />;
      case "error":
        return <XCircle size={20} />;
      case "warning":
        return <AlertTriangle size={20} />;
      case "info":
        return <Info size={20} />;
      default:
        return <Lightbulb size={20} />;
    }
  };

  const notificationContent = (
    <div
      className={clsx(
        styles.notification,
        styles[type],
        isVisible && styles.show,
        isStatic && styles.static,
      )}
      style={{
        transform: isStatic ? "none" : `translateY(${position * 100}%)`,
        zIndex: isStatic ? 1 : 10000 + position,
        position: isStatic ? "static" : "fixed",
      }}
    >
      <div className={styles.content}>
        <span className={styles.icon}>{getIcon()}</span>
        <span className={styles.message}>{message}</span>
        <button className={styles.close} onClick={() => onClose(id)}>
          ×
        </button>
      </div>
      {!isStatic && (
        <div className={styles.progress}>
          <div
            className={styles.progressBar}
            style={{ animationDuration: `${duration}ms` }}
          ></div>
        </div>
      )}
    </div>
  );

  return isStatic
    ? notificationContent
    : createPortal(notificationContent, document.body);
};

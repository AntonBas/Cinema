import React, { useEffect, useRef, useState } from 'react';
import styles from './Tooltip.module.css';

export interface TooltipProps {
    content: string;
    children: React.ReactNode;
    position?: 'top' | 'bottom' | 'left' | 'right';
    align?: 'center' | 'end';
    className?: string;
    id?: string;
}

const SHOW_DELAY_MS = 200;

export const Tooltip: React.FC<TooltipProps> = ({
    content,
    children,
    position = 'top',
    align = 'center',
    className = '',
    id
}) => {
    const [isVisible, setIsVisible] = useState(false);
    const showTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const tooltipId = id || `tooltip-${Math.random().toString(36).substr(2, 9)}`;

    useEffect(() => {
        return () => {
            if (showTimeoutRef.current) {
                clearTimeout(showTimeoutRef.current);
            }
        };
    }, []);

    const scheduleShow = () => {
        if (showTimeoutRef.current) {
            clearTimeout(showTimeoutRef.current);
        }
        showTimeoutRef.current = setTimeout(() => setIsVisible(true), SHOW_DELAY_MS);
    };

    const hide = () => {
        if (showTimeoutRef.current) {
            clearTimeout(showTimeoutRef.current);
            showTimeoutRef.current = null;
        }
        setIsVisible(false);
    };

    return (
        <div className={`${styles.tooltipContainer} ${className}`}>
            <div
                onMouseEnter={scheduleShow}
                onMouseLeave={hide}
                onFocus={() => setIsVisible(true)}
                onBlur={hide}
                className={styles.tooltipTrigger}
                aria-describedby={isVisible ? tooltipId : undefined}
                tabIndex={0}
            >
                {children}
            </div>
            <div
                id={tooltipId}
                role="tooltip"
                className={`${styles.tooltip} ${styles[position]} ${align === 'end' ? styles.alignEnd : ''} ${isVisible ? styles.visible : ''}`}
                aria-hidden={!isVisible}
            >
                {content}
                <div className={styles.tooltipArrow} aria-hidden="true" />
            </div>
        </div>
    );
};

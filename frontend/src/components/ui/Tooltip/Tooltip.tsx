import React, { useState } from 'react';
import styles from './Tooltip.module.css';

export interface TooltipProps {
    content: string;
    children: React.ReactNode;
    position?: 'top' | 'bottom' | 'left' | 'right';
    className?: string;
    id?: string;
}

export const Tooltip: React.FC<TooltipProps> = ({
    content,
    children,
    position = 'top',
    className = '',
    id
}) => {
    const [isVisible, setIsVisible] = useState(false);
    const tooltipId = id || `tooltip-${Math.random().toString(36).substr(2, 9)}`;

    return (
        <div className={`${styles.tooltipContainer} ${className}`}>
            <div
                onMouseEnter={() => setIsVisible(true)}
                onMouseLeave={() => setIsVisible(false)}
                onFocus={() => setIsVisible(true)}
                onBlur={() => setIsVisible(false)}
                className={styles.tooltipTrigger}
                aria-describedby={isVisible ? tooltipId : undefined}
                tabIndex={0}
            >
                {children}
            </div>
            {isVisible && (
                <div
                    id={tooltipId}
                    role="tooltip"
                    className={`${styles.tooltip} ${styles[position]}`}
                >
                    {content}
                    <div className={styles.tooltipArrow} aria-hidden="true" />
                </div>
            )}
        </div>
    );
};
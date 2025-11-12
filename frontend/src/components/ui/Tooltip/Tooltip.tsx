import React, { useState } from 'react';
import styles from './Tooltip.module.css';

export interface TooltipProps {
    content: string;
    children: React.ReactNode;
    position?: 'top' | 'bottom' | 'left' | 'right';
    className?: string;
}

export const Tooltip: React.FC<TooltipProps> = ({
    content,
    children,
    position = 'top',
    className = ''
}) => {
    const [isVisible, setIsVisible] = useState(false);

    return (
        <div className={`${styles.tooltipContainer} ${className}`}>
            <div
                onMouseEnter={() => setIsVisible(true)}
                onMouseLeave={() => setIsVisible(false)}
                className={styles.tooltipTrigger}
            >
                {children}
            </div>
            {isVisible && (
                <div className={`${styles.tooltip} ${styles[position]}`}>
                    {content}
                    <div className={styles.tooltipArrow} />
                </div>
            )}
        </div>
    );
};
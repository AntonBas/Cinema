import React from "react";
import styles from './LoadingSpinner.module.css';

interface LoadingSpinnerProps {
    text?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ text = "Loading..." }) => {
    return (
        <div className={styles.loaderContainer}>
            <span className={styles.loaderWrapper} data-text={text}></span>
        </div>
    );
};

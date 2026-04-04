import React from 'react';
import { Check } from 'lucide-react';
import styles from './ProgressStepper.module.css';
import clsx from 'clsx';

interface Step {
    id: number;
    title: string;
}

interface ProgressStepperProps {
    steps: Step[];
    currentStep: number;
    className?: string;
    showConnectors?: boolean;
    showStepNumbers?: boolean;
}

export const ProgressStepper: React.FC<ProgressStepperProps> = ({
    steps,
    currentStep,
    className = '',
    showConnectors = true,
    showStepNumbers = true
}) => {
    return (
        <div className={clsx(styles.stepper, className)}>
            <div className={styles.stepperContainer}>
                {steps.map((step, index) => {
                    const isCompleted = step.id < currentStep;
                    const isActive = step.id === currentStep;
                    const isUpcoming = step.id > currentStep;

                    return (
                        <React.Fragment key={step.id}>
                            <div className={styles.stepWrapper}>
                                <div className={styles.stepContainer}>
                                    <div className={clsx(
                                        styles.stepCircle,
                                        isCompleted && styles.completed,
                                        isActive && styles.active,
                                        isUpcoming && styles.upcoming
                                    )}>
                                        {isCompleted ? (
                                            <Check size={16} className={styles.checkIcon} />
                                        ) : (
                                            showStepNumbers && (
                                                <span className={styles.stepNumber}>{step.id}</span>
                                            )
                                        )}
                                    </div>

                                    <div className={styles.stepContent}>
                                        <div className={clsx(
                                            styles.stepTitle,
                                            isCompleted && styles.completed,
                                            isActive && styles.active,
                                            isUpcoming && styles.upcoming
                                        )}>
                                            {step.title}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {showConnectors && index < steps.length - 1 && (
                                <div className={clsx(
                                    styles.connector,
                                    isCompleted && styles.completed,
                                    isActive && styles.active,
                                    isUpcoming && styles.upcoming
                                )} />
                            )}
                        </React.Fragment>
                    );
                })}
            </div>
        </div>
    );
};
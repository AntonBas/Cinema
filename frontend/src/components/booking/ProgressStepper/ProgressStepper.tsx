import React from 'react';
import { Check } from 'lucide-react';
import { Tooltip } from '@/components/ui/Tooltip/Tooltip';
import styles from './ProgressStepper.module.css';
import clsx from 'clsx';

interface Step {
    id: number;
    title: string;
    description?: string;
    path?: string;
    isClickable?: boolean;
}

interface ProgressStepperProps {
    steps: Step[];
    currentStep: number;
    className?: string;
    onStepClick?: (step: Step) => void;
    showConnectors?: boolean;
    showStepNumbers?: boolean;
}

export const ProgressStepper: React.FC<ProgressStepperProps> = ({
    steps,
    currentStep,
    className = '',
    onStepClick,
    showConnectors = true,
    showStepNumbers = true
}) => {
    const handleStepClick = (step: Step) => {
        if (onStepClick && step.isClickable && step.id <= currentStep) {
            onStepClick(step);
        }
    };

    return (
        <div className={clsx(styles.stepper, className)}>
            <div className={styles.stepperContainer}>
                {steps.map((step, index) => {
                    const isCompleted = step.id < currentStep;
                    const isActive = step.id === currentStep;
                    const isUpcoming = step.id > currentStep;
                    const isClickable = step.isClickable && step.id <= currentStep;

                    return (
                        <React.Fragment key={step.id}>
                            <div className={styles.stepWrapper}>
                                <div
                                    className={clsx(
                                        styles.stepContainer,
                                        isClickable && styles.clickable
                                    )}
                                    onClick={() => handleStepClick(step)}
                                >
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
                                        {step.description && (
                                            <div className={styles.stepDescription}>
                                                {step.description}
                                            </div>
                                        )}
                                    </div>
                                </div>

                                {isClickable && (
                                    <Tooltip
                                        content={`Go to: ${step.title}`}
                                        position="bottom"
                                        className={styles.tooltip}
                                    >
                                        <span className={styles.tooltipTrigger} />
                                    </Tooltip>
                                )}
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
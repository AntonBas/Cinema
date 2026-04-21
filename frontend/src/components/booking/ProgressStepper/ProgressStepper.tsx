import React, { useState } from "react";
import styles from "./ProgressStepper.module.css";
import clsx from "clsx";

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
  className = "",
  showConnectors = true,
  showStepNumbers = true,
}) => {
  const [showAllSteps, setShowAllSteps] = useState(false);
  const currentStepData = steps.find((step) => step.id === currentStep);
  const completedSteps = steps.filter((step) => step.id < currentStep).length;

  return (
    <div className={clsx(styles.stepper, className)}>
      <div className={styles.stepperContainer}>
        {steps.map((step, index) => {
          const isCompleted = step.id < currentStep;
          const isActive = step.id === currentStep;
          const isUpcoming = step.id > currentStep;

          return (
            <div
              key={step.id}
              className={clsx(styles.stepWrapper, styles.desktopOnly)}
            >
              <div className={styles.stepContainer}>
                <div
                  className={clsx(
                    styles.stepCircle,
                    isCompleted && styles.completed,
                    isActive && styles.active,
                    isUpcoming && styles.upcoming,
                  )}
                >
                  {isCompleted ? (
                    <span className={styles.checkIcon}>✓</span>
                  ) : (
                    showStepNumbers && (
                      <span className={styles.stepNumber}>{step.id}</span>
                    )
                  )}
                </div>

                <div className={styles.stepContent}>
                  <div
                    className={clsx(
                      styles.stepTitle,
                      isCompleted && styles.completedTitle,
                      isActive && styles.activeTitle,
                      isUpcoming && styles.upcomingTitle,
                    )}
                  >
                    {step.title}
                  </div>
                </div>
              </div>

              {showConnectors && index < steps.length - 1 && (
                <div
                  className={clsx(
                    styles.connector,
                    isCompleted && styles.completedConnector,
                  )}
                />
              )}
            </div>
          );
        })}
      </div>

      <div className={styles.mobileStepper}>
        <button
          className={styles.mobileStepperHeader}
          onClick={() => setShowAllSteps(!showAllSteps)}
        >
          <div className={styles.mobileCurrentStep}>
            <div className={clsx(styles.stepCircle, styles.active)}>
              {currentStep}
            </div>
            <div className={styles.mobileStepInfo}>
              <span className={styles.mobileStepLabel}>
                Step {currentStep} of {steps.length}
              </span>
              <span className={styles.mobileStepTitle}>
                {currentStepData?.title}
              </span>
            </div>
          </div>
          <span
            className={clsx(
              styles.mobileChevron,
              showAllSteps && styles.mobileChevronOpen,
            )}
          >
            ▼
          </span>
        </button>

        {showAllSteps && (
          <div className={styles.mobileStepsList}>
            {steps.map((step) => {
              const isCompleted = step.id < currentStep;
              const isActive = step.id === currentStep;

              return (
                <div
                  key={step.id}
                  className={clsx(
                    styles.mobileStepItem,
                    isActive && styles.mobileStepItemActive,
                    isCompleted && styles.mobileStepItemCompleted,
                  )}
                >
                  <div
                    className={clsx(
                      styles.stepCircle,
                      styles.mobileStepCircle,
                      isCompleted && styles.completed,
                      isActive && styles.active,
                    )}
                  >
                    {isCompleted ? "✓" : step.id}
                  </div>
                  <span className={styles.mobileStepItemTitle}>
                    {step.title}
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

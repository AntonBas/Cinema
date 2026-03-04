import { useState } from 'react';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import BonusRules from './BonusRules/BonusRules';
import BonusTransactions from './BonusTransactions/BonusTransactions';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SectionBonus.module.css';

const SectionBonus = () => {
    const [showRules, setShowRules] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [loading] = useState(false);
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    if (error) {
        return (
            <div className={styles.section}>
                <div className={styles.error}>
                    <h3>Error loading bonus system</h3>
                    <p>{error}</p>
                    <button
                        className={styles.retryButton}
                        onClick={() => setError(null)}
                    >
                        Try Again
                    </button>
                </div>
            </div>
        );
    }

    if (showDelayedLoading) {
        return (
            <div className={styles.section}>
                <div className={styles.loading}>
                    <LoadingSpinner text="Loading bonus system..." />
                </div>
            </div>
        );
    }

    return (
        <div className={styles.section}>
            <div className={styles.header}>
                <h1 className={styles.title}>Bonus System</h1>
                <div className={styles.buttons}>
                    <button
                        className={`${styles.button} ${showRules ? styles.active : ''}`}
                        onClick={() => setShowRules(true)}
                    >
                        Bonus Rules
                    </button>
                    <button
                        className={`${styles.button} ${!showRules ? styles.active : ''}`}
                        onClick={() => setShowRules(false)}
                    >
                        Transactions History
                    </button>
                </div>
            </div>

            <div className={styles.content}>
                {showRules ? <BonusRules /> : <BonusTransactions />}
            </div>
        </div>
    );
};

export default SectionBonus;
import { useState } from 'react';
import BonusRules from './BonusRules/BonusRules';
import BonusTransactions from './BonusTransactions/BonusTransactions';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SectionBonus.module.css';

const SectionBonus = () => {
    const [activeTab, setActiveTab] = useState<'rules' | 'transactions'>('rules');
    const [loading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleTabChange = (tab: 'rules' | 'transactions') => {
        setActiveTab(tab);
        setError(null);
    };

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

    return (
        <div className={styles.section}>
            <div className={styles.header}>
                <h1 className={styles.title}>Bonus System</h1>
                <div className={styles.tabs}>
                    <button
                        className={`${styles.tab} ${activeTab === 'rules' ? styles.active : ''}`}
                        onClick={() => handleTabChange('rules')}
                    >
                        Bonus Rules
                    </button>
                    <button
                        className={`${styles.tab} ${activeTab === 'transactions' ? styles.active : ''}`}
                        onClick={() => handleTabChange('transactions')}
                    >
                        Transactions History
                    </button>
                </div>
            </div>

            <div className={styles.content}>
                {loading ? (
                    <div className={styles.loading}>
                        <LoadingSpinner text="Loading..." />
                    </div>
                ) : (
                    <>
                        {activeTab === 'rules' ? (
                            <BonusRules />
                        ) : (
                            <BonusTransactions />
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default SectionBonus;
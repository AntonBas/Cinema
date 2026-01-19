import React from 'react';
import type { BonusBalanceResponse } from '@/types/bonus';
import styles from './BonusBalanceCard.module.css';

interface BonusBalanceCardProps {
    balance: BonusBalanceResponse | null;
    loading: boolean;
}

export const BonusBalanceCard: React.FC<BonusBalanceCardProps> = ({ balance, loading }) => {
    if (loading) {
        return (
            <div className={styles.balanceCard}>
                <div className={styles.loading}>
                    Loading balance...
                </div>
            </div>
        );
    }

    if (!balance) {
        return (
            <div className={styles.balanceCard}>
                <div className={styles.noData}>
                    No balance data available
                </div>
            </div>
        );
    }

    return (
        <div className={styles.balanceCard}>
            <h2 className={styles.title}>Bonus Balance</h2>

            <div className={styles.mainBalance}>
                <div className={styles.points}>{balance.pointsBalance}</div>
                <div className={styles.pointsLabel}>points</div>
            </div>

            <div className={styles.valueInfo}>
                <div className={styles.valueItem}>
                    <div className={styles.valueLabel}>Point Value</div>
                    <div className={styles.valueAmount}>{balance.pointValue} UAH</div>
                </div>
                <div className={styles.valueItem}>
                    <div className={styles.valueLabel}>Total Value</div>
                    <div className={styles.valueAmount}>{balance.balanceValue} UAH</div>
                </div>
            </div>

            <div className={styles.usageLimits}>
                <h3 className={styles.limitsTitle}>Usage Limits</h3>
                <div className={styles.limitsGrid}>
                    <div className={styles.limitItem}>
                        <div className={styles.limitLabel}>Min per use</div>
                        <div className={styles.limitValue}>{balance.minUsablePoints} points</div>
                    </div>
                    <div className={styles.limitItem}>
                        <div className={styles.limitLabel}>Max per use</div>
                        <div className={styles.limitValue}>{balance.maxUsablePoints} points</div>
                    </div>
                    <div className={styles.limitItem}>
                        <div className={styles.limitLabel}>Min value</div>
                        <div className={styles.limitValue}>{balance.minRedemptionValue} UAH</div>
                    </div>
                    <div className={styles.limitItem}>
                        <div className={styles.limitLabel}>Max value</div>
                        <div className={styles.limitValue}>{balance.maxRedemptionValue} UAH</div>
                    </div>
                </div>
            </div>
        </div>
    );
};
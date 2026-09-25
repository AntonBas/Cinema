import React from "react";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import type { BonusBalanceResponse } from "@/types/bonus";
import { formatPrice } from "@/utils/formatters";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import styles from "./BonusBalanceCard.module.css";

interface BonusBalanceCardProps {
  balance: BonusBalanceResponse | null;
  loading: boolean;
}

export const BonusBalanceCard: React.FC<BonusBalanceCardProps> = ({
  balance,
  loading,
}) => {
  const showLoading = useDelayedLoading(loading);

  if (showLoading) {
    return (
      <div className={styles.balanceCard}>
        <LoadingSpinner text="Loading balance..." />
      </div>
    );
  }

  if (loading && !balance) {
    return null;
  }

  if (!balance) {
    return <EmptyState title="No Balance Data Available" />;
  }

  return (
    <div className={styles.balanceCard}>
      <div className={styles.header}>
        <h2 className={styles.title}>Bonus Card</h2>
      </div>

      <div className={styles.mainBalance}>
        <div className={styles.points}>{balance.pointsBalance}</div>
        <div className={styles.pointsLabel}>points</div>
      </div>

      <div className={styles.valueInfo}>
        <div className={styles.valueItem}>
          <div className={styles.valueLabel}>Point Value</div>
          <div className={styles.valueAmount}>
            {formatPrice(balance.pointValue)}
          </div>
        </div>
        <div className={styles.valueItem}>
          <div className={styles.valueLabel}>Total Value</div>
          <div className={styles.valueAmount}>
            {formatPrice(balance.balanceValue)}
          </div>
        </div>
      </div>

      <div className={styles.usageLimits}>
        <h3 className={styles.limitsTitle}>Usage Limits</h3>
        <div className={styles.limitsGrid}>
          <div className={styles.limitItem}>
            <div className={styles.limitLabel}>Min per use</div>
            <div className={styles.limitValue}>
              {balance.minUsablePoints} points
            </div>
          </div>
          <div className={styles.limitItem}>
            <div className={styles.limitLabel}>Max per use</div>
            <div className={styles.limitValue}>
              {balance.maxUsablePoints} points
            </div>
          </div>
          <div className={styles.limitItem}>
            <div className={styles.limitLabel}>Min value</div>
            <div className={styles.limitValue}>
              {formatPrice(balance.minRedemptionValue)}
            </div>
          </div>
          <div className={styles.limitItem}>
            <div className={styles.limitLabel}>Max value</div>
            <div className={styles.limitValue}>
              {formatPrice(balance.maxRedemptionValue)}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

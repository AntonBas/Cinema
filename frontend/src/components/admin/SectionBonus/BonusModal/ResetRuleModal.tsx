import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Notification } from '@/components/ui/Notification';
import { useAdminBonus } from '@/hooks/features/bonus/useAdminBonus';
import type { BonusTransactionType } from '@/types/bonus';
import { BonusTransactionTypeDisplay } from '@/types/bonus';
import styles from './BonusModal.module.css';

interface ResetRuleModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    ruleType: BonusTransactionType;
}

const ResetRuleModal: React.FC<ResetRuleModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
    ruleType
}) => {
    const { resetRule, loading, error } = useAdminBonus();
    const [showNotification, setShowNotification] = useState(false);

    const handleReset = async () => {
        try {
            await resetRule(ruleType);

            setShowNotification(true);
            setTimeout(() => {
                setShowNotification(false);
                onSuccess();
            }, 1500);
        } catch (err) {
            console.error('Failed to reset rule:', err);
        }
    };

    const formatRuleType = (type: BonusTransactionType): string => {
        return BonusTransactionTypeDisplay[type] || type;
    };

    return (
        <>
            <Modal
                isOpen={isOpen}
                onClose={onClose}
                title={`Reset Bonus Rule: ${formatRuleType(ruleType)}`}
                size="small"
            >
                <div className={styles.resetContent}>
                    <div className={styles.resetMessage}>
                        <p>Are you sure you want to reset this bonus rule to its default values?</p>
                        <p><strong>{formatRuleType(ruleType)} ({ruleType})</strong></p>
                    </div>

                    <div className={styles.resetWarning}>
                        ⚠️ This action cannot be undone. All custom settings for this rule will be lost.
                    </div>

                    {error && (
                        <div style={{ color: 'var(--error)', fontSize: '14px', marginBottom: '16px' }}>
                            {error}
                        </div>
                    )}

                    <div className={styles.resetActions}>
                        <Button
                            variant="cancel"
                            onClick={onClose}
                            disabled={loading}
                        >
                            Cancel
                        </Button>
                        <Button
                            variant="error"
                            onClick={handleReset}
                            loading={loading}
                            disabled={loading}
                        >
                            Reset to Defaults
                        </Button>
                    </div>
                </div>
            </Modal>

            {showNotification && (
                <Notification
                    id="reset-rule-success"
                    message="Bonus rule reset successfully!"
                    type="success"
                    isVisible={showNotification}
                    onClose={() => setShowNotification(false)}
                    duration={2000}
                    isStatic={false}
                />
            )}
        </>
    );
};

export default ResetRuleModal;
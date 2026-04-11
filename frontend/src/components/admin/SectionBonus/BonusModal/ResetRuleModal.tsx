import React from 'react';
import { Modal } from '@/components/ui/Modal/Modal';
import { Button } from '@/components/ui/Button/Button';
import { useBonus } from '@/hooks/features/bonus/useBonus';
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
    const { resetRule, loading } = useBonus();

    const handleReset = async () => {
        const result = await resetRule(ruleType);
        if (result) {
            onSuccess();
        }
    };

    const formatRuleType = (type: BonusTransactionType): string => {
        return BonusTransactionTypeDisplay[type] || type;
    };

    return (
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

                <div className={styles.resetActions}>
                    <Button variant="cancel" onClick={onClose} disabled={loading}>
                        Cancel
                    </Button>
                    <Button variant="error" onClick={handleReset} loading={loading} disabled={loading}>
                        Reset to Defaults
                    </Button>
                </div>
            </div>
        </Modal>
    );
};

export default ResetRuleModal;
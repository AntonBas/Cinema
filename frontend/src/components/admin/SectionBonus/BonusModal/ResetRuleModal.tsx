import React from 'react';
import { ConfirmModal } from '@/components/ui/ConfirmModal/ConfirmModal';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import type { BonusTransactionType } from '@/types/bonus';
import { BonusTransactionTypeDisplay } from '@/types/bonus';

interface ResetRuleModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    ruleType: BonusTransactionType;
}

export const ResetRuleModal: React.FC<ResetRuleModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
    ruleType
}) => {
    const { resetRule, loading } = useBonus();

    const handleReset = async () => {
        try {
            const result = await resetRule(ruleType);
            if (result) {
                onSuccess();
            }
        } catch {
            return;
        }
    };

    const formatRuleType = (type: BonusTransactionType): string => {
        return BonusTransactionTypeDisplay[type] || type;
    };

    return (
        <ConfirmModal
            isOpen={isOpen}
            onCancel={onClose}
            onConfirm={handleReset}
            title={`Reset Bonus Rule: ${formatRuleType(ruleType)}`}
            message={`Are you sure you want to reset this bonus rule to its default values?\n${formatRuleType(ruleType)} (${ruleType})\n\n⚠️ This action cannot be undone. All custom settings for this rule will be lost.`}
            confirmText="Reset to Defaults"
            variant="error"
            isLoading={loading}
        />
    );
};

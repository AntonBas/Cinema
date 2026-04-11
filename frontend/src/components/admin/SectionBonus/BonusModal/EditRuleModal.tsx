import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal/Modal';
import { Button } from '@/components/ui/Button/Button';
import { Input } from '@/components/ui/Input/Input';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import type { BonusRulesResponse, BonusRulesRequest, BonusTransactionType } from '@/types/bonus';
import { BonusTransactionTypeDisplay } from '@/types/bonus';
import styles from './BonusModal.module.css';

interface EditRuleModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    rule: BonusRulesResponse;
}

const EditRuleModal: React.FC<EditRuleModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
    rule
}) => {
    const { updateRule, loading } = useBonus();
    const [formValues, setFormValues] = useState({
        points: '',
        moneyRatio: '',
        minPointsPerTransaction: '',
        maxPointsPerTransaction: '',
        active: false
    });

    useEffect(() => {
        if (rule) {
            setFormValues({
                points: rule.points?.toString() || '',
                moneyRatio: rule.moneyRatio || '',
                minPointsPerTransaction: rule.minPointsPerTransaction?.toString() || '',
                maxPointsPerTransaction: rule.maxPointsPerTransaction?.toString() || '',
                active: rule.active
            });
        }
    }, [rule]);

    const handleInputChange = (field: keyof typeof formValues, value: string | boolean) => {
        setFormValues(prev => ({ ...prev, [field]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const requestData: BonusRulesRequest = {};

        if (formValues.points !== (rule.points?.toString() || '')) {
            requestData.points = formValues.points === '' ? null : Number(formValues.points);
        }

        if (formValues.moneyRatio !== (rule.moneyRatio || '')) {
            requestData.moneyRatio = formValues.moneyRatio === '' ? null : formValues.moneyRatio;
        }

        if (formValues.minPointsPerTransaction !== (rule.minPointsPerTransaction?.toString() || '')) {
            requestData.minPointsPerTransaction = formValues.minPointsPerTransaction === ''
                ? null
                : Number(formValues.minPointsPerTransaction);
        }

        if (formValues.maxPointsPerTransaction !== (rule.maxPointsPerTransaction?.toString() || '')) {
            requestData.maxPointsPerTransaction = formValues.maxPointsPerTransaction === ''
                ? null
                : Number(formValues.maxPointsPerTransaction);
        }

        if (formValues.active !== rule.active) {
            requestData.active = formValues.active;
        }

        if (Object.keys(requestData).length === 0) {
            onClose();
            return;
        }

        const result = await updateRule(rule.bonusType as BonusTransactionType, requestData);
        if (result) {
            onSuccess();
        }
    };

    const formatRuleType = (type: string): string => {
        return BonusTransactionTypeDisplay[type as BonusTransactionType] || type;
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={`Edit Bonus Rule: ${formatRuleType(rule.bonusType)}`}
            size="large"
        >
            <div className={styles.ruleInfo}>
                <div className={styles.ruleInfoRow}>
                    <span className={styles.ruleInfoLabel}>Rule Type:</span>
                    <span className={styles.ruleInfoValue}>{formatRuleType(rule.bonusType)}</span>
                </div>
                <div className={styles.ruleInfoRow}>
                    <span className={styles.ruleInfoLabel}>Current Points:</span>
                    <span className={styles.ruleInfoValue}>{rule.points ?? 'Not set'}</span>
                </div>
            </div>

            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Points Awarded</label>
                        <Input
                            type="number"
                            value={formValues.points}
                            onChange={(value) => handleInputChange('points', value)}
                            placeholder="Number of points"
                            min="0"
                            step="1"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Money Ratio</label>
                        <Input
                            type="text"
                            value={formValues.moneyRatio}
                            onChange={(value) => handleInputChange('moneyRatio', value)}
                            placeholder="e.g., 0.1"
                        />
                    </div>
                </div>

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Min Points per Transaction</label>
                        <Input
                            type="number"
                            value={formValues.minPointsPerTransaction}
                            onChange={(value) => handleInputChange('minPointsPerTransaction', value)}
                            placeholder="Minimum points"
                            min="0"
                            step="1"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Max Points per Transaction</label>
                        <Input
                            type="number"
                            value={formValues.maxPointsPerTransaction}
                            onChange={(value) => handleInputChange('maxPointsPerTransaction', value)}
                            placeholder="Maximum points"
                            min="0"
                            step="1"
                        />
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <div className={styles.checkboxGroup}>
                        <input
                            type="checkbox"
                            id="active"
                            checked={formValues.active}
                            onChange={(e) => handleInputChange('active', e.target.checked)}
                        />
                        <label htmlFor="active" className={styles.checkboxLabel}>
                            Active (rule is applied)
                        </label>
                    </div>
                </div>

                <div className={styles.actions}>
                    <Button variant="cancel" onClick={onClose} disabled={loading}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="primary" loading={loading} disabled={loading}>
                        Save Changes
                    </Button>
                </div>
            </form>
        </Modal>
    );
};

export default EditRuleModal;
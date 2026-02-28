import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal/Modal';
import { Button } from '@/components/ui/Button/Button';
import { Input } from '@/components/ui/Input/Input';
import { Notification } from '@/components/ui/Notification/Notification';
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
    const [errorMessage, setErrorMessage] = useState<string>('');
    const [showNotification, setShowNotification] = useState(false);

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

    const handleInputChange = (field: keyof typeof formValues, value: any) => {
        setFormValues(prev => ({ ...prev, [field]: value }));
        if (errorMessage) setErrorMessage('');
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
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
                setShowNotification(true);
                setTimeout(() => setShowNotification(false), 1500);
                return;
            }

            await updateRule(rule.bonusType as BonusTransactionType, requestData);

            setShowNotification(true);
            setTimeout(() => {
                setShowNotification(false);
                onSuccess();
            }, 1500);
        } catch (err) {
            setErrorMessage('Failed to update rule');
        }
    };

    const formatRuleType = (type: string): string => {
        return BonusTransactionTypeDisplay[type as BonusTransactionType] || type;
    };

    return (
        <>
            <Modal
                isOpen={isOpen}
                onClose={onClose}
                title={`Edit Bonus Rule: ${formatRuleType(rule.bonusType)}`}
                size="medium"
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
                            <label className={styles.label}>
                                Points Awarded
                            </label>
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
                            <label className={styles.label}>
                                Money Ratio
                            </label>
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
                            <label className={styles.label}>
                                Min Points per Transaction
                            </label>
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
                            <label className={styles.label}>
                                Max Points per Transaction
                            </label>
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

                    {errorMessage && (
                        <div style={{ color: 'var(--error)', fontSize: '14px' }}>
                            Error: {errorMessage}
                        </div>
                    )}

                    <div className={styles.actions}>
                        <Button
                            variant="cancel"
                            onClick={onClose}
                            disabled={loading}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            variant="primary"
                            loading={loading}
                            disabled={loading}
                        >
                            Save Changes
                        </Button>
                    </div>
                </form>
            </Modal>

            {showNotification && (
                <Notification
                    id="edit-rule-success"
                    message="Bonus rule updated successfully!"
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

export default EditRuleModal;
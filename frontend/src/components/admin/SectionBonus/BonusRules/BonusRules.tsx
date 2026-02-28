import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button/Button';
import { Badge } from '@/components/ui/Badge/Badge';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import EditRuleModal from '../BonusModal/EditRuleModal';
import ResetRuleModal from '../BonusModal/ResetRuleModal';
import type { BonusRulesResponse, BonusTransactionType } from '@/types/bonus';
import { BonusTransactionTypeDisplay } from '@/types/bonus';
import styles from './BonusRules.module.css';

const BonusRules = () => {
    const { getAllRules, loading } = useBonus();
    const [rules, setRules] = useState<BonusRulesResponse[]>([]);
    const [errorMessage, setErrorMessage] = useState('');
    const [editingRule, setEditingRule] = useState<BonusRulesResponse | null>(null);
    const [resettingRuleType, setResettingRuleType] = useState<BonusTransactionType | null>(null);

    useEffect(() => {
        loadRules();
    }, []);

    const loadRules = async () => {
        try {
            const response = await getAllRules();
            setRules(response.data);
            setErrorMessage('');
        } catch (err) {
            setErrorMessage('Failed to load bonus rules');
        }
    };

    const handleEditRule = (rule: BonusRulesResponse) => {
        setEditingRule(rule);
    };

    const handleResetRule = (type: BonusTransactionType) => {
        setResettingRuleType(type);
    };

    const handleEditSuccess = () => {
        setEditingRule(null);
        loadRules();
    };

    const handleResetSuccess = () => {
        setResettingRuleType(null);
        loadRules();
    };

    const getRuleStatus = (rule: BonusRulesResponse) => {
        return rule.active ? 'Active' : 'Inactive';
    };

    const getRuleStatusVariant = (rule: BonusRulesResponse) => {
        return rule.active ? 'success' : 'error';
    };

    const activeRulesCount = rules.filter(rule => rule.active).length;

    if (loading) {
        return <div className={styles.loading}>Loading rules...</div>;
    }

    if (errorMessage) {
        return <div className={styles.error}>Error: {errorMessage}</div>;
    }

    return (
        <div className={styles.rules}>
            <div className={styles.header}>
                <div>
                    <h2 className={styles.title}>Bonus Rules Configuration</h2>
                    <p className={styles.description}>
                        Configure how bonus points are awarded and used
                    </p>
                </div>
                <div className={styles.stats}>
                    <div className={styles.stat}>
                        <span className={styles.statValue}>{rules.length}</span>
                        <span className={styles.statLabel}>Total Rules</span>
                    </div>
                    <div className={styles.stat}>
                        <span className={styles.statValue}>{activeRulesCount}</span>
                        <span className={styles.statLabel}>Active Rules</span>
                    </div>
                </div>
            </div>

            <div className={styles.tableContainer}>
                <table className={styles.table}>
                    <thead>
                        <tr>
                            <th>Type</th>
                            <th>Points</th>
                            <th>Money Ratio</th>
                            <th>Min Points</th>
                            <th>Max Points</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {rules.map((rule) => (
                            <tr key={rule.id}>
                                <td>
                                    <strong>{BonusTransactionTypeDisplay[rule.bonusType as BonusTransactionType]}</strong>
                                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                                        {rule.bonusType}
                                    </div>
                                </td>
                                <td>{rule.points ?? 'N/A'}</td>
                                <td>{rule.moneyRatio ?? 'N/A'}</td>
                                <td>{rule.minPointsPerTransaction}</td>
                                <td>{rule.maxPointsPerTransaction}</td>
                                <td>
                                    <Badge variant={getRuleStatusVariant(rule)}>
                                        {getRuleStatus(rule)}
                                    </Badge>
                                </td>
                                <td>
                                    <div className={styles.actions}>
                                        <Button
                                            variant="secondary"
                                            size="small"
                                            onClick={() => handleEditRule(rule)}
                                        >
                                            Edit
                                        </Button>
                                        <Button
                                            variant="error"
                                            size="small"
                                            onClick={() => handleResetRule(rule.bonusType as BonusTransactionType)}
                                        >
                                            Reset
                                        </Button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {rules.length === 0 && (
                <div className={styles.empty}>
                    <p>No bonus rules found</p>
                </div>
            )}

            {editingRule && (
                <EditRuleModal
                    isOpen={!!editingRule}
                    onClose={() => setEditingRule(null)}
                    onSuccess={handleEditSuccess}
                    rule={editingRule}
                />
            )}

            {resettingRuleType && (
                <ResetRuleModal
                    isOpen={!!resettingRuleType}
                    onClose={() => setResettingRuleType(null)}
                    onSuccess={handleResetSuccess}
                    ruleType={resettingRuleType}
                />
            )}
        </div>
    );
};

export default BonusRules;
import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button/Button';
import { Badge } from '@/components/ui/Badge/Badge';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import EditRuleModal from './BonusModal/EditRuleModal';
import ResetRuleModal from './BonusModal/ResetRuleModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { BonusRulesResponse, BonusTransactionType } from '@/types/bonus';
import { BonusTransactionTypeDisplay } from '@/types/bonus';
import styles from './SectionBonus.module.css';

const SectionBonus = () => {
    const { getAllRules, allRules, loading } = useBonus();
    const [error, setError] = useState<string | null>(null);
    const [editingRule, setEditingRule] = useState<BonusRulesResponse | null>(null);
    const [resettingRuleType, setResettingRuleType] = useState<BonusTransactionType | null>(null);

    useEffect(() => {
        loadRules();
    }, []);

    const loadRules = async (skipCache: boolean = false) => {
        try {
            setError(null);
            await getAllRules(skipCache);
        } catch (err) {
            setError('Failed to load bonus rules');
        }
    };

    const handleEditRule = (rule: BonusRulesResponse) => {
        setEditingRule(rule);
    };

    const handleResetRule = (type: BonusTransactionType) => {
        setResettingRuleType(type);
    };

    const handleEditSuccess = async () => {
        setEditingRule(null);
        await loadRules(true);
    };

    const handleResetSuccess = async () => {
        setResettingRuleType(null);
        await loadRules(true);
    };

    const getRuleStatus = (rule: BonusRulesResponse) => {
        return rule.active ? 'Active' : 'Inactive';
    };

    const getRuleStatusVariant = (rule: BonusRulesResponse) => {
        return rule.active ? 'success' : 'error';
    };

    if (loading && !allRules.length) {
        return (
            <div className={styles.section}>
                <div className={styles.loading}>
                    <LoadingSpinner text="Loading bonus rules..." />
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.section}>
                <div className={styles.error}>
                    <h3>Error loading bonus system</h3>
                    <p>{error}</p>
                    <button
                        className={styles.retryButton}
                        onClick={() => loadRules(true)}
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
                <h1 className={styles.title}>Bonus Rules Configuration</h1>
                <p className={styles.description}>
                    Configure how bonus points are awarded and used
                </p>
            </div>

            <div className={styles.tableContainer}>
                <table className={styles.table}>
                    <thead className={styles.tableHead}>
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
                        {allRules.map((rule) => (
                            <tr key={rule.id}>
                                <td>
                                    <strong>{BonusTransactionTypeDisplay[rule.bonusType as BonusTransactionType]}</strong>
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

            {!allRules.length && !loading && (
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

export default SectionBonus;
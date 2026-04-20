import { useEffect, useState } from "react";
import { Button } from "@/components/ui/Button/Button";
import { Badge } from "@/components/ui/Badge/Badge";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import EditRuleModal from "./BonusModal/EditRuleModal";
import ResetRuleModal from "./BonusModal/ResetRuleModal";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import type { BonusRulesResponse, BonusTransactionType } from "@/types/bonus";
import { BonusTransactionTypeDisplay } from "@/types/bonus";
import styles from "./SectionBonus.module.css";

const SectionBonus = () => {
  const { getAllRules, rules, loading, rulesError } = useBonus();
  const [editingRule, setEditingRule] = useState<BonusRulesResponse | null>(
    null,
  );
  const [resettingRuleType, setResettingRuleType] =
    useState<BonusTransactionType | null>(null);

  useEffect(() => {
    getAllRules();
  }, []);

  const handleEditSuccess = async () => {
    setEditingRule(null);
    await getAllRules();
  };

  const handleResetSuccess = async () => {
    setResettingRuleType(null);
    await getAllRules();
  };

  const getRuleStatus = (rule: BonusRulesResponse) => {
    return rule.active ? "Active" : "Inactive";
  };

  const getRuleStatusVariant = (rule: BonusRulesResponse) => {
    return rule.active ? "success" : "error";
  };

  if (loading && !rules.length) {
    return (
      <div className={styles.section}>
        <div className={styles.loading}>
          <LoadingSpinner text="Loading bonus rules..." />
        </div>
      </div>
    );
  }

  if (rulesError) {
    return (
      <div className={styles.section}>
        <div className={styles.error}>
          <h3>Error loading bonus system</h3>
          <p>{rulesError.message}</p>
          <Button onClick={() => getAllRules()}>Try Again</Button>
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
            {rules.map((rule) => (
              <tr key={rule.id}>
                <td data-label="Type">
                  <strong>
                    {
                      BonusTransactionTypeDisplay[
                        rule.bonusType as BonusTransactionType
                      ]
                    }
                  </strong>
                </td>
                <td data-label="Points">{rule.points ?? "N/A"}</td>
                <td data-label="Money Ratio">{rule.moneyRatio ?? "N/A"}</td>
                <td data-label="Min Points">
                  {rule.minPointsPerTransaction ?? "N/A"}
                </td>
                <td data-label="Max Points">
                  {rule.maxPointsPerTransaction ?? "N/A"}
                </td>
                <td data-label="Status">
                  <Badge variant={getRuleStatusVariant(rule)}>
                    {getRuleStatus(rule)}
                  </Badge>
                </td>
                <td data-label="Actions">
                  <div className={styles.actions}>
                    <Button
                      variant="secondary"
                      size="small"
                      onClick={() => setEditingRule(rule)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="error"
                      size="small"
                      onClick={() =>
                        setResettingRuleType(
                          rule.bonusType as BonusTransactionType,
                        )
                      }
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

      {!rules.length && !loading && (
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

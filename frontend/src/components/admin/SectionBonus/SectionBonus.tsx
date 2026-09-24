import { useEffect, useState } from "react";
import { Pencil, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/Button/Button";
import { Badge } from "@/components/ui/Badge/Badge";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import { EditRuleModal } from "./BonusModal/EditRuleModal";
import { ResetRuleModal } from "./BonusModal/ResetRuleModal";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import type { BonusRulesResponse, BonusTransactionType } from "@/types/bonus";
import { BonusTransactionTypeDisplay } from "@/types/bonus";
import { ActionIconButton } from "@/components/admin/shared/ActionIconButton/ActionIconButton";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import { formatRuleSettings } from "./bonusRuleFields";
import styles from "./SectionBonus.module.css";

export const SectionBonus = () => {
  const { getRules, rules, loading, rulesError } = useBonus();
  const [editingRule, setEditingRule] = useState<BonusRulesResponse | null>(
    null,
  );
  const [resettingRuleType, setResettingRuleType] =
    useState<BonusTransactionType | null>(null);

  useEffect(() => {
    getRules().catch(() => {});
  }, [getRules]);

  const handleEditSuccess = async () => {
    setEditingRule(null);
    await getRules();
  };

  const handleResetSuccess = async () => {
    setResettingRuleType(null);
    await getRules();
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
          <h3>Error Loading Bonus System</h3>
          <p>{rulesError.message}</p>
          <Button onClick={() => getRules()}>Try Again</Button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.section}>
      <PageHeader
        title="Bonus Rules"
        subtitle="Configure how bonus points are awarded and used"
      />

      <div className={tableStyles.wrapper}>
        <div className={tableStyles.container}>
          <table className={tableStyles.table}>
            <colgroup>
              <col style={{ width: "22%" }} />
              <col style={{ width: "44%" }} />
              <col style={{ width: "14%" }} />
              <col style={{ width: "20%" }} />
            </colgroup>
            <thead>
              <tr>
                <th>Type</th>
                <th>Settings</th>
                <th>Status</th>
                <th className={tableStyles.actionsCol}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rules.map((rule) => (
                <tr key={rule.id}>
                  <td data-label="Type">
                    <span className={styles.type}>
                      {BonusTransactionTypeDisplay[rule.bonusType]}
                    </span>
                  </td>
                  <td data-label="Settings">{formatRuleSettings(rule)}</td>
                  <td data-label="Status">
                    <Badge variant={getRuleStatusVariant(rule)}>
                      {getRuleStatus(rule)}
                    </Badge>
                  </td>
                  <td data-label="Actions">
                    <div className={tableStyles.actions}>
                      <ActionIconButton
                        icon={<Pencil />}
                        label="Edit rule"
                        variant="success"
                        onClick={() => setEditingRule(rule)}
                      />
                      <ActionIconButton
                        icon={<RotateCcw />}
                        label="Reset rule to default"
                        variant="error"
                        onClick={() => setResettingRuleType(rule.bonusType)}
                      />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

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

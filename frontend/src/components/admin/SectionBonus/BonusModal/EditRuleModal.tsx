import React, { useState } from "react";
import { Modal } from "@/components/ui/Modal/Modal";
import { Button } from "@/components/ui/Button/Button";
import { Input } from "@/components/ui/Input/Input";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import type {
  BonusRuleField,
  BonusRulesRequest,
  BonusRulesResponse,
} from "@/types/bonus";
import { BonusTransactionTypeDisplay } from "@/types/bonus";
import {
  BONUS_RULE_DESCRIPTIONS,
  getFieldLabel,
  getRuleFields,
} from "../bonusRuleFields";
import styles from "./BonusModal.module.css";

interface EditRuleModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  rule: BonusRulesResponse;
}

type FieldValues = Record<BonusRuleField, string>;

const toInputValue = (value?: number | string | null) =>
  value == null ? "" : String(Number(value));

const LIMIT_FIELDS: BonusRuleField[] = [
  "minPointsPerTransaction",
  "maxPointsPerTransaction",
];

const toNumberOrNull = (value: string) => (value === "" ? null : Number(value));

export const EditRuleModal: React.FC<EditRuleModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  rule,
}) => {
  const { updateRule, loading } = useBonus();
  const fields = getRuleFields(rule);
  const [values, setValues] = useState<FieldValues>({
    points: toInputValue(rule.points),
    moneyRatio: toInputValue(rule.moneyRatio),
    minPointsPerTransaction: toInputValue(rule.minPointsPerTransaction),
    maxPointsPerTransaction: toInputValue(rule.maxPointsPerTransaction),
  });
  const [active, setActive] = useState(rule.active);

  const handleValueChange = (field: BonusRuleField, value: string) => {
    setValues((prev) => ({ ...prev, [field]: value }));
  };

  const valueFor = (field: BonusRuleField) =>
    fields.includes(field) ? values[field] : "";

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const request: BonusRulesRequest = {
      points: toNumberOrNull(valueFor("points")),
      moneyRatio: valueFor("moneyRatio") || null,
      minPointsPerTransaction: toNumberOrNull(
        valueFor("minPointsPerTransaction"),
      ),
      maxPointsPerTransaction: toNumberOrNull(
        valueFor("maxPointsPerTransaction"),
      ),
      active,
    };

    try {
      const result = await updateRule(rule.bonusType, request);
      if (result) {
        onSuccess();
      }
    } catch {
      return;
    }
  };

  const ruleName = BonusTransactionTypeDisplay[rule.bonusType];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Edit Bonus Rule: ${ruleName}`}
      size="large"
    >
      {BONUS_RULE_DESCRIPTIONS[rule.bonusType] && (
        <p className={styles.description}>
          {BONUS_RULE_DESCRIPTIONS[rule.bonusType]}
        </p>
      )}

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.fields}>
          {fields.map((field) => (
            <div
              key={field}
              className={
                LIMIT_FIELDS.includes(field) ? undefined : styles.fullRow
              }
            >
              <Input
                id={`bonus-rule-${field}`}
                label={getFieldLabel(rule.bonusType, field)}
                type="number"
                value={values[field]}
                onChange={(value) => handleValueChange(field, value)}
                required={rule.requiredFields.includes(field)}
                min={0}
                step={field === "moneyRatio" ? "0.0001" : "1"}
                max={field === "moneyRatio" ? 10 : undefined}
                placeholder={
                  rule.requiredFields.includes(field) ? undefined : "No limit"
                }
              />
            </div>
          ))}
        </div>

        <div className={styles.checkboxGroup}>
          <input
            type="checkbox"
            id="bonus-rule-active"
            checked={active}
            onChange={(e) => setActive(e.target.checked)}
          />
          <label htmlFor="bonus-rule-active" className={styles.checkboxLabel}>
            Active (rule is applied)
          </label>
        </div>

        <div className={styles.actions}>
          <Button variant="cancel" onClick={onClose} disabled={loading}>
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
  );
};

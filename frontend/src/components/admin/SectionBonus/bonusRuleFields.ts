import type {
  BonusRuleField,
  BonusRulesResponse,
  BonusTransactionType,
} from "@/types/bonus";

export const BONUS_RULE_FIELD_ORDER: BonusRuleField[] = [
  "points",
  "moneyRatio",
  "minPointsPerTransaction",
  "maxPointsPerTransaction",
];

export const BONUS_RULE_DESCRIPTIONS: Partial<
  Record<BonusTransactionType, string>
> = {
  WELCOME_BONUS: "Points given once when a user registers.",
  BIRTHDAY_BONUS: "Points given to a verified user on their birthday.",
  BOOKING_SPEND:
    "Limits on how many points a user can spend on one booking. Leave a limit empty to remove it.",
  PAYMENT_ACCRUAL:
    "Points earned for each payment: amount × points per ₴1, kept within the limits. Leave a limit empty to remove it.",
};

const LIMIT_SUBJECT: Partial<Record<BonusTransactionType, string>> = {
  BOOKING_SPEND: "booking",
  PAYMENT_ACCRUAL: "payment",
};

export const getRuleFields = (rule: BonusRulesResponse): BonusRuleField[] =>
  BONUS_RULE_FIELD_ORDER.filter(
    (field) =>
      rule.requiredFields.includes(field) ||
      rule.optionalFields.includes(field),
  );

export const getFieldLabel = (
  type: BonusTransactionType,
  field: BonusRuleField,
): string => {
  const subject = LIMIT_SUBJECT[type] ?? "transaction";
  switch (field) {
    case "points":
      return "Points awarded";
    case "moneyRatio":
      return "Points per ₴1";
    case "minPointsPerTransaction":
      return `Min points per ${subject}`;
    case "maxPointsPerTransaction":
      return `Max points per ${subject}`;
  }
};

const formatLimits = (min?: number | null, max?: number | null) => {
  if (min != null && max != null) return `${min}–${max} pts`;
  if (min != null) return `min ${min} pts`;
  if (max != null) return `max ${max} pts`;
  return null;
};

export const formatRuleSettings = (rule: BonusRulesResponse): string => {
  const fields = getRuleFields(rule);
  const parts: string[] = [];

  if (fields.includes("points") && rule.points != null) {
    parts.push(`${rule.points} pts`);
  }
  if (fields.includes("moneyRatio") && rule.moneyRatio != null) {
    parts.push(`${Number(rule.moneyRatio)} pts per ₴1`);
  }
  if (fields.includes("minPointsPerTransaction")) {
    const limits = formatLimits(
      rule.minPointsPerTransaction,
      rule.maxPointsPerTransaction,
    );
    const subject = LIMIT_SUBJECT[rule.bonusType] ?? "transaction";
    parts.push(limits ? `${limits} per ${subject}` : "No limits");
  }

  return parts.length ? parts.join(" · ") : "Not configured";
};

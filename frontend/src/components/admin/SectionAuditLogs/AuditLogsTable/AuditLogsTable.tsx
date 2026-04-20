import React from "react";
import { Badge, Tooltip } from "@/components/ui";
import type { AuditLogResponse } from "@/types/audit";
import styles from "./AuditLogsTable.module.css";

interface AuditLogsTableProps {
  logs: AuditLogResponse[];
}

const truncateText = (text: string, maxLength: number = 40): string => {
  if (!text) return "";
  return text.length > maxLength ? `${text.substring(0, maxLength)}...` : text;
};

const formatDateTime = (dateStr: string) => {
  const date = new Date(dateStr);
  const formattedDate = date.toLocaleDateString("uk-UA");
  const formattedTime = date.toLocaleTimeString("uk-UA", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
  return { date: formattedDate, time: formattedTime };
};

const formatFieldName = (field: string): string => {
  const fieldMap: Record<string, string> = {
    points: "Points",
    moneyRatio: "Money Ratio",
    minPointsPerTransaction: "Min Points",
    maxPointsPerTransaction: "Max Points",
    bonusType: "Bonus Type",
    active: "Active",
    role: "Role",
    enabled: "Enabled",
    status: "Status",
    title: "Title",
    description: "Description",
    startDate: "Start Date",
    endDate: "End Date",
    displayName: "Display Name",
    priceMultiplier: "Price Multiplier",
    minAge: "Min Age",
    maxAge: "Max Age",
    firstName: "First Name",
    lastName: "Last Name",
    email: "Email",
    phoneNumber: "Phone Number",
    city: "City",
    dateOfBirth: "Date of Birth",
  };
  return fieldMap[field] || field;
};

const formatDetails = (log: AuditLogResponse): React.ReactNode => {
  if (!log.details || log.details.length === 0) return "—";

  return (
    <div className={styles.changeObject}>
      {log.details.map((detail, idx) => (
        <div key={idx} className={styles.changeField}>
          <span className={styles.changeKey}>
            {formatFieldName(detail.fieldName)}:
          </span>
          <Tooltip content={detail.oldValue || "null"}>
            <span className={styles.oldValue}>
              {truncateText(detail.oldValue || "null", 20)}
            </span>
          </Tooltip>
          {" → "}
          <Tooltip content={detail.newValue || "null"}>
            <span className={styles.newValue}>
              {truncateText(detail.newValue || "null", 20)}
            </span>
          </Tooltip>
        </div>
      ))}
    </div>
  );
};

const getActionDisplay = (action: string): string => {
  return action
    .replace(/_/g, " ")
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
};

const getBadgeVariant = (
  action: string,
): "success" | "error" | "warning" | "secondary" => {
  if (action.includes("CREATED") || action.includes("REGISTER"))
    return "success";
  if (action.includes("DELETED") || action.includes("REJECTED")) return "error";
  if (action.includes("SUCCESS") || action.includes("CONFIRMED"))
    return "success";
  if (action.includes("FAILED") || action.includes("CANCELLED")) return "error";
  if (action.includes("UPDATED") || action.includes("TOGGLE")) return "warning";
  if (action.includes("REFUND") || action.includes("POINTS")) return "warning";
  return "secondary";
};

export const AuditLogsTable: React.FC<AuditLogsTableProps> = ({ logs }) => {
  if (logs.length === 0) {
    return (
      <div className={styles.emptyContainer}>
        <p className={styles.emptyText}>No audit logs found</p>
      </div>
    );
  }

  return (
    <div className={styles.tableWrapper}>
      <table className={styles.table}>
        <thead className={styles.tableHead}>
          <tr>
            <th className={styles.th}>Time</th>
            <th className={styles.th}>Changed By</th>
            <th className={styles.th}>Target</th>
            <th className={styles.th}>Action</th>
            <th className={styles.th}>Changes</th>
          </tr>
        </thead>
        <tbody className={styles.tableBody}>
          {logs.map((log) => {
            const { date, time } = formatDateTime(log.changedAt);
            return (
              <tr key={log.id} className={styles.tr}>
                <td className={styles.td} data-label="Time">
                  <span className={styles.timeDate}>{date}</span>
                  <span className={styles.timeTime}>{time}</span>
                </td>
                <td className={styles.td} data-label="Changed By">
                  {log.changedBy}
                </td>
                <td className={styles.td} data-label="Target">
                  <Tooltip content={log.targetInfo}>
                    <span className={styles.targetInfo}>
                      {truncateText(log.targetInfo, 30)}
                    </span>
                  </Tooltip>
                </td>
                <td className={styles.td} data-label="Action">
                  <Badge
                    variant={getBadgeVariant(log.action)}
                    className={styles.actionBadge}
                  >
                    {getActionDisplay(log.action)}
                  </Badge>
                </td>
                <td className={styles.td} data-label="Changes">
                  {formatDetails(log)}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

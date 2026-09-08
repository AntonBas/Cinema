import React from "react";
import { Badge, Tooltip } from "@/components/ui";
import type { AuditLogResponse } from "@/types/audit";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import styles from "./AuditLogsTable.module.css";

interface AuditLogsTableProps {
  logs: AuditLogResponse[];
  onViewHistory?: (entityType: string, entityId: number) => void;
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

export const AuditLogsTable: React.FC<AuditLogsTableProps> = ({
  logs,
  onViewHistory,
}) => {
  if (logs.length === 0) {
    return (
      <div className={tableStyles.empty}>
        <h3>No audit logs found</h3>
        <p>Actions will appear here once changes are made.</p>
      </div>
    );
  }

  return (
    <div className={tableStyles.wrapper}>
      <div className={tableStyles.container}>
        <table className={tableStyles.table}>
          <colgroup>
            <col style={{ width: "12%" }} />
            <col style={{ width: "16%" }} />
            <col style={{ width: "17%" }} />
            <col style={{ width: "13%" }} />
            <col style={{ width: "42%" }} />
          </colgroup>
          <thead>
            <tr>
              <th>Time</th>
              <th>Changed By</th>
              <th>Target</th>
              <th>Action</th>
              <th>Changes</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => {
              const { date, time } = formatDateTime(log.changedAt);
              return (
                <tr key={log.id}>
                  <td data-label="Time">
                    <span className={styles.timeDate}>{date}</span>
                    <span className={styles.timeTime}>{time}</span>
                  </td>
                  <td data-label="Changed By">{log.changedBy}</td>
                  <td data-label="Target">
                    {onViewHistory ? (
                      <button
                        type="button"
                        className={styles.targetButton}
                        onClick={() => onViewHistory(log.entityType, log.entityId)}
                      >
                        <Tooltip content={log.targetInfo}>
                          <span className={styles.targetInfo}>
                            {truncateText(log.targetInfo, 30)}
                          </span>
                        </Tooltip>
                      </button>
                    ) : (
                      <Tooltip content={log.targetInfo}>
                        <span className={styles.targetInfo}>
                          {truncateText(log.targetInfo, 30)}
                        </span>
                      </Tooltip>
                    )}
                  </td>
                  <td data-label="Action">
                    <Badge variant={getBadgeVariant(log.action)}>
                      {getActionDisplay(log.action)}
                    </Badge>
                  </td>
                  <td data-label="Changes">{formatDetails(log)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

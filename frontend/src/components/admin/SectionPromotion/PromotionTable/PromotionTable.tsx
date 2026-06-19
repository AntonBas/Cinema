import React from "react";
import { Button, Badge } from "@/components/ui";
import type { PromotionListResponse } from "@/types/promotion";
import { safeFormatDate } from "@/utils/dateUtils";
import styles from "./PromotionTable.module.css";

interface PromotionTableProps {
  promotions: PromotionListResponse[];
  onEdit: (promotionId: number) => void;
  onDelete: (promotionId: number, title: string) => void;
}

const getPromotionStatus = (promotion: PromotionListResponse): string => {
  const now = new Date();
  const startDate = promotion.startDate ? new Date(promotion.startDate) : null;
  const endDate = promotion.endDate ? new Date(promotion.endDate) : null;

  if (!startDate && !endDate) return "active";
  if (startDate && now < startDate) return "upcoming";
  if (endDate && now > endDate) return "expired";
  return "active";
};

const getStatusDisplay = (status: string): string => {
  switch (status) {
    case "active":
      return "Active";
    case "upcoming":
      return "Upcoming";
    case "expired":
      return "Expired";
    default:
      return status;
  }
};

const getStatusVariant = (status: string) => {
  switch (status) {
    case "active":
      return "success";
    case "upcoming":
      return "warning";
    case "expired":
      return "error";
    default:
      return "info";
  }
};

const PromotionTable: React.FC<PromotionTableProps> = ({
  promotions,
  onEdit,
  onDelete,
}) => {
  if (promotions.length === 0) {
    return (
      <div className={styles.empty}>
        <h3>No promotions found</h3>
        <p>Create your first promotion to get started!</p>
      </div>
    );
  }

  return (
    <div className={styles.tableWrapper}>
      <table className={styles.table}>
        <thead className={styles.tableHead}>
          <tr>
            <th className={styles.th}>Title</th>
            <th className={styles.th}>Bonus Points</th>
            <th className={styles.th}>Date Range</th>
            <th className={styles.th}>Status</th>
            <th className={styles.th}>Actions</th>
          </tr>
        </thead>
        <tbody className={styles.tableBody}>
          {promotions.map((promotion) => {
            const status = getPromotionStatus(promotion);

            return (
              <tr key={promotion.id} className={styles.tr}>
                <td className={styles.td} data-label="Title">
                  <div className={styles.titleCell}>
                    <div className={styles.title}>{promotion.title}</div>
                  </div>
                </td>
                <td className={styles.td} data-label="Bonus Points">
                  <span className={styles.points}>
                    {promotion.bonusPoints} pts
                  </span>
                </td>
                <td className={styles.td} data-label="Date Range">
                  <div className={styles.dates}>
                    <div>{safeFormatDate(promotion.startDate)}</div>
                    <div className={styles.dateSeparator}>to</div>
                    <div>{safeFormatDate(promotion.endDate)}</div>
                  </div>
                </td>
                <td className={styles.td} data-label="Status">
                  <Badge variant={getStatusVariant(status)}>
                    {getStatusDisplay(status)}
                  </Badge>
                </td>
                <td className={styles.td} data-label="Actions">
                  <div className={styles.actions}>
                    <Button
                      variant="success"
                      size="small"
                      onClick={() => onEdit(promotion.id)}
                      className={styles.actionButton}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="error"
                      size="small"
                      onClick={() => onDelete(promotion.id, promotion.title)}
                      className={styles.actionButton}
                    >
                      Delete
                    </Button>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default PromotionTable;

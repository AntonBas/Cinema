import React from "react";
import { Pencil, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui";
import type { PromotionListResponse } from "@/types/promotion";
import { safeFormatDate } from "@/utils/dateUtils";
import { ActionIconButton } from "@/components/admin/shared/ActionIconButton/ActionIconButton";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
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
      <div className={tableStyles.empty}>
        <h3>No promotions found</h3>
        <p>Create your first promotion to get started!</p>
      </div>
    );
  }

  return (
    <div className={tableStyles.wrapper}>
      <div className={tableStyles.container}>
        <table className={tableStyles.table}>
          <colgroup>
            <col style={{ width: "26%" }} />
            <col style={{ width: "16%" }} />
            <col style={{ width: "22%" }} />
            <col style={{ width: "16%" }} />
            <col style={{ width: "20%" }} />
          </colgroup>
          <thead>
            <tr>
              <th>Title</th>
              <th>Bonus Points</th>
              <th>Date Range</th>
              <th>Status</th>
              <th className={tableStyles.actionsCol}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {promotions.map((promotion) => {
              const status = getPromotionStatus(promotion);

              return (
                <tr key={promotion.id}>
                  <td data-label="Title">
                    <div className={styles.title}>{promotion.title}</div>
                  </td>
                  <td data-label="Bonus Points">
                    <span className={styles.points}>
                      {promotion.bonusPoints} pts
                    </span>
                  </td>
                  <td data-label="Date Range">
                    <div className={styles.dates}>
                      <div>{safeFormatDate(promotion.startDate)}</div>
                      <div className={styles.dateSeparator}>to</div>
                      <div>{safeFormatDate(promotion.endDate)}</div>
                    </div>
                  </td>
                  <td data-label="Status">
                    <Badge variant={getStatusVariant(status)}>
                      {getStatusDisplay(status)}
                    </Badge>
                  </td>
                  <td data-label="Actions">
                    <div className={tableStyles.actions}>
                      <ActionIconButton
                        icon={<Pencil />}
                        label="Edit promotion"
                        variant="success"
                        onClick={() => onEdit(promotion.id)}
                      />
                      <ActionIconButton
                        icon={<Trash2 />}
                        label="Delete promotion"
                        variant="error"
                        onClick={() => onDelete(promotion.id, promotion.title)}
                      />
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PromotionTable;

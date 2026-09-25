import React from "react";
import { Pencil, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/Badge/Badge";
import type { PromotionListResponse, PromotionStatus } from "@/types/promotion";
import { formatDate } from "@/utils/formatters";
import { ActionIconButton } from "@/components/admin/shared/ActionIconButton/ActionIconButton";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import styles from "./PromotionTable.module.css";

interface PromotionTableProps {
  promotions: PromotionListResponse[];
  onEdit: (promotionId: number) => void;
  onDelete: (promotionId: number, title: string) => void;
}

const getStatusDisplay = (status: PromotionStatus): string => {
  switch (status) {
    case "ACTIVE":
      return "Active";
    case "UPCOMING":
      return "Upcoming";
    case "EXPIRED":
      return "Expired";
    case "INACTIVE":
      return "Inactive";
  }
};

const getStatusVariant = (status: PromotionStatus) => {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "UPCOMING":
      return "warning";
    case "EXPIRED":
      return "error";
    case "INACTIVE":
      return "secondary";
  }
};

export const PromotionTable: React.FC<PromotionTableProps> = ({
  promotions,
  onEdit,
  onDelete,
}) => {
  if (promotions.length === 0) {
    return (
      <EmptyState
        title="No Promotions Found"
        message="Create your first promotion to get started!"
      />
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
              const status = promotion.status;

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
                      <div>{formatDate(promotion.startDate)}</div>
                      <div className={styles.dateSeparator}>to</div>
                      <div>{formatDate(promotion.endDate)}</div>
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

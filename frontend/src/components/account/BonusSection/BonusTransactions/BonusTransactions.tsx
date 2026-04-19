import React from "react";
import { Badge } from "@/components/ui/Badge/Badge";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import type {
  BonusTransactionResponse,
  BonusTransactionType,
} from "@/types/bonus";
import { BonusTransactionTypeDisplay } from "@/types/bonus";
import styles from "./BonusTransactions.module.css";

interface BonusTransactionsProps {
  transactions: BonusTransactionResponse[];
  loading: boolean;
  onPageChange?: (page: number) => void;
  currentPage?: number;
  totalPages?: number;
  totalElements?: number;
}

const getBadgeVariant = (
  type: BonusTransactionType,
): "success" | "primary" | "error" | "warning" | "secondary" => {
  switch (type) {
    case "WELCOME_BONUS":
    case "BIRTHDAY_BONUS":
    case "PROMOTION_BONUS":
      return "success";
    case "PAYMENT_ACCRUAL":
    case "BOOKING_SPEND":
      return "primary";
    case "REFUND_RETURN":
      return "warning";
    case "BOOKING_CANCEL":
      return "error";
    default:
      return "secondary";
  }
};

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export const BonusTransactions: React.FC<BonusTransactionsProps> = ({
  transactions,
  loading,
  onPageChange,
  currentPage = 0,
  totalPages = 1,
  totalElements = 0,
}) => {
  const showLoading = useDelayedLoading(loading, {
    delay: 200,
    minDisplayTime: 300,
  });

  if (showLoading) {
    return (
      <div className={styles.transactions}>
        <div className={styles.loadingContainer}>
          <div className={styles.spinner}></div>
          <span>Loading transactions...</span>
        </div>
      </div>
    );
  }

  if (!transactions.length) {
    return (
      <div className={styles.transactions}>
        <div className={styles.noData}>
          <span className={styles.noDataIcon}>📋</span>
          <p>No transactions found</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.transactions}>
      <div className={styles.header}>
        <h2 className={styles.title}>Bonus Transactions</h2>
        <div className={styles.count}>
          <span className={styles.countNumber}>
            {totalElements || transactions.length}
          </span>{" "}
          transactions
        </div>
      </div>

      <div className={styles.table}>
        <div className={styles.tableHeader}>
          <div className={styles.headerCell}>Date & Time</div>
          <div className={styles.headerCell}>Type</div>
          <div className={styles.headerCell}>Points Change</div>
          <div className={styles.headerCell}>New Balance</div>
        </div>

        <div className={styles.tableBody}>
          {transactions.map((transaction) => {
            const pointsValue = parseFloat(transaction.pointsChange);
            const displayType =
              BonusTransactionTypeDisplay[transaction.type] || transaction.type;

            return (
              <div key={transaction.id} className={styles.tableRow}>
                <div className={styles.tableCell}>
                  {formatDate(transaction.createdAt)}
                </div>
                <div className={styles.tableCell}>
                  <Badge
                    variant={getBadgeVariant(transaction.type)}
                    size="small"
                  >
                    {displayType}
                  </Badge>
                </div>
                <div className={styles.tableCell}>
                  <span
                    className={
                      pointsValue > 0 ? styles.positive : styles.negative
                    }
                  >
                    {transaction.pointsChange}
                  </span>
                </div>
                <div className={styles.tableCell}>
                  <span className={styles.balance}>
                    {transaction.newBalance}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {onPageChange && totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            totalElements={totalElements}
            pageSize={20}
            onPageChange={onPageChange}
            showInfo={true}
          />
        </div>
      )}
    </div>
  );
};

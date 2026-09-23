import React, { useEffect, useState } from "react";
import { AccountPageLayout } from "@/components/account/AccountPageLayout/AccountPageLayout";
import { BonusBalanceCard } from "@/components/account/BonusSection/BonusBalanceCard/BonusBalanceCard";
import { BonusTransactions } from "@/components/account/BonusSection/BonusTransactions/BonusTransactions";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import { usePagination } from "@/hooks/common/usePagination";
import { DEFAULT_PAGE_SIZE_ADMIN } from "@/utils/paginationUtils";
import { Tabs, type TabItem } from "@/components/ui/Tabs/Tabs";
import styles from "./BonusPage.module.css";

const BONUS_TABS: ReadonlyArray<TabItem<"balance" | "transactions">> = [
  { id: "balance", label: "Balance" },
  { id: "transactions", label: "Transactions" },
];

export const BonusPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<"balance" | "transactions">(
    "balance",
  );

  const { params, setPage } = usePagination({ size: DEFAULT_PAGE_SIZE_ADMIN });

  const {
    balance,
    transactions,
    transactionsPagination,
    getBalance,
    getTransactions,
  } = useBonus();

  useEffect(() => {
    getBalance().catch(() => {});
  }, [getBalance]);

  useEffect(() => {
    if (activeTab === "transactions") {
      getTransactions({ page: params.page, size: params.size }).catch(() => {});
    }
  }, [activeTab, params.page, params.size, getTransactions]);

  return (
    <AccountPageLayout title="My Bonuses">
      <Tabs
        items={BONUS_TABS}
        activeId={activeTab}
        onChange={setActiveTab}
        ariaLabel="Bonus sections"
      />

      <div className={styles.tabContent}>
        {activeTab === "balance" ? (
          <div className={styles.balanceContent}>
            <BonusBalanceCard balance={balance} loading={!balance} />
          </div>
        ) : (
          <div className={styles.transactionsContent}>
            <BonusTransactions
              transactions={transactions}
              loading={!transactions.length}
              onPageChange={setPage}
              currentPage={transactionsPagination?.number || 0}
              totalPages={transactionsPagination?.totalPages || 1}
              totalElements={transactionsPagination?.totalElements || 0}
            />
          </div>
        )}
      </div>
    </AccountPageLayout>
  );
};

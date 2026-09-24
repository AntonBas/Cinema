import React, { useCallback, useEffect } from "react";
import { AccountPageLayout } from "@/components/account/AccountPageLayout/AccountPageLayout";
import { BonusBalanceCard } from "@/components/account/BonusSection/BonusBalanceCard/BonusBalanceCard";
import { BonusTransactions } from "@/components/account/BonusSection/BonusTransactions/BonusTransactions";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import {
  parseEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { DEFAULT_PAGE_SIZE_ADMIN } from "@/utils/paginationUtils";
import { Tabs, type TabItem } from "@/components/ui/Tabs/Tabs";
import styles from "./BonusPage.module.css";

type BonusTab = "balance" | "transactions";

const BONUS_TABS: ReadonlyArray<TabItem<BonusTab>> = [
  { id: "balance", label: "Balance" },
  { id: "transactions", label: "Transactions" },
];

const BONUS_TAB_IDS = BONUS_TABS.map((tab) => tab.id);

export const BonusPage: React.FC = () => {
  const { page, getParam, setParams, setPage } = useUrlParams();
  const activeTab = parseEnumParam(getParam("tab"), BONUS_TAB_IDS, "balance");

  const handleTabChange = useCallback(
    (tab: BonusTab) => {
      setParams({ tab: toUrlEnumValue(tab, "balance") }, { reset: true });
    },
    [setParams],
  );

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
      getTransactions({ page, size: DEFAULT_PAGE_SIZE_ADMIN }).catch(() => {});
    }
  }, [activeTab, page, getTransactions]);

  return (
    <AccountPageLayout title="My Bonuses">
      <Tabs
        items={BONUS_TABS}
        activeId={activeTab}
        onChange={handleTabChange}
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

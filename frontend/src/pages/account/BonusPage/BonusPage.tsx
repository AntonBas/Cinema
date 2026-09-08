import React, { useEffect, useState } from "react";
import { Layout } from "@/components/layout/Layout/Layout";
import { AccountSidebar } from "@/components/account/AccountSidebar/AccountSidebar";
import { BonusBalanceCard } from "@/components/account/BonusSection/BonusBalanceCard/BonusBalanceCard";
import { BonusTransactions } from "@/components/account/BonusSection/BonusTransactions/BonusTransactions";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import { usePagination } from "@/hooks/common/usePagination";
import styles from "./BonusPage.module.css";

export const BonusPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<"balance" | "transactions">(
    "balance",
  );

  const { params, setPage } = usePagination({ size: 20 });

  const {
    balance,
    transactions,
    transactionsPagination,
    getMyBalance,
    getMyTransactions,
  } = useBonus();

  useEffect(() => {
    getMyBalance().catch(() => {});
  }, [getMyBalance]);

  useEffect(() => {
    if (activeTab === "transactions") {
      getMyTransactions({ page: params.page, size: params.size }).catch(() => {});
    }
  }, [activeTab, params.page, params.size, getMyTransactions]);

  return (
    <Layout>
      <div className={styles.bonusPage}>
        <div className={styles.container}>
          <AccountSidebar />

          <div className={styles.content}>
            <div className={styles.header}>
              <h1 className={styles.title}>My Bonus</h1>
            </div>

            <div className={styles.tabs}>
              <button
                className={`${styles.tab} ${activeTab === "balance" ? styles.active : ""}`}
                onClick={() => setActiveTab("balance")}
              >
                Balance
              </button>
              <button
                className={`${styles.tab} ${activeTab === "transactions" ? styles.active : ""}`}
                onClick={() => setActiveTab("transactions")}
              >
                Transactions
              </button>
            </div>

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
          </div>
        </div>
      </div>
    </Layout>
  );
};

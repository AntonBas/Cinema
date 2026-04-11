import React, { useEffect, useState } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { BonusBalanceCard } from '@/components/account/BonusSection/BonusBalanceCard/BonusBalanceCard';
import { BonusTransactions } from '@/components/account/BonusSection/BonusTransactions/BonusTransactions';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import { usePagination } from '@/hooks/common/usePagination';
import styles from './BonusPage.module.css';

export const BonusPage: React.FC = () => {
    const [activeTab, setActiveTab] = useState<'balance' | 'transactions'>('balance');

    const { params, setPage } = usePagination({ size: 20 });

    const {
        balance,
        transactions,
        transactionsPagination,
        getMyBalance,
        getMyTransactions,
    } = useBonus();

    useEffect(() => {
        getMyBalance();
    }, [getMyBalance]);

    useEffect(() => {
        if (activeTab === 'transactions') {
            getMyTransactions({ page: params.page, size: params.size });
        }
    }, [activeTab, params.page, params.size, getMyTransactions]);

    const totalEarned = transactions
        .filter(t => parseFloat(t.pointsChange) > 0)
        .reduce((sum, t) => sum + parseFloat(t.pointsChange), 0);

    const totalSpent = transactions
        .filter(t => parseFloat(t.pointsChange) < 0)
        .reduce((sum, t) => sum + Math.abs(parseFloat(t.pointsChange)), 0);

    const netChange = totalEarned - totalSpent;

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
                                className={`${styles.tab} ${activeTab === 'balance' ? styles.active : ''}`}
                                onClick={() => setActiveTab('balance')}
                            >
                                Balance
                            </button>
                            <button
                                className={`${styles.tab} ${activeTab === 'transactions' ? styles.active : ''}`}
                                onClick={() => setActiveTab('transactions')}
                            >
                                Transactions
                            </button>
                        </div>

                        <div className={styles.tabContent}>
                            {activeTab === 'balance' ? (
                                <div className={styles.balanceContent}>
                                    <BonusBalanceCard balance={balance} loading={!balance} />

                                    {transactions.length > 0 && (
                                        <div className={styles.summary}>
                                            <h3>Transaction Summary</h3>
                                            <div className={styles.summaryGrid}>
                                                <div className={styles.summaryItem}>
                                                    <div className={styles.summaryLabel}>Total Earned</div>
                                                    <div className={styles.summaryValue}>+{totalEarned} points</div>
                                                </div>
                                                <div className={styles.summaryItem}>
                                                    <div className={styles.summaryLabel}>Total Spent</div>
                                                    <div className={styles.summaryValue}>-{totalSpent} points</div>
                                                </div>
                                                <div className={styles.summaryItem}>
                                                    <div className={styles.summaryLabel}>Net Change</div>
                                                    <div className={`${styles.summaryValue} ${netChange >= 0 ? styles.positive : styles.negative}`}>
                                                        {netChange >= 0 ? '+' : ''}{netChange} points
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    )}
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
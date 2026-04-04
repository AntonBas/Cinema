import React, { useState, useEffect } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { BonusBalanceCard } from '@/components/account/BonusSection/BonusBalanceCard/BonusBalanceCard';
import { BonusTransactions } from '@/components/account/BonusSection/BonusTransactions/BonusTransactions';
import { Notification } from '@/components/ui/Notification/Notification';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import { usePagination } from '@/hooks/common/usePagination';
import { DEFAULT_PAGE_SIZE_ADMIN } from '@/utils/paginationUtils';
import type { BonusBalanceResponse, BonusTransactionResponse } from '@/types/bonus';
import styles from './BonusPage.module.css';

export const BonusPage: React.FC = () => {
    const { getMyBalance, getMyTransactions, loading } = useBonus();
    const [balance, setBalance] = useState<BonusBalanceResponse | null>(null);
    const [transactions, setTransactions] = useState<BonusTransactionResponse[]>([]);
    const [pagination, setPagination] = useState({
        currentPage: 0,
        totalPages: 1,
        totalElements: 0
    });
    const [activeTab, setActiveTab] = useState<'balance' | 'transactions'>('balance');
    const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null);
    const [localError, setLocalError] = useState<string | null>(null);

    const { params, setPage } = usePagination(
        { page: 0, size: DEFAULT_PAGE_SIZE_ADMIN },
        DEFAULT_PAGE_SIZE_ADMIN
    );

    useEffect(() => {
        loadBonusData();
    }, []);

    useEffect(() => {
        if (activeTab === 'transactions') {
            loadTransactions(params.page || 0);
        }
    }, [params.page, activeTab]);

    const loadBonusData = async () => {
        try {
            const balanceResponse = await getMyBalance();
            setBalance(balanceResponse || null);
            setLocalError(null);

            await loadTransactions(0);
        } catch (error: any) {
            console.error('Error loading bonus data:', error);
            setLocalError(error.message || 'Failed to load bonus data');
        }
    };

    const loadTransactions = async (page: number) => {
        try {
            const response = await getMyTransactions({ page, size: DEFAULT_PAGE_SIZE_ADMIN });
            setTransactions(response?.content || []);
            setPagination({
                currentPage: response?.number || 0,
                totalPages: response?.totalPages || 1,
                totalElements: response?.totalElements || 0
            });
        } catch (error: any) {
            console.error('Error loading transactions:', error);
            showNotification('error', 'Failed to load transactions');
        }
    };

    const handlePageChange = (page: number) => {
        setPage(page);
    };

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 3000);
    };

    const getTransactionSummary = () => {
        if (!transactions.length) return null;

        const totalEarned = transactions
            .filter(t => parseFloat(t.pointsChange) > 0)
            .reduce((sum, t) => sum + parseFloat(t.pointsChange), 0);

        const totalSpent = transactions
            .filter(t => parseFloat(t.pointsChange) < 0)
            .reduce((sum, t) => sum + Math.abs(parseFloat(t.pointsChange)), 0);

        const netChange = totalEarned - totalSpent;

        return { totalEarned, totalSpent, netChange };
    };

    const summary = getTransactionSummary();

    return (
        <Layout>
            <div className={styles.bonusPage}>
                <div className={styles.container}>
                    <AccountSidebar activePage="bonuses" />

                    <div className={styles.content}>
                        {notification && (
                            <Notification
                                id="bonus-notification"
                                message={notification.message}
                                type={notification.type}
                                isVisible={true}
                                onClose={() => setNotification(null)}
                                duration={3000}
                                isStatic={true}
                            />
                        )}

                        <div className={styles.header}>
                            <h1 className={styles.title}>My Bonus</h1>
                        </div>

                        {localError && (
                            <div className={styles.error}>
                                {localError}
                            </div>
                        )}

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
                                    <BonusBalanceCard
                                        balance={balance}
                                        loading={loading}
                                    />

                                    {summary && (
                                        <div className={styles.summary}>
                                            <h3>Transaction Summary</h3>
                                            <div className={styles.summaryGrid}>
                                                <div className={styles.summaryItem}>
                                                    <div className={styles.summaryLabel}>Total Earned</div>
                                                    <div className={styles.summaryValue}>
                                                        +{summary.totalEarned} points
                                                    </div>
                                                </div>
                                                <div className={styles.summaryItem}>
                                                    <div className={styles.summaryLabel}>Total Spent</div>
                                                    <div className={styles.summaryValue}>
                                                        -{summary.totalSpent} points
                                                    </div>
                                                </div>
                                                <div className={styles.summaryItem}>
                                                    <div className={styles.summaryLabel}>Net Change</div>
                                                    <div className={`${styles.summaryValue} ${summary.netChange >= 0 ? styles.positive : styles.negative}`}>
                                                        {summary.netChange >= 0 ? '+' : ''}{summary.netChange} points
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
                                        loading={loading}
                                        onPageChange={handlePageChange}
                                        currentPage={pagination.currentPage}
                                        totalPages={pagination.totalPages}
                                        totalElements={pagination.totalElements}
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
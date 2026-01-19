import React, { useState, useEffect } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { BonusBalanceCard } from '@/components/account/BonusBalanceCard/BonusBalanceCard';
import { BonusCardInfo } from '@/components/account/BonusCardInfo/BonusCardInfo';
import { BonusTransactions } from '@/components/account/BonusTransactions/BonusTransactions';
import { Button } from '@/components/ui/Button/Button';
import { Notification } from '@/components/ui/Notification/Notification';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import { useBonusTransactions } from '@/hooks/features/bonus/useBonusTransactions';
import styles from './BonusPage.module.css';

export const BonusPage: React.FC = () => {
    const { getMyBalance, getMyCard, loading: bonusLoading, error: bonusError } = useBonus();
    const {
        transactions,
        loading: transactionsLoading,
        error: transactionsError,
        refresh: refreshTransactions,
        getTransactionSummary
    } = useBonusTransactions({ autoFetch: true });

    const [balance, setBalance] = useState<any>(null);
    const [cardInfo, setCardInfo] = useState<any>(null);
    const [activeTab, setActiveTab] = useState<'balance' | 'transactions'>('balance');
    const [notification, setNotification] = useState<{ type: 'success' | 'error', message: string } | null>(null);

    useEffect(() => {
        loadBonusData();
    }, []);

    const loadBonusData = async () => {
        try {
            const [balanceData, cardData] = await Promise.all([
                getMyBalance(),
                getMyCard()
            ]);
            setBalance(balanceData);
            setCardInfo(cardData);
        } catch (error) {
            console.error('Failed to load bonus data:', error);
        }
    };

    const handleRefresh = async () => {
        try {
            await Promise.all([
                loadBonusData(),
                refreshTransactions()
            ]);
            showNotification('success', 'Bonus data refreshed successfully');
        } catch (error) {
            showNotification('error', 'Failed to refresh bonus data');
        }
    };

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 3000);
    };

    const error = bonusError || transactionsError;
    const loading = bonusLoading || transactionsLoading;
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
                                position={0}
                            />
                        )}

                        <div className={styles.header}>
                            <h1 className={styles.title}>My Bonus</h1>
                            <div className={styles.headerActions}>
                                <Button
                                    variant="primary"
                                    onClick={handleRefresh}
                                    loading={loading}
                                    disabled={loading}
                                    size="medium"
                                >
                                    Refresh
                                </Button>
                            </div>
                        </div>

                        {error && (
                            <div className={styles.error}>
                                {error}
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
                                        loading={bonusLoading}
                                    />
                                    <BonusCardInfo
                                        cardInfo={cardInfo}
                                        loading={bonusLoading}
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
                                        loading={transactionsLoading}
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
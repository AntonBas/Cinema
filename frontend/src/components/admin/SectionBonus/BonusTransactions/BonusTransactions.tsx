import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Pagination } from '@/components/ui/Pagination';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import type { BonusTransactionType } from '@/types/bonus';
import { BonusTransactionTypeDisplay } from '@/types/bonus';
import styles from './BonusTransactions.module.css';

const BonusTransactions = () => {
    const [typeFilter, setTypeFilter] = useState<string>('');
    const [userId, setUserId] = useState<string>('');
    const [currentPage, setCurrentPage] = useState(0);
    const [errorMessage, setErrorMessage] = useState<string>('');

    const {
        transactions,
        loading,
        getAllTransactions,
        getUserTransactions,
        getTransactionsByType,
        totalPages,
        totalElements,
        pageSize
    } = useBonus();

    useEffect(() => {
        loadTransactions();
    }, [currentPage, typeFilter, userId]);

    const loadTransactions = async () => {
        try {
            setErrorMessage('');
            if (userId && typeFilter) {
                await getUserTransactions(parseInt(userId), { page: currentPage, size: 20 });
            } else if (typeFilter) {
                await getTransactionsByType(typeFilter as BonusTransactionType, { page: currentPage, size: 20 });
            } else {
                await getAllTransactions({ page: currentPage, size: 20 });
            }
        } catch (err) {
            setErrorMessage('Failed to load transactions');
        }
    };

    useEffect(() => {
        setCurrentPage(0);
    }, [typeFilter, userId]);

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    const handleTypeFilterChange = (value: string | number) => {
        setTypeFilter(String(value));
    };

    const handleUserIdChange = (value: string) => {
        setUserId(value);
    };

    const clearFilters = () => {
        setTypeFilter('');
        setUserId('');
        setCurrentPage(0);
    };

    const getTransactionTypeDisplay = (type: BonusTransactionType): string => {
        return BonusTransactionTypeDisplay[type] || type;
    };

    const getTransactionSummary = () => {
        let totalEarned = 0;
        let totalSpent = 0;

        transactions.forEach(transaction => {
            if (transaction.pointsChange > 0) {
                totalEarned += transaction.pointsChange;
            } else {
                totalSpent += Math.abs(transaction.pointsChange);
            }
        });

        return {
            totalEarned,
            totalSpent,
            netChange: totalEarned - totalSpent
        };
    };

    const typeOptions = [
        { value: '', label: 'All Types' },
        ...Object.entries(BonusTransactionTypeDisplay).map(([value, label]) => ({
            value,
            label
        }))
    ];

    const summary = getTransactionSummary();

    if (loading) {
        return <div className={styles.loading}>Loading transactions...</div>;
    }

    if (errorMessage) {
        return <div className={styles.error}>Error: {errorMessage}</div>;
    }

    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);

    return (
        <div className={styles.transactions}>
            <div className={styles.header}>
                <div>
                    <h2 className={styles.title}>Bonus Transactions History</h2>
                    <p className={styles.description}>
                        View all bonus points transactions in the system
                    </p>
                </div>
            </div>

            <div className={styles.filters}>
                <div className={styles.filterGroup}>
                    <label className={styles.filterLabel}>Transaction Type</label>
                    <Select
                        options={typeOptions}
                        value={typeFilter}
                        onChange={handleTypeFilterChange}
                        placeholder="Filter by type"
                    />
                </div>
                <div className={styles.filterGroup}>
                    <label className={styles.filterLabel}>User ID</label>
                    <Input
                        type="number"
                        value={userId}
                        onChange={handleUserIdChange}
                        placeholder="Enter user ID"
                        min="1"
                    />
                </div>
                <div style={{ alignSelf: 'flex-end' }}>
                    <Button
                        variant="secondary"
                        onClick={clearFilters}
                        disabled={!typeFilter && !userId}
                    >
                        Clear Filters
                    </Button>
                </div>
            </div>

            <div className={styles.stats}>
                <div className={styles.stat}>
                    <span className={styles.statValue}>{totalElements}</span>
                    <span className={styles.statLabel}>Total Transactions</span>
                </div>
                <div className={styles.stat}>
                    <span className={`${styles.statValue} ${styles.positive}`}>
                        +{summary.totalEarned}
                    </span>
                    <span className={styles.statLabel}>Points Earned</span>
                </div>
                <div className={styles.stat}>
                    <span className={`${styles.statValue} ${styles.negative}`}>
                        -{summary.totalSpent}
                    </span>
                    <span className={styles.statLabel}>Points Spent</span>
                </div>
                <div className={styles.stat}>
                    <span className={`${styles.statValue} ${summary.netChange >= 0 ? styles.positive : styles.negative}`}>
                        {summary.netChange >= 0 ? '+' : ''}{summary.netChange}
                    </span>
                    <span className={styles.statLabel}>Net Change</span>
                </div>
            </div>

            <div className={styles.tableContainer}>
                <table className={styles.table}>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Type</th>
                            <th>Points Change</th>
                            <th>New Balance</th>
                            <th>Reference ID</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {transactions.map((transaction) => (
                            <tr key={transaction.id}>
                                <td>{transaction.id}</td>
                                <td>
                                    <div className={styles.typeCell}>
                                        <span className={styles.typeName}>
                                            {getTransactionTypeDisplay(transaction.type)}
                                        </span>
                                        <span className={styles.typeCode}>
                                            {transaction.type}
                                        </span>
                                    </div>
                                </td>
                                <td>
                                    <span className={`${styles.points} ${transaction.pointsChange > 0
                                        ? styles.positivePoints
                                        : styles.negativePoints
                                        }`}>
                                        {transaction.pointsChange > 0 ? '+' : ''}{transaction.pointsChange}
                                    </span>
                                </td>
                                <td>
                                    <span className={styles.balance}>
                                        {transaction.newBalance}
                                    </span>
                                </td>
                                <td>
                                    {transaction.referenceId ? (
                                        <Badge variant="outline">
                                            {transaction.referenceId}
                                        </Badge>
                                    ) : (
                                        <span style={{ color: 'var(--text-secondary)' }}>N/A</span>
                                    )}
                                </td>
                                <td>
                                    <span className={styles.date}>
                                        {new Date(transaction.createdAt).toLocaleString()}
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {transactions.length === 0 && (
                <div className={styles.empty}>
                    <p>No transactions found</p>
                    {(typeFilter || userId) && (
                        <Button
                            variant="secondary"
                            onClick={clearFilters}
                            style={{ marginTop: '12px' }}
                        >
                            Clear filters to see all transactions
                        </Button>
                    )}
                </div>
            )}

            {totalPages > 1 && (
                <div className={styles.pagination}>
                    <div className={styles.pageInfo}>
                        Showing {startItem}-{endItem} of {totalElements} transactions
                    </div>
                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        totalElements={totalElements}
                        pageSize={pageSize}
                        onPageChange={handlePageChange}
                        variant="pages"
                        showInfo={false}
                    />
                </div>
            )}
        </div>
    );
};

export default BonusTransactions;
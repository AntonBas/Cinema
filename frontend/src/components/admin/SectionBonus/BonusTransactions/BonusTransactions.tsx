import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Pagination } from '@/components/ui/Pagination';
import { useAdminBonusTransactions } from '@/hooks/features/bonus/useAdminBonusTransactions';
import type { BonusTransactionType } from '@/types/bonus';
import { BonusTransactionTypeDisplay } from '@/types/bonus';
import styles from './BonusTransactions.module.css';

const BonusTransactions = () => {
    const [typeFilter, setTypeFilter] = useState<string>('');
    const [userId, setUserId] = useState<string>('');
    const [currentPage, setCurrentPage] = useState(0);

    const {
        transactions,
        loading,
        error,
        loadPage,
        getTransactionTypeDisplay,
        getTransactionSummary,
        totalPages,
        totalElements,
        pageSize
    } = useAdminBonusTransactions({
        type: typeFilter ? (typeFilter as BonusTransactionType) : undefined,
        userId: userId ? parseInt(userId) : undefined,
        initialPage: currentPage,
        pageSize: 20,
        autoFetch: true
    });

    useEffect(() => {
        setCurrentPage(0);
    }, [typeFilter, userId]);

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
        loadPage(page);
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

    if (error) {
        return <div className={styles.error}>Error: {error}</div>;
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
                            <th>Actions</th>
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
                                <td>
                                    <div className={styles.actions}>
                                        <Button
                                            variant="secondary"
                                            size="small"
                                            onClick={() => {
                                                console.log('View transaction:', transaction.id);
                                            }}
                                        >
                                            View
                                        </Button>
                                    </div>
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
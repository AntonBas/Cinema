import React from 'react';
import styles from './Pagination.module.css';

export type PaginationVariant = 'pages' | 'load-more';

export interface PaginationProps {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    pageSize: number;
    onPageChange: (page: number) => void;
    onLoadMore?: () => void;
    variant?: PaginationVariant;
    loading?: boolean;
    className?: string;
}

export const Pagination: React.FC<PaginationProps> = ({
    currentPage,
    totalPages,
    totalElements,
    pageSize,
    onPageChange,
    onLoadMore,
    variant = 'pages',
    loading = false,
    className = ''
}) => {
    if (totalPages <= 1) return null;

    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);

    const getPageNumbers = () => {
        const pages = [];
        const maxVisiblePages = 5;

        let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            pages.push(i);
        }

        return pages;
    };

    if (variant === 'load-more') {
        const hasMore = currentPage < totalPages - 1;

        if (!hasMore) return null;

        return (
            <div className={`${styles.pagination} ${className}`}>
                <div className={styles.info}>
                    Showing {endItem} of {totalElements} items
                </div>
                <button
                    className={styles.loadMoreButton}
                    onClick={onLoadMore}
                    disabled={loading}
                >
                    {loading ? (
                        <>
                            <span className={styles.spinner}></span>
                            Loading...
                        </>
                    ) : (
                        'Load More'
                    )}
                </button>
            </div>
        );
    }

    return (
        <div className={`${styles.pagination} ${className}`}>
            <div className={styles.info}>
                Showing {startItem}-{endItem} of {totalElements} items
            </div>

            <div className={styles.controls}>
                <button
                    className={styles.pageButton}
                    onClick={() => onPageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                >
                    ‹
                </button>

                {getPageNumbers().map(page => (
                    <button
                        key={page}
                        className={`${styles.pageButton} ${page === currentPage ? styles.active : ''}`}
                        onClick={() => onPageChange(page)}
                    >
                        {page + 1}
                    </button>
                ))}

                <button
                    className={styles.pageButton}
                    onClick={() => onPageChange(currentPage + 1)}
                    disabled={currentPage === totalPages - 1}
                >
                    ›
                </button>
            </div>
        </div>
    );
};
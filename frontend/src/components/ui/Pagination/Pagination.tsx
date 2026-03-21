import React from 'react';
import styles from './Pagination.module.css';

export type PaginationVariant = 'pages' | 'load-more';

export interface PaginationProps {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    pageSize: number;
    onLoadMore?: () => void;
    variant?: PaginationVariant;
    loading?: boolean;
    className?: string;
    showInfo?: boolean;
}

interface PagesPaginationProps extends Omit<PaginationProps, 'onLoadMore' | 'variant'> {
    variant?: 'pages';
    onPageChange: (page: number) => void;
}

interface LoadMorePaginationProps extends Omit<PaginationProps, 'onPageChange' | 'variant'> {
    variant: 'load-more';
    onLoadMore: () => void;
}

type CombinedPaginationProps = PagesPaginationProps | LoadMorePaginationProps;

export const Pagination: React.FC<CombinedPaginationProps> = (props) => {
    const {
        currentPage,
        totalPages,
        totalElements,
        variant = 'pages',
        loading = false,
        className = '' } = props;

    if (totalPages <= 0 || totalElements === 0) return null;

    const getPageNumbers = (): (number | string)[] => {
        const pages: (number | string)[] = [];
        const maxVisible = 5;

        if (totalPages <= maxVisible) {
            for (let i = 0; i < totalPages; i++) {
                pages.push(i);
            }
            return pages;
        }

        pages.push(0);

        let leftBound = currentPage - 2;
        let rightBound = currentPage + 2;

        if (leftBound <= 1) {
            leftBound = 1;
            rightBound = Math.min(maxVisible - 1, totalPages - 1);
        }

        if (rightBound >= totalPages - 2) {
            rightBound = totalPages - 1;
            leftBound = Math.max(totalPages - maxVisible, 1);
        }

        if (leftBound > 1) {
            pages.push('...');
        }

        for (let i = leftBound; i <= rightBound; i++) {
            if (i !== 0 && i !== totalPages - 1) {
                pages.push(i);
            }
        }

        if (rightBound < totalPages - 2) {
            pages.push('...');
        }

        if (totalPages - 1 !== pages[pages.length - 1]) {
            pages.push(totalPages - 1);
        }

        return pages;
    };

    if (variant === 'load-more') {
        const { onLoadMore } = props as LoadMorePaginationProps;
        const hasMore = currentPage < totalPages - 1 && totalPages > 1;

        if (!hasMore) return null;

        return (
            <div className={`${styles.pagination} ${className}`}>
                <button
                    className={styles.loadMoreButton}
                    onClick={onLoadMore}
                    disabled={loading}
                >
                    {loading ? 'Loading...' : 'Load More'}
                </button>
            </div>
        );
    }

    const { onPageChange } = props as PagesPaginationProps;

    return (
        <div className={`${styles.pagination} ${className}`}>
            <div className={styles.controls}>
                <button
                    className={styles.navButton}
                    onClick={() => onPageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="15 18 9 12 15 6"></polyline>
                    </svg>
                </button>

                <div className={styles.pages}>
                    {getPageNumbers().map((page, index) => {
                        if (page === '...') {
                            return <span key={`dots-${index}`} className={styles.dots}>⋯</span>;
                        }
                        const pageNum = page as number;
                        return (
                            <button
                                key={pageNum}
                                className={`${styles.pageButton} ${pageNum === currentPage ? styles.active : ''}`}
                                onClick={() => onPageChange(pageNum)}
                            >
                                {pageNum + 1}
                            </button>
                        );
                    })}
                </div>

                <button
                    className={styles.navButton}
                    onClick={() => onPageChange(currentPage + 1)}
                    disabled={currentPage === totalPages - 1}
                >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="9 18 15 12 9 6"></polyline>
                    </svg>
                </button>
            </div>
        </div>
    );
};
import { useState } from "react";

interface UsePaginationReturn {
    page: number;
    size: number;
    setPage: (page: number) => void;
    setSize: (size: number) => void;
    reset: () => void;
}

export const usePagination = (initialPage = 0, initialSize = 12): UsePaginationReturn => {
    const [page, setPage] = useState(initialPage);
    const [size, setSize] = useState(initialSize);

    const reset = () => {
        setPage(initialPage);
        setSize(initialSize);
    };

    return { page, size, setPage, setSize, reset };
};
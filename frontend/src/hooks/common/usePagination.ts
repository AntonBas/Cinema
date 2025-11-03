import { useState } from "react";

export const usePagination = (initialPage = 0, initialSize = 20) => {
    const [page, setPage] = useState(initialPage);
    const [size, setSize] = useState(initialSize);

    return { page, size, setPage, setSize };
};
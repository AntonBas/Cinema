import React, {
  useState,
  useEffect,
  useCallback,
  useMemo,
  useRef,
} from "react";
import type {
  MovieAdminResponse,
  MovieCardResponse,
  MovieStatus,
} from "@/types/movie";
import type { PageResponse } from "@/types/pagination";
import { useMovie } from "@/hooks/features/movie/useMovie";
import {
  parseEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { useNotification } from "@/context/NotificationContext";
import { isApiErrorException } from "@/utils/apiErrorHandler";
import { DEFAULT_PAGE_SIZE } from "@/utils/paginationUtils";
import { MovieList } from "./MovieList/MovieList";
import { MovieForm } from "./MovieForm/MovieForm";
import { DeleteConfirmModal } from "@/components/ui/DeleteConfirmModal/DeleteConfirmModal";
import { Button } from "@/components/ui/Button/Button";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { movieApi } from "@/api/movieApi";
import { Tabs, type TabItem } from "@/components/ui/Tabs/Tabs";
import styles from "./MovieTab.module.css";

type MovieTabType = "CURRENT" | "UPCOMING" | "ARCHIVED";

interface TabData {
  data: MovieCardResponse[];
  total: number;
  pagination: PageResponse<MovieCardResponse> | null;
}

const MOVIE_TABS: ReadonlyArray<TabItem<MovieTabType>> = [
  { id: "CURRENT", label: "Currently Showing" },
  { id: "UPCOMING", label: "Upcoming" },
  { id: "ARCHIVED", label: "Archived" },
];

const MOVIE_TAB_IDS = MOVIE_TABS.map((tab) => tab.id);

export const MovieTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieAdminResponse | null>(
    null,
  );
  const [deletingMovie, setDeletingMovie] = useState<MovieCardResponse | null>(
    null,
  );
  const [tabData, setTabData] = useState<Record<MovieTabType, TabData>>({
    CURRENT: { data: [], total: 0, pagination: null },
    UPCOMING: { data: [], total: 0, pagination: null },
    ARCHIVED: { data: [], total: 0, pagination: null },
  });
  const [loadingMovie, setLoadingMovie] = useState(false);

  const { page, query, getParam, setParams, setPage, setSearch } =
    useUrlParams();
  const activeTab = parseEnumParam(
    getParam("status"),
    MOVIE_TAB_IDS,
    "CURRENT",
  );
  const { loading: moviesLoading, remove } = useMovie();
  const { showNotification } = useNotification();
  const showLoading = useDelayedLoading(moviesLoading || loadingMovie, {
    delay: 150,
    minDisplayTime: 300,
  });

  const latestRequestRef = useRef<Record<MovieTabType, number>>({
    CURRENT: 0,
    UPCOMING: 0,
    ARCHIVED: 0,
  });

  const loadTabData = useCallback(
    async (tab: MovieTabType, page: number, search?: string) => {
      const requestId = ++latestRequestRef.current[tab];
      const isLatest = () => requestId === latestRequestRef.current[tab];

      try {
        const status = tab as MovieStatus;
        const requestParams = {
          page,
          size: DEFAULT_PAGE_SIZE,
          ...(search ? { query: search } : {}),
          status,
        };

        const response = await movieApi.admin.getAll(requestParams);
        if (!isLatest()) return;

        setTabData((prev) => ({
          ...prev,
          [tab]: {
            data: response?.data?.content || [],
            total: response?.data?.totalElements || 0,
            pagination: response?.data || null,
          },
        }));
      } catch (error) {
        if (!isLatest()) return;
        const message = isApiErrorException(error)
          ? error.message
          : `Failed to load ${tab.toLowerCase()} movies`;
        showNotification(message, "error");
      }
    },
    [showNotification],
  );

  const loadTabCount = useCallback(
    async (tab: MovieTabType, search?: string) => {
      try {
        const status = tab as MovieStatus;
        const response = await movieApi.admin.getAll({
          page: 0,
          size: 1,
          ...(search ? { query: search } : {}),
          status,
        });

        setTabData((prev) => ({
          ...prev,
          [tab]: {
            ...prev[tab],
            total: response?.data?.totalElements || 0,
          },
        }));
      } catch {
        return;
      }
    },
    [],
  );

  const loadAllTabCounts = useCallback(
    async (search?: string) => {
      await Promise.all([
        loadTabCount("CURRENT", search),
        loadTabCount("UPCOMING", search),
        loadTabCount("ARCHIVED", search),
      ]);
    },
    [loadTabCount],
  );

  useEffect(() => {
    loadAllTabCounts(query);
  }, [loadAllTabCounts, query]);

  useEffect(() => {
    loadTabData(activeTab, page, query);
  }, [activeTab, page, query, loadTabData]);

  const currentTabData = useMemo(
    () => tabData[activeTab],
    [tabData, activeTab],
  );

  const paginationInfo = useMemo(() => {
    const total = currentTabData.total;
    const start = total > 0 ? page * DEFAULT_PAGE_SIZE + 1 : 0;
    const end = Math.min(start + DEFAULT_PAGE_SIZE - 1, total);
    return { start, end };
  }, [currentTabData.total, page]);

  const handleSearch = useCallback(
    (query: string) => {
      setSearch(query);
    },
    [setSearch],
  );

  const handleTabChange = useCallback(
    (tab: MovieTabType) => {
      setParams({ status: toUrlEnumValue(tab, "CURRENT"), page: undefined });
    },
    [setParams],
  );

  const handlePageChange = useCallback(
    (page: number) => {
      setPage(page);
    },
    [setPage],
  );

  const handleEdit = useCallback(
    async (movie: MovieCardResponse) => {
      setLoadingMovie(true);
      try {
        const response = await movieApi.admin.getById(movie.id);
        if (response?.data) {
          setEditingMovie(response.data);
          setIsModalOpen(true);
        }
      } catch (error) {
        showNotification(
          isApiErrorException(error) ? error.message : "Failed to load movie",
          "error",
        );
      } finally {
        setLoadingMovie(false);
      }
    },
    [showNotification],
  );

  const handleDeleteClick = useCallback((movie: MovieCardResponse) => {
    setDeletingMovie(movie);
    setIsDeleteModalOpen(true);
  }, []);

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingMovie?.id) return;

    try {
      await remove(deletingMovie.id);
    } catch {
      setIsDeleteModalOpen(false);
      setDeletingMovie(null);
      return;
    }

    const newPage =
      currentTabData.data.length === 1 && page > 0 ? page - 1 : page;

    setPage(newPage);
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);

    await loadTabData(activeTab, newPage, query);
    await loadAllTabCounts(query);
  }, [
    deletingMovie,
    remove,
    activeTab,
    page,
    query,
    currentTabData.data.length,
    setPage,
    loadTabData,
    loadAllTabCounts,
  ]);

  const handleFormSuccess = useCallback(async () => {
    setIsModalOpen(false);
    setEditingMovie(null);
    await loadTabData(activeTab, page, query);
    await loadAllTabCounts(query);
  }, [activeTab, page, query, loadTabData, loadAllTabCounts]);

  const handleAddNew = useCallback(() => {
    setEditingMovie(null);
    setIsModalOpen(true);
  }, []);

  if (showLoading && !currentTabData.data.length && !query) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text={`Loading ${activeTab.toLowerCase()} movies...`} />
      </div>
    );
  }

  const tabCounts = {
    CURRENT: tabData.CURRENT.total,
    UPCOMING: tabData.UPCOMING.total,
    ARCHIVED: tabData.ARCHIVED.total,
  };

  return (
    <div className={styles.container}>
      <PageHeader
        title="Movies"
        subtitle="Manage movie catalog, posters, cast and crew"
        divider
        actions={
          <Button onClick={handleAddNew} variant="primary">
            Add Movie
          </Button>
        }
      />

      <div className={styles.searchContainer}>
        <SearchInput
          onSearch={handleSearch}
          value={query}
          placeholder="Search movies by title..."
          delay={300}
        />
      </div>

      <Tabs
        items={MOVIE_TABS.map((tab) => ({ ...tab, badge: tabCounts[tab.id] }))}
        activeId={activeTab}
        onChange={handleTabChange}
        ariaLabel="Movie status"
      />

      {currentTabData.total > 0 && (
        <div className={styles.resultsInfo}>
          Showing {paginationInfo.start}-{paginationInfo.end} of{" "}
          {currentTabData.total} movies
          {query && ` for "${query}"`}
        </div>
      )}

      <div className={styles.content}>
        <MovieList
          movies={currentTabData.data}
          onEdit={handleEdit}
          onDelete={handleDeleteClick}
          loading={moviesLoading && !currentTabData.data.length}
          onCreateNew={handleAddNew}
        />
      </div>

      {currentTabData.pagination &&
        currentTabData.pagination.totalPages > 1 && (
          <div className={styles.paginationContainer}>
            <Pagination
              currentPage={page}
              totalPages={currentTabData.pagination.totalPages}
              totalElements={currentTabData.total}
              pageSize={DEFAULT_PAGE_SIZE}
              onPageChange={handlePageChange}
              variant="pages"
              showInfo={false}
            />
          </div>
        )}

      {isModalOpen && (
        <MovieForm
          movie={editingMovie}
          onSuccess={handleFormSuccess}
          onCancel={() => setIsModalOpen(false)}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={() => setIsDeleteModalOpen(false)}
        itemName={deletingMovie?.title}
        itemType="movie"
        isDeleting={moviesLoading}
      />
    </div>
  );
};

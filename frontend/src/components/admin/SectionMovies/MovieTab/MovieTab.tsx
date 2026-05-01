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
import { useMovies } from "@/hooks/features/movies/useMovies";
import { usePagination } from "@/hooks/common/usePagination";
import { MovieList } from "./MovieList/MovieList";
import { MovieForm } from "./MovieForm/MovieForm";
import { DeleteConfirmModal } from "@/components/ui/DeleteConfirmModal/DeleteConfirmModal";
import { Button } from "@/components/ui/Button/Button";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { Badge } from "@/components/ui/Badge/Badge";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { movieApi } from "@/api/movieApi";
import styles from "./MovieTab.module.css";

type MovieTabType = "CURRENT" | "UPCOMING" | "ARCHIVED";

interface TabData {
  data: MovieCardResponse[];
  total: number;
  pagination: any;
}

export const MovieTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieAdminResponse | null>(
    null,
  );
  const [deletingMovie, setDeletingMovie] = useState<MovieCardResponse | null>(
    null,
  );
  const [activeTab, setActiveTab] = useState<MovieTabType>("CURRENT");
  const [tabData, setTabData] = useState<Record<MovieTabType, TabData>>({
    CURRENT: { data: [], total: 0, pagination: null },
    UPCOMING: { data: [], total: 0, pagination: null },
    ARCHIVED: { data: [], total: 0, pagination: null },
  });
  const [loadingMovie, setLoadingMovie] = useState(false);

  const { params, setPage, setSearch } = usePagination({ size: 12 });
  const { loading: moviesLoading, remove } = useMovies();
  const showLoading = useDelayedLoading(moviesLoading || loadingMovie, {
    delay: 150,
    minDisplayTime: 300,
  });

  const loadingDataRef = useRef<Record<MovieTabType, boolean>>({
    CURRENT: false,
    UPCOMING: false,
    ARCHIVED: false,
  });

  const loadTabData = useCallback(
    async (tab: MovieTabType, page: number, search?: string) => {
      if (loadingDataRef.current[tab]) return;

      loadingDataRef.current[tab] = true;

      try {
        const status = tab as MovieStatus;
        const requestParams = {
          page,
          size: 12,
          ...(search ? { query: search } : {}),
          status,
        };

        const response = await movieApi.admin.getMovies(requestParams);

        setTabData((prev) => ({
          ...prev,
          [tab]: {
            data: response?.data?.content || [],
            total: response?.data?.totalElements || 0,
            pagination: response?.data || null,
          },
        }));
      } catch (error) {
        console.error(`Failed to load ${tab} movies:`, error);
      } finally {
        loadingDataRef.current[tab] = false;
      }
    },
    [],
  );

  const loadAllTabCounts = useCallback(async () => {
    await Promise.all([
      loadTabData("CURRENT", 0, ""),
      loadTabData("UPCOMING", 0, ""),
      loadTabData("ARCHIVED", 0, ""),
    ]);
  }, [loadTabData]);

  useEffect(() => {
    loadAllTabCounts();
  }, []);

  useEffect(() => {
    loadTabData(activeTab, params.page || 0, params.query);
  }, [activeTab, params.page, params.query, loadTabData]);

  const currentTabData = useMemo(
    () => tabData[activeTab],
    [tabData, activeTab],
  );

  const handleSearch = useCallback(
    (query: string) => {
      setSearch(query);
    },
    [setSearch],
  );

  const handleTabChange = useCallback(
    (tab: MovieTabType) => {
      setActiveTab(tab);
      setPage(0);
    },
    [setPage],
  );

  const handlePageChange = useCallback(
    (page: number) => {
      setPage(page);
    },
    [setPage],
  );

  const handleEdit = useCallback(async (movie: MovieCardResponse) => {
    setLoadingMovie(true);
    try {
      const response = await movieApi.admin.getById(movie.id);
      if (response?.data) {
        setEditingMovie(response.data);
        setIsModalOpen(true);
      }
    } finally {
      setLoadingMovie(false);
    }
  }, []);

  const handleDeleteClick = useCallback((movie: MovieCardResponse) => {
    setDeletingMovie(movie);
    setIsDeleteModalOpen(true);
  }, []);

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingMovie?.id) return;

    await remove(deletingMovie.id);

    const newPage =
      currentTabData.data.length === 1 && params.page && params.page > 0
        ? params.page - 1
        : params.page || 0;

    setPage(newPage);
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);

    await loadTabData(activeTab, newPage, params.query);
    await loadAllTabCounts();
  }, [
    deletingMovie,
    remove,
    activeTab,
    params.page,
    params.query,
    currentTabData.data.length,
    setPage,
    loadTabData,
    loadAllTabCounts,
  ]);

  const handleFormSuccess = useCallback(async () => {
    setIsModalOpen(false);
    setEditingMovie(null);
    await loadTabData(activeTab, params.page || 0, params.query);
    await loadAllTabCounts();
  }, [activeTab, params.page, params.query, loadTabData, loadAllTabCounts]);

  const handleAddNew = useCallback(() => {
    setEditingMovie(null);
    setIsModalOpen(true);
  }, []);

  if (showLoading && !currentTabData.data.length && !params.query) {
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

  const paginationInfo = useMemo(() => {
    const total = currentTabData.total;
    const page = params.page || 0;
    const pageSize = params.size || 12;
    const start = total > 0 ? page * pageSize + 1 : 0;
    const end = Math.min(start + pageSize - 1, total);
    return { start, end };
  }, [currentTabData.total, params.page, params.size]);

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h2 className={styles.title}>Movie Management</h2>
          <p className={styles.description}>
            Manage movie catalog, posters, cast and crew
          </p>
        </div>
        <Button onClick={handleAddNew} variant="primary">
          Add Movie
        </Button>
      </div>

      <div className={styles.searchContainer}>
        <SearchInput
          onSearch={handleSearch}
          placeholder="Search movies by title..."
          delay={300}
        />
      </div>

      <div className={styles.tabs}>
        {(["CURRENT", "UPCOMING", "ARCHIVED"] as const).map((tab) => (
          <button
            key={tab}
            className={`${styles.tab} ${activeTab === tab ? styles.active : ""}`}
            onClick={() => handleTabChange(tab)}
          >
            <span className={styles.tabLabel}>
              {tab === "CURRENT"
                ? "Currently Showing"
                : tab === "UPCOMING"
                  ? "Upcoming"
                  : "Archived"}
            </span>
            <Badge variant={activeTab === tab ? "primary" : "secondary"}>
              {tabCounts[tab]}
            </Badge>
          </button>
        ))}
      </div>

      {currentTabData.total > 0 && (
        <div className={styles.resultsInfo}>
          Showing {paginationInfo.start}-{paginationInfo.end} of{" "}
          {currentTabData.total} movies
          {params.query && ` for "${params.query}"`}
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
              currentPage={params.page || 0}
              totalPages={currentTabData.pagination.totalPages}
              totalElements={currentTabData.total}
              pageSize={params.size || 12}
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

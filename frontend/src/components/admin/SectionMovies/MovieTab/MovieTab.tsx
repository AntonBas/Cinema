import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import type { MovieDetailResponse, MovieCardResponse } from '@/types/movie';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { useNotification } from '@/hooks/common/useNotification';
import { usePagination } from '@/hooks/common/usePagination';
import { MovieList } from './MovieList/MovieList';
import { MovieForm } from './MovieForm/MovieForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import { Button } from '@/components/ui/Button/Button';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { Badge } from '@/components/ui/Badge/Badge';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { Notification } from '@/components/ui/Notification/Notification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './MovieTab.module.css';

type MovieTabType = 'CURRENT' | 'UPCOMING' | 'ARCHIVED';

interface TabData {
  data: MovieCardResponse[];
  total: number;
  pagination: any;
}

export const MovieTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieDetailResponse | null>(null);
  const [deletingMovie, setDeletingMovie] = useState<MovieCardResponse | null>(null);
  const [activeTab, setActiveTab] = useState<MovieTabType>('CURRENT');
  const [tabData, setTabData] = useState<Record<MovieTabType, TabData>>({
    CURRENT: { data: [], total: 0, pagination: null },
    UPCOMING: { data: [], total: 0, pagination: null },
    ARCHIVED: { data: [], total: 0, pagination: null }
  });

  const { params, setPage, setSearch } = usePagination({ size: 12 });

  const {
    getAdminCurrent,
    getAdminUpcoming,
    getAdminArchived,
    getAdminById,
    remove,
    loading: moviesLoading
  } = useMovies();

  const { notifications, showNotification, hideNotification } = useNotification();
  const showLoading = useDelayedLoading(moviesLoading, { delay: 150, minDisplayTime: 300 });

  const searchTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const initialLoadRef = useRef(false);
  const loadingDataRef = useRef<Record<MovieTabType, boolean>>({
    CURRENT: false,
    UPCOMING: false,
    ARCHIVED: false
  });
  const previousParamsRef = useRef({ tab: activeTab, page: params.page, search: params.search });
  const isMountedRef = useRef(true);

  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  const currentTabData = useMemo(() =>
    tabData[activeTab],
    [activeTab, tabData]
  );

  const loadTabData = useCallback(async (tab: MovieTabType, page: number, search?: string) => {
    if (loadingDataRef.current[tab] || !isMountedRef.current) return;

    loadingDataRef.current[tab] = true;

    try {
      const baseParams = { page, size: 12 };
      const requestParams = search ? { ...baseParams, title: search } : baseParams;

      let response;
      switch (tab) {
        case 'CURRENT':
          response = await getAdminCurrent(requestParams);
          break;
        case 'UPCOMING':
          response = await getAdminUpcoming(requestParams);
          break;
        case 'ARCHIVED':
          response = await getAdminArchived(requestParams);
          break;
      }

      if (isMountedRef.current) {
        setTabData(prev => ({
          ...prev,
          [tab]: {
            data: response?.content || [],
            total: response?.totalElements || 0,
            pagination: response
          }
        }));
      }
    } catch (error) {
      if (isMountedRef.current) {
        if (isApiErrorException(error)) {
          showNotification(error.message, 'error');
        } else {
          showNotification(`Failed to load ${tab.toLowerCase()} movies`, 'error');
        }
      }
    } finally {
      if (isMountedRef.current) {
        loadingDataRef.current[tab] = false;
      }
    }
  }, [getAdminCurrent, getAdminUpcoming, getAdminArchived, showNotification]);

  const loadTabDataStable = useRef(loadTabData).current;

  const loadTabCounts = useCallback(async () => {
    try {
      await Promise.all([
        loadTabDataStable('CURRENT', 0, ''),
        loadTabDataStable('UPCOMING', 0, ''),
        loadTabDataStable('ARCHIVED', 0, '')
      ]);
    } catch (error) {
      console.error('Failed to load tab counts:', error);
    }
  }, [loadTabDataStable]);

  useEffect(() => {
    if (!initialLoadRef.current) {
      initialLoadRef.current = true;
      loadTabCounts();
    }
  }, [loadTabCounts]);

  useEffect(() => {
    if (!initialLoadRef.current) return;

    const currentParams = { tab: activeTab, page: params.page || 0, search: params.search };

    if (
      previousParamsRef.current.tab === currentParams.tab &&
      previousParamsRef.current.page === currentParams.page &&
      previousParamsRef.current.search === currentParams.search
    ) {
      return;
    }

    previousParamsRef.current = currentParams;

    const timer = setTimeout(() => {
      loadTabDataStable(activeTab, params.page || 0, params.search);
    }, 100);

    return () => clearTimeout(timer);
  }, [activeTab, params.page, params.search, loadTabDataStable]);

  const handleSearch = useCallback((query: string) => {
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(() => {
      setSearch(query);
    }, 300);
  }, [setSearch]);

  const handleTabChange = useCallback((tab: MovieTabType) => {
    setActiveTab(tab);
    setPage(0);
  }, [setPage]);

  const handlePageChange = useCallback((page: number) => {
    setPage(page);
  }, [setPage]);

  const handleEdit = useCallback(async (movie: MovieCardResponse) => {
    try {
      const response = await getAdminById(movie.id);
      if (response) {
        setEditingMovie(response);
        setIsModalOpen(true);
      }
    } catch (error) {
      showNotification(isApiErrorException(error) ? error.message : 'Failed to load movie details', 'error');
    }
  }, [getAdminById, showNotification]);

  const handleDeleteClick = useCallback((movie: MovieCardResponse) => {
    setDeletingMovie(movie);
    setIsDeleteModalOpen(true);
  }, []);

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingMovie?.id) return;

    try {
      await remove(deletingMovie.id);
      showNotification(`Movie "${deletingMovie.title}" deleted successfully!`, 'success');

      const newPage = currentTabData.data.length === 1 && params.page && params.page > 0
        ? params.page - 1
        : params.page || 0;

      setPage(newPage);
      await loadTabDataStable(activeTab, newPage, params.search);
    } catch (err) {
      if (isApiErrorException(err) && err.isConflict?.()) {
        showNotification(`Cannot delete movie "${deletingMovie.title}" because it has associated sessions.`, 'error');
      } else {
        showNotification(isApiErrorException(err) ? err.message : 'Failed to delete movie', 'error');
      }
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingMovie(null);
    }
  }, [deletingMovie, remove, activeTab, params.page, params.search, currentTabData.data.length, setPage, loadTabDataStable, showNotification]);

  const handleDeleteCancel = useCallback(() => {
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);
  }, []);

  const handleFormSuccess = useCallback((result?: MovieDetailResponse) => {
    setIsModalOpen(false);
    setEditingMovie(null);
    if (result) {
      showNotification(`Movie "${result.title}" successfully ${editingMovie ? 'updated' : 'created'}!`, 'success');
    }
    loadTabDataStable(activeTab, params.page || 0, params.search);
  }, [activeTab, params.page, params.search, loadTabDataStable, editingMovie, showNotification]);

  const handleFormCancel = useCallback(() => {
    setIsModalOpen(false);
    setEditingMovie(null);
  }, []);

  const handleAddNew = useCallback(() => {
    setEditingMovie(null);
    setIsModalOpen(true);
  }, []);

  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  const paginationInfo = useMemo(() => {
    const total = currentTabData.total;
    const page = params.page || 0;
    const pageSize = params.size || 12;
    const start = total > 0 ? page * pageSize + 1 : 0;
    const end = Math.min(start + pageSize - 1, total);
    const totalPages = Math.ceil(total / pageSize);

    return { total, start, end, totalPages, showPagination: totalPages > 1 };
  }, [currentTabData.total, params.page, params.size]);

  if (showLoading && !currentTabData.data.length && !params.search) {
    return <div className={styles.loading}><LoadingSpinner text={`Loading ${activeTab.toLowerCase()} movies...`} /></div>;
  }

  const tabCounts = {
    CURRENT: tabData.CURRENT.total,
    UPCOMING: tabData.UPCOMING.total,
    ARCHIVED: tabData.ARCHIVED.total
  };

  return (
    <div className={styles.container}>
      {notifications.map((notification) => (
        <Notification
          key={notification.id}
          id={notification.id}
          message={notification.message}
          type={notification.type}
          isVisible={notification.isVisible}
          onClose={hideNotification}
          duration={notification.duration}
        />
      ))}

      <div className={styles.header}>
        <h2 className={styles.title}>Movie Management</h2>
        <Button onClick={handleAddNew} variant="primary">Add Movie</Button>
      </div>

      <div className={styles.searchContainer}>
        <SearchInput onSearch={handleSearch} placeholder="Search movies by title..." delay={300} />
      </div>

      <div className={styles.tabs}>
        {(['CURRENT', 'UPCOMING', 'ARCHIVED'] as const).map((tab) => (
          <button
            key={tab}
            className={`${styles.tab} ${activeTab === tab ? styles.active : ''}`}
            onClick={() => handleTabChange(tab)}
          >
            <span className={styles.tabLabel}>
              {tab === 'CURRENT' ? 'Currently Showing' : tab === 'UPCOMING' ? 'Upcoming' : 'Archived'}
            </span>
            <Badge variant={activeTab === tab ? 'primary' : 'secondary'}>{tabCounts[tab]}</Badge>
          </button>
        ))}
      </div>

      {paginationInfo.total > 0 && (
        <div className={styles.resultsInfo}>
          Showing {paginationInfo.start}-{paginationInfo.end} of {paginationInfo.total} movies
          {params.search && ` for "${params.search}"`}
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

      {paginationInfo.showPagination && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={params.page || 0}
            totalPages={paginationInfo.totalPages}
            totalElements={paginationInfo.total}
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
          onCancel={handleFormCancel}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={deletingMovie?.title}
        itemType="movie"
        isDeleting={moviesLoading}
      />
    </div>
  );
};
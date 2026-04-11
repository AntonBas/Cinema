import React, { useState, useEffect, useCallback } from 'react';
import type { MovieCardResponse, MovieAdminResponse, MovieStatus } from '@/types/movie';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { usePagination } from '@/hooks/common/usePagination';
import { MovieList } from './MovieList/MovieList';
import { MovieForm } from './MovieForm/MovieForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import { Button } from '@/components/ui/Button/Button';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { Badge } from '@/components/ui/Badge/Badge';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { movieApi } from '@/api/movieApi';
import styles from './MovieTab.module.css';

type MovieTabType = 'CURRENT' | 'UPCOMING' | 'ARCHIVED';

export const MovieTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieAdminResponse | null>(null);
  const [deletingMovie, setDeletingMovie] = useState<MovieCardResponse | null>(null);
  const [activeTab, setActiveTab] = useState<MovieTabType>('CURRENT');
  const [loadingMovie, setLoadingMovie] = useState(false);

  const { params, setPage, setSearch } = usePagination({ size: 12 });
  const { adminMovies, adminPagination, loading, getAdminMovies, remove } = useMovies();
  const showLoading = useDelayedLoading(loading || loadingMovie, { delay: 150, minDisplayTime: 300 });

  useEffect(() => {
    getAdminMovies({
      page: params.page ?? 0,
      size: params.size,
      query: params.search,
      status: activeTab as MovieStatus,
    });
  }, [params.page, params.size, params.search, activeTab]);

  const handleSearch = useCallback((query: string) => {
    setSearch(query);
  }, [setSearch]);

  const handleTabChange = useCallback((tab: MovieTabType) => {
    setActiveTab(tab);
    setPage(0);
  }, [setPage]);

  const handlePageChange = useCallback((page: number) => {
    setPage(page);
  }, [setPage]);

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
    if (!deletingMovie) return;

    await remove(deletingMovie.id);

    const newPage = adminMovies.length === 1 && (params.page ?? 0) > 0
      ? (params.page ?? 0) - 1
      : params.page ?? 0;

    setPage(newPage);
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);
  }, [deletingMovie, adminMovies.length, params.page, setPage]);

  const handleFormSuccess = useCallback(() => {
    setIsModalOpen(false);
    setEditingMovie(null);
    getAdminMovies({
      page: params.page ?? 0,
      size: params.size,
      query: params.search,
      status: activeTab as MovieStatus,
    });
  }, [params.page, params.size, params.search, activeTab]);

  const handleAddNew = useCallback(() => {
    setEditingMovie(null);
    setIsModalOpen(true);
  }, []);

  if (showLoading && !adminMovies.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text={`Loading ${activeTab.toLowerCase()} movies...`} />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Movie Management</h2>
        <Button onClick={handleAddNew} variant="primary">Add Movie</Button>
      </div>

      <div className={styles.searchContainer}>
        <SearchInput onSearch={handleSearch} placeholder="Search movies by title..." delay={300} />
      </div>

      <div className={styles.tabs}>
        {(['CURRENT', 'UPCOMING', 'ARCHIVED'] as const).map(tab => (
          <button
            key={tab}
            className={`${styles.tab} ${activeTab === tab ? styles.active : ''}`}
            onClick={() => handleTabChange(tab)}
          >
            <span className={styles.tabLabel}>
              {tab === 'CURRENT' ? 'Currently Showing' : tab === 'UPCOMING' ? 'Upcoming' : 'Archived'}
            </span>
            <Badge variant={activeTab === tab ? 'primary' : 'secondary'}>
              {adminPagination?.totalElements || 0}
            </Badge>
          </button>
        ))}
      </div>

      {adminPagination && adminPagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {adminPagination.number * adminPagination.size + 1}-
          {Math.min((adminPagination.number + 1) * adminPagination.size, adminPagination.totalElements)} of{' '}
          {adminPagination.totalElements} movies
        </div>
      )}

      <div className={styles.content}>
        <MovieList
          movies={adminMovies}
          onEdit={handleEdit}
          onDelete={handleDeleteClick}
          loading={loading}
          onCreateNew={handleAddNew}
        />
      </div>

      {adminPagination && adminPagination.totalPages > 1 && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={adminPagination.number}
            totalPages={adminPagination.totalPages}
            totalElements={adminPagination.totalElements}
            pageSize={adminPagination.size}
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
        isDeleting={loading}
      />
    </div>
  );
};
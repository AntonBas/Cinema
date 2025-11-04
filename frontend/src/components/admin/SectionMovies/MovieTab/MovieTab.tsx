import React, { useState, useEffect } from 'react';
import type { MovieDto, MovieResponse } from '@/types/movie';
import { MovieStatus } from '@/types/movie';
import { useMovieSearch, useMovieMutation } from '@/hooks/features/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { MovieList } from './MovieList';
import { MovieForm } from './MovieForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { Notification } from '@/components/ui/Notification';
import styles from './MovieTab.module.css';

type MovieTabType = 'CURRENT' | 'UPCOMING' | 'ARCHIVED';

export const MovieTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieDto | null>(null);
  const [deletingMovie, setDeletingMovie] = useState<MovieDto | null>(null);
  const [activeTab, setActiveTab] = useState<MovieTabType>('CURRENT');
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [allMovies, setAllMovies] = useState<MovieDto[]>([]);

  const { movies, pagination, loading, error: searchError, searchMovies } = useMovieSearch();
  const { deleteMovie, loading: mutationLoading, error: mutationError } = useMovieMutation();
  const { notifications, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(searchQuery), 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  useEffect(() => {
    setAllMovies(movies);
  }, [movies]);

  useEffect(() => {
    loadMovies(true);
  }, [activeTab, debouncedSearch]);

  useEffect(() => {
    if (searchError) showNotification(searchError, 'error');
  }, [searchError, showNotification]);

  useEffect(() => {
    if (mutationError) showNotification(mutationError, 'error');
  }, [mutationError, showNotification]);

  const loadMovies = async (reset: boolean = false, pageOverride?: number) => {
    const page = reset ? 0 : (pageOverride ?? currentPage);

    const searchParams: any = {
      page,
      size: 10
    };

    if (activeTab !== 'CURRENT') {
      searchParams.status = activeTab;
    }

    if (debouncedSearch) {
      searchParams.search = debouncedSearch;
    }

    await searchMovies(searchParams);

    if (pagination) {
      setHasMore(!pagination.last);
      setCurrentPage(page);
    }
  };

  const loadMore = () => {
    if (!loading && hasMore) {
      const nextPage = currentPage + 1;
      loadMovies(false, nextPage);
    }
  };

  const handleTabChange = (tab: MovieTabType) => {
    setActiveTab(tab);
    setCurrentPage(0);
    setHasMore(true);
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
    setHasMore(true);
  };

  const clearSearch = () => {
    setSearchQuery('');
    setDebouncedSearch('');
    setCurrentPage(0);
    setHasMore(true);
  };

  const handleEdit = (movie: MovieResponse) => {
    const fullMovie = allMovies.find(m => m.id === movie.id);
    if (fullMovie) {
      setEditingMovie(fullMovie);
      setIsModalOpen(true);
    }
  };

  const handleDeleteClick = (movie: MovieResponse) => {
    const fullMovie = allMovies.find(m => m.id === movie.id);
    if (fullMovie) {
      setDeletingMovie(fullMovie);
      setIsDeleteModalOpen(true);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingMovie?.id) return;

    try {
      await deleteMovie(deletingMovie.id);
      showNotification('Movie deleted successfully!', 'success');
      loadMovies(true);
    } catch (err) {
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingMovie(null);
    }
  };

  const handleDeleteCancel = () => {
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);
  };

  const handleFormSuccess = () => {
    resetForm();
    loadMovies(true);
  };

  const resetForm = () => {
    setIsModalOpen(false);
    setEditingMovie(null);
  };

  const handleAddNew = () => {
    setEditingMovie(null);
    setIsModalOpen(true);
  };

  const filteredMovies = allMovies.filter(movie => {
    const matchesTab = movie.status === activeTab;
    const matchesSearch = debouncedSearch === '' ||
      movie.title.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
      movie.description.toLowerCase().includes(debouncedSearch.toLowerCase());
    return matchesTab && matchesSearch;
  });

  const movieResponses: MovieResponse[] = filteredMovies.map(movie => ({
    id: movie.id,
    title: movie.title,
    slug: movie.slug,
    posterUrl: movie.posterUrl,
    durationMinutes: movie.durationMinutes,
    ageRating: movie.ageRating,
    releaseDate: movie.releaseDate,
    status: movie.status,
    currentlyShowing: movie.currentlyShowing
  }));

  const getTabStats = () => {
    const stats = {
      CURRENT: allMovies.filter(m => m.status === MovieStatus.CURRENT).length,
      UPCOMING: allMovies.filter(m => m.status === MovieStatus.UPCOMING).length,
      ARCHIVED: allMovies.filter(m => m.status === MovieStatus.ARCHIVED).length
    };
    return stats;
  };

  const tabStats = getTabStats();

  const shouldShowLoadMore = hasMore &&
    !loading &&
    filteredMovies.length > 0 &&
    filteredMovies.length >= 10;

  if (loading && allMovies.length === 0) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading movies...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Movie Management</h2>
        <button
          className={styles.addButton}
          onClick={handleAddNew}
        >
          Add New Movie
        </button>
      </div>

      <div className={styles.searchContainer}>
        <div className={styles.searchWrapper}>
          <input
            type="text"
            placeholder="Search movies by title or description..."
            value={searchQuery}
            onChange={handleSearchChange}
            className={styles.searchInput}
          />
          {loading && <div className={styles.searchSpinner}></div>}
        </div>
        {searchQuery && (
          <button
            className={styles.clearSearch}
            onClick={clearSearch}
          >
            Clear
          </button>
        )}
      </div>

      {pagination && (
        <div className={styles.resultsInfo}>
          Showing {filteredMovies.length} of {pagination.totalElements} movies
          {debouncedSearch && ` for "${debouncedSearch}"`}
        </div>
      )}

      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${activeTab === 'CURRENT' ? styles.active : ''}`}
          onClick={() => handleTabChange('CURRENT')}
        >
          <span className={styles.tabLabel}>Currently Showing</span>
          <span className={styles.tabCount}>{tabStats.CURRENT}</span>
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'UPCOMING' ? styles.active : ''}`}
          onClick={() => handleTabChange('UPCOMING')}
        >
          <span className={styles.tabLabel}>Upcoming</span>
          <span className={styles.tabCount}>{tabStats.UPCOMING}</span>
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'ARCHIVED' ? styles.active : ''}`}
          onClick={() => handleTabChange('ARCHIVED')}
        >
          <span className={styles.tabLabel}>Archived</span>
          <span className={styles.tabCount}>{tabStats.ARCHIVED}</span>
        </button>
      </div>

      <div className={styles.tabContent}>
        <MovieList
          movies={movieResponses}
          onEdit={handleEdit}
          onDelete={handleDeleteClick}
        />
      </div>

      {shouldShowLoadMore && (
        <div className={styles.loadMoreContainer}>
          <button
            className={styles.loadMoreButton}
            onClick={loadMore}
            disabled={loading}
          >
            {loading ? (
              <>
                <div className={styles.loadingSpinnerSmall}></div>
                Loading...
              </>
            ) : (
              'Load More'
            )}
          </button>
        </div>
      )}

      {isModalOpen && (
        <MovieForm
          movie={editingMovie}
          onSuccess={handleFormSuccess}
          onCancel={resetForm}
          showNotification={showNotification}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={deletingMovie?.title}
        itemType="movie"
        isDeleting={mutationLoading}
      />

      {notifications.map((notification, index) => (
        <Notification
          key={notification.id}
          id={notification.id}
          message={notification.message}
          type={notification.type}
          isVisible={notification.isVisible}
          onClose={hideNotification}
          duration={4000}
          position={index}
        />
      ))}
    </div>
  );
};
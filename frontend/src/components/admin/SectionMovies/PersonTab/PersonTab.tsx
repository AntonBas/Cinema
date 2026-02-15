import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { PersonTabs } from './PersonTabs/PersonTabs';
import { PersonList } from './PersonList/PersonList';
import { PersonForm } from './PersonForm/PersonForm';
import { DeleteConfirmModal, Button, Pagination, LoadingSpinner } from '@/components/ui';
import { Notification } from '@/components/ui/Notification';
import { SearchInput } from '@/components/ui/SearchInput';
import { usePerson } from '@/hooks/features/persons/usePerson';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import type { PersonResponse, PersonRequest, PersonRole } from '@/types/person';
import styles from './PersonTab.module.css';

interface TabData {
  key: PersonRole | 'ALL';
  data: PersonResponse[];
  total: number;
}

export const PersonTab: React.FC = () => {
  const [activeTab, setActiveTab] = useState<PersonRole | 'ALL'>('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonResponse | null>(null);
  const [personToDelete, setPersonToDelete] = useState<PersonResponse | null>(null);
  const [totalCounts, setTotalCounts] = useState({
    ALL: 0,
    ACTOR: 0,
    DIRECTOR: 0,
    SCREENWRITER: 0,
  });

  const { notifications, showNotification, hideNotification } = useNotification();
  const { params, setPage, setSearch } = usePagination({ size: 12 });

  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const initialLoadRef = useRef(false);
  const loadingDataRef = useRef(false);

  const {
    allPersons,
    actors,
    directors,
    screenwriters,
    loading,
    getAll,
    getActors,
    getDirectors,
    getScreenwriters,
    create,
    update,
    remove
  } = usePerson();

  const showDelayedLoading = useDelayedLoading(loading, 300);

  const tabsData: Record<PersonRole | 'ALL', TabData> = useMemo(() => ({
    ALL: { key: 'ALL', data: allPersons, total: totalCounts.ALL },
    ACTOR: { key: 'ACTOR', data: actors, total: totalCounts.ACTOR },
    DIRECTOR: { key: 'DIRECTOR', data: directors, total: totalCounts.DIRECTOR },
    SCREENWRITER: { key: 'SCREENWRITER', data: screenwriters, total: totalCounts.SCREENWRITER }
  }), [allPersons, actors, directors, screenwriters, totalCounts]);

  const currentTabData = useMemo(() =>
    tabsData[activeTab] || tabsData.ALL,
    [activeTab, tabsData]
  );

  const loadData = useCallback(async (tab: PersonRole | 'ALL', page: number, search?: string) => {
    if (loadingDataRef.current) return;

    loadingDataRef.current = true;

    try {
      let loadMethod;
      switch (tab) {
        case 'ACTOR':
          loadMethod = getActors;
          break;
        case 'DIRECTOR':
          loadMethod = getDirectors;
          break;
        case 'SCREENWRITER':
          loadMethod = getScreenwriters;
          break;
        default:
          loadMethod = getAll;
      }

      await loadMethod({
        name: search?.trim() || undefined,
        page,
        size: 12
      });
    } catch (error) {
      if (isApiErrorException(error)) {
        showNotification(error.message, 'error');
      } else {
        showNotification('Failed to load data', 'error');
      }
    } finally {
      loadingDataRef.current = false;
    }
  }, [getAll, getActors, getDirectors, getScreenwriters, showNotification]);

  const loadCounts = useCallback(async () => {
    try {
      const allResult = await getAll({ page: 0, size: 1 });
      const actorsResult = await getActors({ page: 0, size: 1 });
      const directorsResult = await getDirectors({ page: 0, size: 1 });
      const screenwritersResult = await getScreenwriters({ page: 0, size: 1 });

      setTotalCounts({
        ALL: allResult?.totalElements || 0,
        ACTOR: actorsResult?.totalElements || 0,
        DIRECTOR: directorsResult?.totalElements || 0,
        SCREENWRITER: screenwritersResult?.totalElements || 0
      });
    } catch (error) {
      console.error('Failed to load counts:', error);
    }
  }, [getAll, getActors, getDirectors, getScreenwriters]);

  useEffect(() => {
    if (!initialLoadRef.current) {
      initialLoadRef.current = true;
      loadCounts();
      loadData(activeTab, 0, '');
    }
  }, []);

  useEffect(() => {
    if (initialLoadRef.current) {
      const timer = setTimeout(() => {
        loadData(activeTab, params.page || 0, params.search);
      }, 100);

      return () => clearTimeout(timer);
    }
  }, [activeTab, params.page, params.search]);

  const handleSearch = useCallback((query: string) => {
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(() => {
      setSearch(query);
    }, 300);
  }, [setSearch]);

  const handleTabChange = useCallback((tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
    setPage(0);
  }, [setPage]);

  const handleSubmit = useCallback(async (data: PersonRequest) => {
    try {
      const result = editingPerson
        ? await update(editingPerson.id, data)
        : await create(data);

      showNotification(
        `Person "${result.name}" ${editingPerson ? 'updated' : 'created'} successfully!`,
        'success'
      );

      setIsModalOpen(false);
      setEditingPerson(null);

      await Promise.all([
        loadCounts(),
        loadData(activeTab, params.page || 0, params.search)
      ]);
    } catch (err) {
      if (isApiErrorException(err)) {
        const validationError = err.getFirstValidationError();
        showNotification(validationError || err.message, 'error');
      } else {
        showNotification(err instanceof Error ? err.message : 'Failed to save person', 'error');
      }
    }
  }, [editingPerson, create, update, activeTab, params.page, params.search, showNotification, loadData, loadCounts]);

  const handleDelete = useCallback(async () => {
    if (!personToDelete) return;

    try {
      await remove(personToDelete.id);
      showNotification(`Person "${personToDelete.name}" deleted successfully!`, 'success');

      const newPage = currentTabData.data.length === 1 && params.page && params.page > 0
        ? params.page - 1
        : params.page || 0;

      setPage(newPage);

      await Promise.all([
        loadCounts(),
        loadData(activeTab, newPage, params.search)
      ]);
    } catch (err) {
      if (isApiErrorException(err) && err.isConflict()) {
        showNotification(`Cannot delete "${personToDelete.name}" - associated with movies`, 'error');
      } else {
        showNotification(err instanceof Error ? err.message : 'Failed to delete person', 'error');
      }
    } finally {
      setIsDeleteModalOpen(false);
      setPersonToDelete(null);
    }
  }, [personToDelete, remove, activeTab, params.page, params.search, currentTabData.data.length, setPage, showNotification, loadData, loadCounts]);

  const paginationInfo = useMemo(() => {
    const total = currentTabData.total;
    const page = params.page || 0;
    const pageSize = params.size || 12;
    const start = total > 0 ? page * pageSize + 1 : 0;
    const end = Math.min(start + pageSize - 1, total);
    const totalPages = Math.ceil(total / pageSize);

    return { total, start, end, totalPages, showPagination: totalPages > 1 };
  }, [currentTabData.total, params.page, params.size]);

  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  if (showDelayedLoading && !currentTabData.data.length && !params.search) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading persons" />
      </div>
    );
  }

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
        <h2>People Management</h2>
        <Button variant="primary" onClick={() => setIsModalOpen(true)}>
          Add Person
        </Button>
      </div>

      <div className={styles.searchSection}>
        <SearchInput
          onSearch={handleSearch}
          placeholder="Search people by name..."
          delay={300}
        />
      </div>

      {paginationInfo.total > 0 && (
        <div className={styles.resultsInfo}>
          Showing {paginationInfo.start}-{paginationInfo.end} of {paginationInfo.total} people
          {params.search && ` for "${params.search}"`}
        </div>
      )}

      <PersonTabs
        activeTab={activeTab}
        onTabChange={handleTabChange}
        stats={totalCounts}
      />

      <PersonList
        persons={currentTabData.data}
        activeTab={activeTab}
        onEdit={(person) => {
          setEditingPerson(person);
          setIsModalOpen(true);
        }}
        onDelete={(person) => {
          setPersonToDelete(person);
          setIsDeleteModalOpen(true);
        }}
        onAddPerson={() => setIsModalOpen(true)}
      />

      {paginationInfo.showPagination && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={params.page || 0}
            totalPages={paginationInfo.totalPages}
            totalElements={paginationInfo.total}
            pageSize={params.size || 12}
            onPageChange={setPage}
            variant="pages"
            loading={loading}
            showInfo={false}
          />
        </div>
      )}

      {isModalOpen && (
        <PersonForm
          person={editingPerson}
          onSubmit={handleSubmit}
          onCancel={() => {
            setIsModalOpen(false);
            setEditingPerson(null);
          }}
          isLoading={loading}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDelete}
        onCancel={() => {
          setIsDeleteModalOpen(false);
          setPersonToDelete(null);
        }}
        itemName={personToDelete?.name}
        itemType="person"
        isDeleting={loading}
      />
    </div>
  );
};
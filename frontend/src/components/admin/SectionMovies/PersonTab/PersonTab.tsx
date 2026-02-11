import React, { useState, useEffect, useCallback, useRef } from 'react';
import { PersonTabs } from './PersonTabs/PersonTabs';
import { PersonList } from './PersonList/PersonList';
import { PersonForm } from './PersonForm/PersonForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { Notification } from '@/components/ui/Notification';
import { SearchInput } from '@/components/ui/SearchInput';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';
import { usePerson } from '@/hooks/features/persons/usePerson';
import type { PersonResponse, PersonRequest, PersonRole } from '@/types/person';
import styles from './PersonTab.module.css';

export const PersonTab: React.FC = () => {
  const [activeTab, setActiveTab] = useState<PersonRole | 'ALL'>('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonResponse | null>(null);
  const [personToDelete, setPersonToDelete] = useState<PersonResponse | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [totalCounts, setTotalCounts] = useState({
    ALL: 0,
    ACTOR: 0,
    DIRECTOR: 0,
    SCREENWRITER: 0,
  });
  const [hasLoadedCounts, setHasLoadedCounts] = useState(false);
  const isMountedRef = useRef(true);

  const {
    allPersons,
    pagination,
    loading,
    getAll,
    getActors,
    getDirectors,
    getScreenwriters,
    create,
    update,
    remove
  } = usePerson();

  const getLoadMethod = useCallback(() => {
    if (activeTab === 'ALL') return getAll;
    if (activeTab === 'ACTOR') return getActors;
    if (activeTab === 'DIRECTOR') return getDirectors;
    return getScreenwriters;
  }, [activeTab, getAll, getActors, getDirectors, getScreenwriters]);

  const loadCounts = useCallback(async () => {
    if (!isMountedRef.current || searchQuery.trim() || hasLoadedCounts) return;

    try {
      const result = await getAll({ page: 0, size: 1 });

      if (isMountedRef.current) {
        setTotalCounts({
          ALL: result.totalElements || 0,
          ACTOR: 0,
          DIRECTOR: 0,
          SCREENWRITER: 0,
        });
        setHasLoadedCounts(true);
      }
    } catch (error) {
      console.error('Failed to load counts:', error);
    }
  }, [searchQuery, hasLoadedCounts, getAll]);

  const loadPersons = useCallback(async () => {
    try {
      const loadMethod = getLoadMethod();
      await loadMethod({
        name: searchQuery.trim() || undefined,
        page: currentPage,
        size: 12
      });

      if (searchQuery.trim() && isMountedRef.current) {
        setHasLoadedCounts(false);
      }
    } catch (error) {
      if (isMountedRef.current) {
        setErrorMessage('Failed to load persons');
      }
    }
  }, [activeTab, currentPage, searchQuery, getLoadMethod]);

  useEffect(() => {
    isMountedRef.current = true;

    const loadAllData = async () => {
      await loadPersons();
      if (!searchQuery.trim()) {
        await loadCounts();
      }
    };

    loadAllData();

    return () => {
      isMountedRef.current = false;
    };
  }, [activeTab, currentPage, searchQuery, loadPersons, loadCounts]);

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);
  }, []);

  const handleTabChange = (tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
    setCurrentPage(0);
    setSearchQuery('');
    if (isMountedRef.current) {
      setHasLoadedCounts(false);
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleSubmit = async (data: PersonRequest) => {
    try {
      if (editingPerson?.id) {
        await update(editingPerson.id, data);
        setSuccessMessage('Person updated successfully!');
      } else {
        await create(data);
        setSuccessMessage('Person created successfully!');
      }

      resetForm();
      if (isMountedRef.current) {
        setHasLoadedCounts(false);
      }

      await loadPersons();
      if (!searchQuery.trim()) {
        await loadCounts();
      }
    } catch (error) {
      setErrorMessage('Failed to save person');
    }
  };

  const handleEdit = (person: PersonResponse) => {
    setEditingPerson(person);
    setIsModalOpen(true);
  };

  const handleDeleteClick = (person: PersonResponse) => {
    setPersonToDelete(person);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!personToDelete?.id) return;
    try {
      await remove(personToDelete.id);
      setSuccessMessage('Person deleted successfully!');
      if (isMountedRef.current) {
        setHasLoadedCounts(false);
      }

      await loadPersons();
      if (!searchQuery.trim()) {
        await loadCounts();
      }
    } catch (error) {
      setErrorMessage('Failed to delete person');
    } finally {
      setIsDeleteModalOpen(false);
      setPersonToDelete(null);
    }
  };

  const handleDeleteCancel = () => {
    setIsDeleteModalOpen(false);
    setPersonToDelete(null);
  };

  const resetForm = () => {
    setIsModalOpen(false);
    setEditingPerson(null);
  };

  const handleAddNew = () => {
    setEditingPerson(null);
    setIsModalOpen(true);
  };

  const handleCloseNotification = () => {
    setErrorMessage('');
    setSuccessMessage('');
  };

  const getTabStats = () => {
    if (searchQuery.trim()) {
      const filtered = allPersons.filter(person =>
        activeTab === 'ALL' || person.role === activeTab
      );

      const actors = allPersons.filter(p => p.role === 'ACTOR');
      const directors = allPersons.filter(p => p.role === 'DIRECTOR');
      const screenwriters = allPersons.filter(p => p.role === 'SCREENWRITER');

      return {
        ALL: activeTab === 'ALL' ? filtered.length : allPersons.length,
        ACTOR: actors.length,
        DIRECTOR: directors.length,
        SCREENWRITER: screenwriters.length,
      };
    }

    return totalCounts;
  };

  const getDisplayRange = () => {
    if (!pagination) return { start: 0, end: 0 };

    const pageSize = pagination.size || 12;
    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, pagination.totalElements);

    return { start: startItem, end: endItem };
  };

  const tabStats = getTabStats();
  const { start, end } = getDisplayRange();

  const isLoading = loading;

  if (isLoading && !allPersons.length) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading persons...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      {successMessage && (
        <Notification
          id="success"
          message={successMessage}
          type="success"
          isVisible={true}
          onClose={handleCloseNotification}
          duration={4000}
        />
      )}

      {errorMessage && (
        <Notification
          id="error"
          message={errorMessage}
          type="error"
          isVisible={true}
          onClose={handleCloseNotification}
          duration={4000}
        />
      )}

      <div className={styles.header}>
        <h2>People Management</h2>
        <Button
          variant="primary"
          onClick={handleAddNew}
          className={styles.addButton}
        >
          Add Person
        </Button>
      </div>

      <div className={styles.searchSection}>
        <SearchInput
          onSearch={handleSearch}
          placeholder="Search people by name..."
          delay={300}
          className={styles.searchInput}
        />
      </div>

      {pagination && (
        <div className={styles.resultsInfo}>
          Showing {start}-{end} of {pagination.totalElements} people
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <PersonTabs
        activeTab={activeTab}
        onTabChange={handleTabChange}
        stats={tabStats}
      />

      <PersonList
        persons={allPersons}
        activeTab={activeTab}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
        onAddPerson={handleAddNew}
      />

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationContainer}>
          <Pagination
            currentPage={currentPage}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.size}
            onPageChange={handlePageChange}
            variant="pages"
            loading={isLoading}
            className={styles.pagination}
          />
        </div>
      )}

      {isModalOpen && (
        <PersonForm
          person={editingPerson}
          onSubmit={handleSubmit}
          onCancel={resetForm}
          isLoading={loading}
        />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={personToDelete?.name}
        itemType="person"
        isDeleting={loading}
      />
    </div>
  );
};
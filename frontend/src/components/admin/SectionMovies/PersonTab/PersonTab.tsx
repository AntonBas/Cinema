import React, { useState, useEffect, useCallback, useRef } from 'react';
import { PersonTabs } from './PersonTabs';
import { PersonList } from './PersonList';
import { PersonForm } from './PersonForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { Notification } from '@/components/ui/Notification';
import { SearchInput } from '@/components/ui/SearchInput';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';
import { usePerson } from '@/hooks/features/persons/usePerson';
import type { PersonResponse, PersonRequest, PersonRole } from '@/types/person';
import { PersonRoleEnum } from '@/types/person';
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
    [PersonRoleEnum.ACTOR]: 0,
    [PersonRoleEnum.DIRECTOR]: 0,
    [PersonRoleEnum.SCREENWRITER]: 0,
  });
  const [hasLoadedCounts, setHasLoadedCounts] = useState(false);
  const isMountedRef = useRef(true);

  const {
    persons,
    pagination,
    loading,
    searchPersons,
    getByRole,
    createPerson,
    updatePerson,
    deletePerson
  } = usePerson();

  const loadData = useCallback(async () => {
    try {
      if (searchQuery.trim()) {
        await searchPersons({
          query: searchQuery,
          role: activeTab === 'ALL' ? undefined : activeTab,
          page: currentPage,
          size: 12
        });
        setHasLoadedCounts(false);
      } else if (activeTab === 'ALL') {
        await searchPersons({
          page: currentPage,
          size: 12
        });

        if (isMountedRef.current && !hasLoadedCounts && !searchQuery.trim()) {
          const allResult = await searchPersons({ page: 0, size: 1 });
          const actorsResult = await getByRole(PersonRoleEnum.ACTOR, { page: 0, size: 1 });
          const directorsResult = await getByRole(PersonRoleEnum.DIRECTOR, { page: 0, size: 1 });
          const screenwritersResult = await getByRole(PersonRoleEnum.SCREENWRITER, { page: 0, size: 1 });

          setTotalCounts({
            ALL: allResult.totalElements,
            [PersonRoleEnum.ACTOR]: actorsResult.totalElements,
            [PersonRoleEnum.DIRECTOR]: directorsResult.totalElements,
            [PersonRoleEnum.SCREENWRITER]: screenwritersResult.totalElements,
          });
          setHasLoadedCounts(true);
        }
      } else {
        await getByRole(activeTab, {
          page: currentPage,
          size: 12
        });

        if (isMountedRef.current && !hasLoadedCounts && !searchQuery.trim()) {
          const allResult = await searchPersons({ page: 0, size: 1 });
          const actorsResult = await getByRole(PersonRoleEnum.ACTOR, { page: 0, size: 1 });
          const directorsResult = await getByRole(PersonRoleEnum.DIRECTOR, { page: 0, size: 1 });
          const screenwritersResult = await getByRole(PersonRoleEnum.SCREENWRITER, { page: 0, size: 1 });

          setTotalCounts({
            ALL: allResult.totalElements,
            [PersonRoleEnum.ACTOR]: actorsResult.totalElements,
            [PersonRoleEnum.DIRECTOR]: directorsResult.totalElements,
            [PersonRoleEnum.SCREENWRITER]: screenwritersResult.totalElements,
          });
          setHasLoadedCounts(true);
        }
      }
    } catch (error) {
      setErrorMessage('Failed to load persons');
    }
  }, [activeTab, currentPage, searchQuery, searchPersons, getByRole, hasLoadedCounts]);

  useEffect(() => {
    isMountedRef.current = true;
    loadData();

    return () => {
      isMountedRef.current = false;
    };
  }, [loadData]);

  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);
  }, []);

  const handleTabChange = (tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
    setCurrentPage(0);
    setSearchQuery('');
    setHasLoadedCounts(false);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleSubmit = async (data: PersonRequest) => {
    try {
      if (editingPerson?.id) {
        await updatePerson(editingPerson.id, data);
        setSuccessMessage('Person updated successfully!');
      } else {
        await createPerson(data);
        setSuccessMessage('Person created successfully!');
      }

      resetForm();
      setHasLoadedCounts(false);
      await loadData();
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
      await deletePerson(personToDelete.id);
      setSuccessMessage('Person deleted successfully!');
      setHasLoadedCounts(false);
      await loadData();
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
      const filtered = persons.filter(person =>
        activeTab === 'ALL' || person.role === activeTab
      );

      const actors = persons.filter(p => p.role === PersonRoleEnum.ACTOR);
      const directors = persons.filter(p => p.role === PersonRoleEnum.DIRECTOR);
      const screenwriters = persons.filter(p => p.role === PersonRoleEnum.SCREENWRITER);

      return {
        ALL: activeTab === 'ALL' ? filtered.length : persons.length,
        [PersonRoleEnum.ACTOR]: actors.length,
        [PersonRoleEnum.DIRECTOR]: directors.length,
        [PersonRoleEnum.SCREENWRITER]: screenwriters.length,
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

  if (loading && !persons.length) {
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
        persons={persons}
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
            loading={loading}
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
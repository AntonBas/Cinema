import React, { useState, useEffect } from 'react';
import { PersonTabs } from './PersonTabs';
import { PersonList } from './PersonList';
import { PersonForm } from './PersonForm';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { useNotification } from '@/hooks/useNotification';
import { Notification } from '@/components/ui/Notification';
import { personApi } from '@/api/personApi';
import type { PersonDto, PersonFormData } from '@/types/Person';
import { PersonRole } from '@/types/Person';
import styles from './PersonTab.module.css';

export const PersonTab: React.FC = () => {
  const [persons, setPersons] = useState<PersonDto[]>([]);
  const [filteredPersons, setFilteredPersons] = useState<PersonDto[]>([]);
  const [activeTab, setActiveTab] = useState<PersonRole | 'ALL'>('ALL');
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonDto | null>(null);
  const [personToDelete, setPersonToDelete] = useState<PersonDto | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalCounts, setTotalCounts] = useState({
    ALL: 0,
    [PersonRole.ACTOR]: 0,
    [PersonRole.DIRECTOR]: 0,
    [PersonRole.SCREENWRITER]: 0,
  });

  const { notifications, showNotification, hideNotification } = useNotification();

  // --- Load persons and counts on mount and tab change ---
  useEffect(() => {
    loadPersons(true);
    loadCounts(); // окремо рахуємо статистику
  }, [activeTab]);

  useEffect(() => {
    filterPersons();
  }, [persons, activeTab]);

  // --- Load paginated persons ---
  const loadPersons = async (reset: boolean = false, pageOverride?: number) => {
    try {
      const page = reset ? 0 : (pageOverride ?? currentPage);
      const role = activeTab === 'ALL' ? undefined : activeTab;

      if (reset) {
        setIsLoading(true);
        setPersons([]);
        setHasMore(true);
      } else {
        setIsLoadingMore(true);
      }

      const result = await personApi.search({
        query: '',
        role,
        page,
        size: 10,
      });

      if (reset) {
        setPersons(result.content);
        setCurrentPage(0);
      } else {
        setPersons((prev) => [...prev, ...result.content]);
        setCurrentPage(page);
      }

      setHasMore(result.currentPage < result.totalPages - 1);

      if (reset) {
        setTotalCounts((prev) => ({
          ...prev,
          [activeTab]: result.totalElements,
        }));
      }
    } catch (err) {
      showNotification('Failed to load persons', 'error');
      console.error('Error loading persons:', err);
    } finally {
      setIsLoading(false);
      setIsLoadingMore(false);
    }
  };

  // --- Load more handler ---
  const loadMore = () => {
    if (!isLoadingMore && hasMore) {
      const nextPage = currentPage + 1;
      loadPersons(false, nextPage);
    }
  };

  // --- Load total counts for all roles ---
  const loadCounts = async () => {
    try {
      const roles = [undefined, PersonRole.ACTOR, PersonRole.DIRECTOR, PersonRole.SCREENWRITER];
      const counts: Record<string, number> = {};

      for (const role of roles) {
        const result = await personApi.search({ query: '', role, page: 0, size: 1 });
        counts[role ?? 'ALL'] = result.totalElements;
      }

      setTotalCounts({
        ALL: counts.ALL,
        [PersonRole.ACTOR]: counts[PersonRole.ACTOR],
        [PersonRole.DIRECTOR]: counts[PersonRole.DIRECTOR],
        [PersonRole.SCREENWRITER]: counts[PersonRole.SCREENWRITER],
      });
    } catch (err) {
      console.error('Error loading counts:', err);
    }
  };

  // --- Filter persons by tab ---
  const filterPersons = () => {
    if (activeTab === 'ALL') {
      setFilteredPersons(persons);
    } else {
      setFilteredPersons(persons.filter((person) => person.role === activeTab));
    }
  };

  // --- Tab change handler ---
  const handleTabChange = (tab: PersonRole | 'ALL') => {
    setActiveTab(tab);
  };

  // --- Form submission (create / update) ---
  const handleSubmit = async (data: PersonFormData) => {
    try {
      if (editingPerson?.id) {
        await personApi.update(editingPerson.id, data);
        showNotification('Person updated successfully!', 'success');
      } else {
        await personApi.create(data);
        showNotification('Person created successfully!', 'success');
      }
      resetForm();
      loadPersons(true);
      loadCounts();
    } catch (err) {
      showNotification('Failed to save person', 'error');
      console.error('Error saving person:', err);
    }
  };

  // --- Edit person ---
  const handleEdit = (person: PersonDto) => {
    setEditingPerson(person);
    setIsModalOpen(true);
  };

  // --- Delete person flow ---
  const handleDeleteClick = (person: PersonDto) => {
    setPersonToDelete(person);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!personToDelete?.id) return;

    try {
      setIsDeleting(true);
      await personApi.delete(personToDelete.id);
      showNotification('Person deleted successfully!', 'success');
      loadPersons(true);
      loadCounts();
    } catch (err) {
      showNotification('Failed to delete person', 'error');
      console.error('Error deleting person:', err);
    } finally {
      setIsDeleting(false);
      setIsDeleteModalOpen(false);
      setPersonToDelete(null);
    }
  };

  const handleDeleteCancel = () => {
    setIsDeleteModalOpen(false);
    setPersonToDelete(null);
  };

  // --- Reset form state ---
  const resetForm = () => {
    setIsModalOpen(false);
    setEditingPerson(null);
  };

  const getTabStats = () => ({
    ALL: totalCounts.ALL,
    [PersonRole.ACTOR]: totalCounts[PersonRole.ACTOR],
    [PersonRole.DIRECTOR]: totalCounts[PersonRole.DIRECTOR],
    [PersonRole.SCREENWRITER]: totalCounts[PersonRole.SCREENWRITER],
  });

  const handleAddNew = () => {
    setEditingPerson(null);
    setIsModalOpen(true);
  };

  // --- Loading state ---
  if (isLoading) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading persons...</p>
      </div>
    );
  }

  // --- Render ---
  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2>People Management</h2>
        <button className={styles.primaryButton} onClick={handleAddNew}>
          <span className={styles.buttonIcon}>+</span>
          Add Person
        </button>
      </div>

      <PersonTabs activeTab={activeTab} onTabChange={handleTabChange} stats={getTabStats()} />

      <PersonList
        persons={filteredPersons}
        activeTab={activeTab}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
        onAddPerson={handleAddNew}
      />

      {hasMore && (
        <div className={styles.loadMoreContainer}>
          <button
            className={styles.loadMoreButton}
            onClick={loadMore}
            disabled={isLoadingMore}
          >
            {isLoadingMore ? (
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
        <PersonForm person={editingPerson} onSubmit={handleSubmit} onCancel={resetForm} />
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={personToDelete?.name}
        itemType="person"
        isDeleting={isDeleting}
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

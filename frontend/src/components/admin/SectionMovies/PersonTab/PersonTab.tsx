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
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingPerson, setEditingPerson] = useState<PersonDto | null>(null);
  const [personToDelete, setPersonToDelete] = useState<PersonDto | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const { notifications, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    loadPersons();
  }, []);

  useEffect(() => {
    filterPersons();
  }, [persons, activeTab]);

  const loadPersons = async () => {
    try {
      setIsLoading(true);
      const data = await personApi.getAll();
      setPersons(data);
    } catch (err) {
      showNotification('Failed to load persons', 'error');
      console.error('Error loading persons:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const filterPersons = () => {
    if (activeTab === 'ALL') {
      setFilteredPersons(persons);
    } else {
      setFilteredPersons(persons.filter(person => person.role === activeTab));
    }
  };

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
      loadPersons();
    } catch (err) {
      showNotification('Failed to save person', 'error');
      console.error('Error saving person:', err);
    }
  };

  const handleEdit = (person: PersonDto) => {
    setEditingPerson(person);
    setIsModalOpen(true);
  };

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
      loadPersons();
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

  const resetForm = () => {
    setIsModalOpen(false);
    setEditingPerson(null);
  };

  const getTabStats = () => {
    return {
      ALL: persons.length,
      [PersonRole.ACTOR]: persons.filter(p => p.role === PersonRole.ACTOR).length,
      [PersonRole.DIRECTOR]: persons.filter(p => p.role === PersonRole.DIRECTOR).length,
      [PersonRole.SCREENWRITER]: persons.filter(p => p.role === PersonRole.SCREENWRITER).length,
    };
  };

  const handleAddNew = () => {
    setEditingPerson(null);
    setIsModalOpen(true);
  };

  if (isLoading) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading persons...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2>People Management</h2>
        <button
          className={styles.primaryButton}
          onClick={handleAddNew}
        >
          <span className={styles.buttonIcon}>+</span>
          Add Person
        </button>
      </div>

      <PersonTabs
        activeTab={activeTab}
        onTabChange={setActiveTab}
        stats={getTabStats()}
      />

      <PersonList
        persons={filteredPersons}
        activeTab={activeTab}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
        onAddPerson={handleAddNew}
      />

      {isModalOpen && (
        <PersonForm
          person={editingPerson}
          onSubmit={handleSubmit}
          onCancel={resetForm}
        />
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
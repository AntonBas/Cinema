import React, { useState, useEffect } from 'react';
import { type PersonDto, type PersonFormData, PersonRole } from '@/types/Person';
import { personApi } from '@/api/personApi';
import { Notification } from '@/components/ui/Notification/Notification';
import { useNotification } from '@/hooks/useNotification';
import styles from './PersonTab.module.css';

export const PersonTab: React.FC = () => {
  const [persons, setPersons] = useState<PersonDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [personToDelete, setPersonToDelete] = useState<PersonDto | null>(null);
  const [editingPerson, setEditingPerson] = useState<PersonDto | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const { notifications, showNotification, hideNotification } = useNotification();

  const [formData, setFormData] = useState<PersonFormData>({
    name: '',
    role: PersonRole.ACTOR
  });

  useEffect(() => {
    loadPersons();
  }, []);

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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      if (editingPerson?.id) {
        await personApi.update(editingPerson.id, formData);
        showNotification('Person updated successfully!', 'success');
      } else {
        await personApi.create(formData);
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
    setFormData({
      name: person.name,
      role: person.role
    });
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
    setFormData({
      name: '',
      role: PersonRole.ACTOR
    });
  };

  const getRoleIcon = (role: PersonRole) => {
    switch (role) {
      case PersonRole.ACTOR: return '🎭';
      case PersonRole.DIRECTOR: return '🎬';
      case PersonRole.SCREENWRITER: return '✍️';
      default: return '👤';
    }
  };

  const getRoleColor = (role: PersonRole) => {
    switch (role) {
      case PersonRole.ACTOR: return '#4CAF50';
      case PersonRole.DIRECTOR: return '#2196F3';
      case PersonRole.SCREENWRITER: return '#FF9800';
      default: return '#6b7280';
    }
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
          onClick={() => setIsModalOpen(true)}
        >
          <span className={styles.buttonIcon}>+</span>
          Add Person
        </button>
      </div>

      <div className={styles.grid}>
        {persons.length === 0 ? (
          <div className={styles.empty}>
            <div className={styles.emptyIcon}>👥</div>
            <h3>No people found</h3>
            <p>Get started by adding your first person</p>
            <button
              className={styles.primaryButton}
              onClick={() => setIsModalOpen(true)}
            >
              Add First Person
            </button>
          </div>
        ) : (
          persons.map(person => (
            <div key={person.id} className={styles.card}>
              <div className={styles.info}>
                <div className={styles.header}>
                  <span
                    className={styles.roleIcon}
                    style={{ color: getRoleColor(person.role) }}
                  >
                    {getRoleIcon(person.role)}
                  </span>
                  <h3 className={styles.name}>{person.name}</h3>
                </div>
                <div className={styles.details}>
                  <span
                    className={styles.roleBadge}
                    style={{
                      background: `${getRoleColor(person.role)}20`,
                      color: getRoleColor(person.role),
                      border: `1px solid ${getRoleColor(person.role)}`
                    }}
                  >
                    {person.role.toLowerCase()}
                  </span>
                </div>
              </div>
              <div className={styles.actions}>
                <button
                  className={styles.editButton}
                  onClick={() => handleEdit(person)}
                >
                  Edit
                </button>
                <button
                  className={styles.deleteButton}
                  onClick={() => handleDeleteClick(person)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {isModalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h3>{editingPerson ? 'Edit Person' : 'Add New Person'}</h3>
            <form onSubmit={handleSubmit} className={styles.form}>
              <div className={styles.formGroup}>
                <label>Full Name *</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Enter full name"
                  required
                  className={styles.input}
                />
              </div>

              <div className={styles.formGroup}>
                <label>Role *</label>
                <select
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value as PersonRole })}
                  required
                  className={styles.select}
                >
                  <option value={PersonRole.ACTOR}>Actor 🎭</option>
                  <option value={PersonRole.DIRECTOR}>Director 🎬</option>
                  <option value={PersonRole.SCREENWRITER}>Screenwriter ✍️</option>
                </select>
              </div>

              <div className={styles.formActions}>
                <button type="submit" className={styles.primaryButton}>
                  {editingPerson ? 'Update' : 'Create'} Person
                </button>
                <button type="button" onClick={resetForm} className={styles.cancelButton}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {isDeleteModalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.confirmModal}>
            <div className={styles.confirmIcon}>🗑️</div>
            <h3 className={styles.confirmTitle}>Delete Person</h3>
            <p className={styles.confirmMessage}>
              Are you sure you want to delete
            </p>
            <p className={styles.confirmWarning}>
              "{personToDelete?.name}"?
            </p>
            <p className={styles.confirmMessage}>
              This action cannot be undone.
            </p>
            <div className={styles.confirmActions}>
              <button
                className={styles.cancelConfirmButton}
                onClick={handleDeleteCancel}
                disabled={isDeleting}
              >
                Cancel
              </button>
              <button
                className={styles.deleteConfirmButton}
                onClick={handleDeleteConfirm}
                disabled={isDeleting}
              >
                {isDeleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}

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
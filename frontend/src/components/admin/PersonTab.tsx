import React, { useState, useEffect } from 'react';
import { type PersonDto, type PersonFormData, PersonRole } from '../../types/Person';
import { personApi } from '../../api/personApi';
import { Notification } from '../ui/Notification';
import { useNotification } from '../../hooks/useNotification';
import './PersonTab.css';

export const PersonTab: React.FC = () => {
  const [persons, setPersons] = useState<PersonDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [personToDelete, setPersonToDelete] = useState<PersonDto | null>(null);
  const [editingPerson, setEditingPerson] = useState<PersonDto | null>(null);

  const { notification, showSuccess, showError, hideNotification } = useNotification();

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
      showError('Failed to load persons');
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
        showSuccess('Person updated successfully!');
      } else {
        await personApi.create(formData);
        showSuccess('Person created successfully!');
      }
      resetForm();
      loadPersons();
    } catch (err) {
      showError('Failed to save person');
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
      await personApi.delete(personToDelete.id);
      showSuccess('Person deleted successfully!');
      loadPersons();
    } catch (err) {
      showError('Failed to delete person');
      console.error('Error deleting person:', err);
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
      <div className="loading">
        <div className="loading-spinner"></div>
        <p>Loading persons...</p>
      </div>
    );
  }

  return (
    <div className="person-tab">
      <Notification
        message={notification.message}
        type={notification.type}
        isVisible={notification.isVisible}
        onClose={hideNotification}
        duration={4000}
      />

      <div className="tab-header">
        <h2 className="tab-title">People Management</h2>
        <button
          className="btn-primary"
          onClick={() => setIsModalOpen(true)}
        >
          <span className="btn-icon">+</span>
          Add Person
        </button>
      </div>

      <div className="persons-grid">
        {persons.map(person => (
          <div key={person.id} className="person-card">
            <div className="person-info">
              <div className="person-header">
                <span
                  className="person-role-icon"
                  style={{ color: getRoleColor(person.role) }}
                >
                  {getRoleIcon(person.role)}
                </span>
                <h3 className="person-name">{person.name}</h3>
              </div>
              <div className="person-details">
                <span
                  className="person-role-badge"
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
            <div className="person-actions">
              <button
                className="btn-edit"
                onClick={() => handleEdit(person)}
              >
                Edit
              </button>
              <button
                className="btn-delete"
                onClick={() => handleDeleteClick(person)}
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>

      {persons.length === 0 && !isLoading && (
        <div className="empty-state">
          <div className="empty-icon">👥</div>
          <h3>No people found</h3>
          <p>Get started by adding your first person</p>
          <button
            className="btn-primary"
            onClick={() => setIsModalOpen(true)}
          >
            Add First Person
          </button>
        </div>
      )}

      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>{editingPerson ? 'Edit Person' : 'Add New Person'}</h2>

            <form onSubmit={handleSubmit} className="person-form">
              <div className="form-group">
                <label htmlFor="name">Full Name *</label>
                <input
                  type="text"
                  id="name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Enter full name"
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="role">Role *</label>
                <select
                  id="role"
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value as PersonRole })}
                  required
                >
                  <option value={PersonRole.ACTOR}>Actor 🎭</option>
                  <option value={PersonRole.DIRECTOR}>Director 🎬</option>
                  <option value={PersonRole.SCREENWRITER}>Screenwriter ✍️</option>
                </select>
              </div>

              <div className="form-actions">
                <button type="submit" className="btn-primary">
                  {editingPerson ? 'Update' : 'Create'} Person
                </button>
                <button type="button" onClick={resetForm} className="btn-cancel">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {isDeleteModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content delete-modal">
            <div className="delete-modal-icon">🗑️</div>
            <h2>Delete Person</h2>
            <p>Are you sure you want to delete <strong>"{personToDelete?.name}"</strong>?</p>
            <p className="delete-warning">This action cannot be undone.</p>

            <div className="delete-modal-actions">
              <button
                className="btn-delete-confirm"
                onClick={handleDeleteConfirm}
              >
                Yes, Delete
              </button>
              <button
                className="btn-cancel"
                onClick={handleDeleteCancel}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
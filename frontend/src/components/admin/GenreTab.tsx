import React, { useState, useEffect } from 'react';
import type { GenreDto, GenreFormData } from '../../types/Genre';
import { genreApi } from '../../api/genreApi';

export const GenreTab: React.FC = () => {
  const [genres, setGenres] = useState<GenreDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreDto | null>(null);
  const [formData, setFormData] = useState<GenreFormData>({ name: '' });

  useEffect(() => {
    loadGenres();
  }, []);

  const loadGenres = async () => {
    try {
      const data = await genreApi.getAll();
      setGenres(data);
    } catch (error) {
      console.error('Error loading genres:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingGenre?.id) {
        await genreApi.update(editingGenre.id, formData);
      } else {
        await genreApi.create(formData);
      }
      resetForm();
      loadGenres();
    } catch (error) {
      console.error('Error saving genre:', error);
      alert('Error saving genre. Check console for details.');
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this genre?')) {
      try {
        await genreApi.delete(id);
        loadGenres();
      } catch (error) {
        console.error('Error deleting genre:', error);
        alert('Cannot delete genre. It might be used in movies.');
      }
    }
  };

  const resetForm = () => {
    setIsModalOpen(false);
    setEditingGenre(null);
    setFormData({ name: '' });
  };

  const handleEdit = (genre: GenreDto) => {
    setEditingGenre(genre);
    setFormData({ name: genre.name });
    setIsModalOpen(true);
  };

  if (isLoading) return <div className="loading">Loading genres...</div>;

  return (
    <div className="tab-content-inner">
      <div className="tab-header">
        <h2>Genre Management</h2>
        <button className="btn-primary" onClick={() => setIsModalOpen(true)}>
          Add New Genre
        </button>
      </div>

      <div className="genres-grid">
        {genres.length === 0 ? (
          <div className="empty-state">
            <p>No genres found. Create your first genre!</p>
          </div>
        ) : (
          genres.map(genre => (
            <div key={genre.id} className="genre-card">
              <div className="genre-info">
                <h3 className="genre-name">{genre.name}</h3>
                {genre.movies && (
                  <span className="movie-count">
                    {genre.movies.length} movie{genre.movies.length !== 1 ? 's' : ''}
                  </span>
                )}
              </div>
              <div className="genre-actions">
                <button
                  className="btn-edit"
                  onClick={() => handleEdit(genre)}
                >
                  Edit
                </button>
                <button
                  className="btn-delete"
                  onClick={() => genre.id && handleDelete(genre.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>{editingGenre ? 'Edit Genre' : 'Add New Genre'}</h3>
            <form onSubmit={handleSubmit} className="genre-form">
              <div className="form-group">
                <input
                  type="text"
                  placeholder="Genre name (e.g., Action, Drama, Comedy)"
                  value={formData.name}
                  onChange={(e) => setFormData({ name: e.target.value })}
                  required
                  maxLength={50}
                />
                <small>Maximum 50 characters</small>
              </div>

              <div className="form-actions">
                <button type="submit" className="btn-primary">
                  {editingGenre ? 'Update' : 'Create'} Genre
                </button>
                <button type="button" onClick={resetForm}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
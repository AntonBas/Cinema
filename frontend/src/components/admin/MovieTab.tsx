import React, { useState, useEffect, useRef } from 'react';
import { type MovieDto, type MovieFormData, MovieStatus, AgeRating } from '../../types/Movie';
import { movieApi } from '../../api/movieApi';

export const MovieTab: React.FC = () => {
  const [movies, setMovies] = useState<MovieDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieDto | null>(null);
  const [posterPreview, setPosterPreview] = useState<string>('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [formData, setFormData] = useState<MovieFormData>({
    title: '',
    slug: '',
    trailer: '',
    description: '',
    production: '',
    durationMinutes: 0,
    releaseDate: '',
    endShowingDate: '',
    status: MovieStatus.ACTIVE,
    ageRating: AgeRating.G,
  });

  useEffect(() => {
    loadMovies();
  }, []);

  const loadMovies = async () => {
    try {
      const data = await movieApi.getAll();
      setMovies(data);
    } catch (error) {
      console.error('Error loading movies:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingMovie?.id) {
        await movieApi.update(editingMovie.id, formData);
      } else {
        await movieApi.create(formData);
      }
      resetForm();
      loadMovies();
    } catch (error) {
      console.error('Error saving movie:', error);
    }
  };

  const resetForm = () => {
    setIsModalOpen(false);
    setEditingMovie(null);
    setPosterPreview('');
    setFormData({
      title: '',
      slug: '',
      trailer: '',
      description: '',
      production: '',
      durationMinutes: 0,
      releaseDate: '',
      endShowingDate: '',
      status: MovieStatus.ACTIVE,
      ageRating: AgeRating.G,
    });
  };

  if (isLoading) return <div>Loading movies...</div>;

    function handleDelete(id: number): void {
        throw new Error('Function not implemented.');
    }

  return (
    <div className="tab-content-inner">
      <div className="tab-header">
        <h2>Movie Management</h2>
        <button className="btn-primary" onClick={() => setIsModalOpen(true)}>
          Add New Movie
        </button>
      </div>

      <div className="movies-grid">
        {movies.map(movie => (
          <div key={movie.id} className="movie-card">
            <img src={movie.posterImagePath} alt={movie.title} />
            <h3>{movie.title}</h3>
            <div className="movie-actions">
              <button onClick={() => setEditingMovie(movie)}>Edit</button>
              <button onClick={() => movie.id && handleDelete(movie.id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>

      {isModalOpen && (
        <div className="modal">
        </div>
      )}
    </div>
  );
};
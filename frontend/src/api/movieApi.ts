import type { MovieDto, MovieCreateRequest } from '../types/Movie';
import type { GenreDto } from '../types/Genre';
import type { PersonDto } from '../types/Person';

const API_BASE_URL = 'http://localhost:8080/api';

const getAuthHeaders = (isFormData = false) => {
  const token = localStorage.getItem('authToken');
  const headers: Record<string, string> = {};

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (!isFormData) {
    headers['Content-Type'] = 'application/json';
  }

  return headers;
};

export const movieApi = {
  getAll: async (): Promise<MovieDto[]> => {
    const response = await fetch(`${API_BASE_URL}/movies`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies');
    return response.json();
  },

  getById: async (id: number): Promise<MovieDto> => {
    const response = await fetch(`${API_BASE_URL}/movies/${id}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movie');
    return response.json();
  },

  getBySlug: async (slug: string): Promise<MovieDto> => {
    const response = await fetch(`${API_BASE_URL}/movies/slug/${slug}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movie by slug');
    return response.json();
  },

  getPaginated: async (page: number = 0, size: number = 10): Promise<{ content: MovieDto[], totalElements: number }> => {
    const response = await fetch(`${API_BASE_URL}/movies/page?page=${page}&size=${size}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch paginated movies');
    return response.json();
  },

  getByStatus: async (status: string): Promise<MovieDto[]> => {
    const response = await fetch(`${API_BASE_URL}/movies/status/${status}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies by status');
    return response.json();
  },

  getCurrentlyShowing: async (): Promise<MovieDto[]> => {
    const response = await fetch(`${API_BASE_URL}/movies/current`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch currently showing movies');
    return response.json();
  },

  getUpcoming: async (): Promise<MovieDto[]> => {
    const response = await fetch(`${API_BASE_URL}/movies/upcoming`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch upcoming movies');
    return response.json();
  },

  getByGenre: async (genreId: number): Promise<MovieDto[]> => {
    const response = await fetch(`${API_BASE_URL}/movies/genre/${genreId}`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch movies by genre');
    return response.json();
  },

  create: async (formData: FormData): Promise<MovieDto> => {
    const response = await fetch(`${API_BASE_URL}/movies`, {
      method: 'POST',
      headers: getAuthHeaders(true),
      body: formData,
    });
    if (!response.ok) throw new Error('Failed to create movie');
    return response.json();
  },

  update: async (id: number, movieData: MovieDto): Promise<MovieDto> => {
    const response = await fetch(`${API_BASE_URL}/movies/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(movieData),
    });
    if (!response.ok) throw new Error('Failed to update movie');
    return response.json();
  },

  updateWithPoster: async (id: number, formData: FormData): Promise<MovieDto> => {
    const response = await fetch(`${API_BASE_URL}/movies/${id}/poster`, {
      method: 'PUT',
      headers: getAuthHeaders(true),
      body: formData,
    });
    if (!response.ok) throw new Error('Failed to update movie with poster');
    return response.json();
  },

  delete: async (id: number): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/movies/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to delete movie');
  },

  getPosterUrl: (id: number): string => {
    return `${API_BASE_URL}/movies/${id}/poster`;
  },

  getGenres: async (): Promise<GenreDto[]> => {
    const response = await fetch(`${API_BASE_URL}/genres`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch genres');
    return response.json();
  },

  getPersons: async (): Promise<PersonDto[]> => {
    const response = await fetch(`${API_BASE_URL}/persons`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch persons');
    return response.json();
  },
};

export const movieFormHelper = {
  createFormData: (movieData: MovieCreateRequest): FormData => {
    const formData = new FormData();

    formData.append('title', movieData.title);
    formData.append('slug', movieData.slug);
    formData.append('trailerUrl', movieData.trailerUrl);
    formData.append('description', movieData.description);
    formData.append('durationMinutes', movieData.durationMinutes.toString());
    formData.append('releaseDate', movieData.releaseDate);
    formData.append('endShowingDate', movieData.endShowingDate);
    formData.append('status', movieData.status);
    formData.append('ageRating', movieData.ageRating);

    movieData.genreIds.forEach(id => formData.append('genreIds', id.toString()));
    movieData.directorIds.forEach(id => formData.append('directorIds', id.toString()));
    movieData.screenwriterIds.forEach(id => formData.append('screenwriterIds', id.toString()));
    movieData.castIds.forEach(id => formData.append('castIds', id.toString()));

    if (movieData.posterFile) {
      formData.append('posterFile', movieData.posterFile);
    }

    return formData;
  },

  updateFormData: (movieData: MovieDto): FormData => {
    const formData = new FormData();

    if (movieData.id) formData.append('id', movieData.id.toString());
    formData.append('title', movieData.title);
    formData.append('slug', movieData.slug);
    formData.append('trailerUrl', movieData.trailerUrl);
    formData.append('description', movieData.description);
    formData.append('durationMinutes', movieData.durationMinutes.toString());
    formData.append('releaseDate', movieData.releaseDate);
    formData.append('endShowingDate', movieData.endShowingDate);
    formData.append('status', movieData.status);
    formData.append('ageRating', movieData.ageRating);

    movieData.genreIds.forEach(id => formData.append('genreIds', id.toString()));
    movieData.directorIds.forEach(id => formData.append('directorIds', id.toString()));
    movieData.screenwriterIds.forEach(id => formData.append('screenwriterIds', id.toString()));
    movieData.castIds.forEach(id => formData.append('castIds', id.toString()));

    if (movieData.posterFile) {
      formData.append('posterFile', movieData.posterFile);
    }

    return formData;
  }
};
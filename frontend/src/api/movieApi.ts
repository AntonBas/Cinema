import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieDto,
  MovieResponse,
  PageResponse
} from '@/types/Movie';

const API_BASE_URL = 'http://localhost:8080/api';

class MovieApi {
  private baseUrl = `${API_BASE_URL}/movies`;

  private getAuthHeaders(isFormData: boolean = false): HeadersInit {
    const token = localStorage.getItem('authToken');
    const headers: HeadersInit = {};

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    if (!isFormData) {
      headers['Content-Type'] = 'application/json';
    }

    return headers;
  }

  async getById(id: number): Promise<MovieDto> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to fetch movie: ${response.statusText}`);
    return response.json();
  }

  async getBySlug(slug: string): Promise<MovieDto> {
    const response = await fetch(`${this.baseUrl}/slug/${slug}`, {
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to fetch movie by slug: ${response.statusText}`);
    return response.json();
  }

  async getAll(): Promise<MovieResponse[]> {
    const response = await fetch(this.baseUrl, {
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to fetch movies: ${response.statusText}`);
    return response.json();
  }

  async getPaginated(page: number = 0, size: number = 10): Promise<PageResponse<MovieDto>> {
    const response = await fetch(`${this.baseUrl}/paginated?page=${page}&size=${size}`, {
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to fetch paginated movies: ${response.statusText}`);
    return response.json();
  }

  async getCurrentlyShowing(): Promise<MovieResponse[]> {
    const response = await fetch(`${this.baseUrl}/status/current`, {
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to fetch currently showing movies: ${response.statusText}`);
    return response.json();
  }

  async getUpcoming(): Promise<MovieResponse[]> {
    const response = await fetch(`${this.baseUrl}/status/upcoming`, {
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to fetch upcoming movies: ${response.statusText}`);
    return response.json();
  }

  async getArchived(): Promise<MovieResponse[]> {
    const response = await fetch(`${this.baseUrl}/status/archived`, {
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to fetch archived movies: ${response.statusText}`);
    return response.json();
  }

  async getMoviesForSessions(): Promise<MovieResponse[]> {
    const response = await fetch(`${this.baseUrl}/for-sessions`, {
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to fetch movies for sessions: ${response.statusText}`);
    return response.json();
  }

  async create(movieData: MovieCreateRequest, posterFile: File): Promise<MovieDto> {
    const formData = new FormData();

    const { posterFile: _, ...requestData } = movieData;

    formData.append('movieData', new Blob([JSON.stringify(requestData)], {
      type: 'application/json'
    }));

    formData.append('posterFile', posterFile);

    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: this.getAuthHeaders(true),
      body: formData,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to create movie: ${response.status} - ${errorText}`);
    }

    return response.json();
  }

  async update(id: number, movieData: MovieUpdateRequest): Promise<MovieDto> {
    const { posterFile: _, ...updateData } = movieData;

    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(updateData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to update movie: ${response.status} - ${errorText}`);
    }

    return response.json();
  }

  async delete(id: number): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(),
    });
    if (!response.ok) throw new Error(`Failed to delete movie: ${response.statusText}`);
  }

  getPosterUrl(id: number): string {
    return `${this.baseUrl}/${id}/poster`;
  }
}

export const movieApi = new MovieApi();
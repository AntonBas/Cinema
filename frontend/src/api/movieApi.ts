import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieDto,
  MovieResponse
} from '@/types/movie';
import type { PageResponse } from '@/types/pagination';

const API_BASE_URL = 'http://localhost:8080';

class MovieApi {
  private baseUrl = `${API_BASE_URL}/api/movies`;

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

  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP ${response.status}: ${errorText || response.statusText}`);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return response.json();
  }

  async getById(id: number): Promise<MovieDto> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<MovieDto>(response);
  }

  async getBySlug(slug: string): Promise<MovieDto> {
    const response = await fetch(`${this.baseUrl}/slug/${slug}`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<MovieDto>(response);
  }

  async getAll(): Promise<MovieDto[]> {
    const response = await fetch(this.baseUrl, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<MovieDto[]>(response);
  }

  async getPaginated(page: number = 0, size: number = 10): Promise<PageResponse<MovieDto>> {
    const response = await fetch(`${this.baseUrl}/paginated?page=${page}&size=${size}`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<PageResponse<MovieDto>>(response);
  }

  async getCurrentlyShowing(): Promise<MovieResponse[]> {
    const response = await fetch(`${this.baseUrl}/status/current`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<MovieResponse[]>(response);
  }

  async getUpcoming(): Promise<MovieResponse[]> {
    const response = await fetch(`${this.baseUrl}/status/upcoming`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<MovieResponse[]>(response);
  }

  async getArchived(): Promise<MovieResponse[]> {
    const response = await fetch(`${this.baseUrl}/status/archived`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<MovieResponse[]>(response);
  }

  async getMoviesForSessions(): Promise<MovieResponse[]> {
    const response = await fetch(`${this.baseUrl}/for-sessions`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<MovieResponse[]>(response);
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

    return this.handleResponse<MovieDto>(response);
  }

  async update(id: number, movieData: MovieUpdateRequest, posterFile?: File): Promise<MovieDto> {
    const formData = new FormData();

    const { posterFile: _, removePoster, ...updateData } = movieData;

    formData.append('movieData', new Blob([JSON.stringify({
      ...updateData,
      removePoster: removePoster || false
    })], {
      type: 'application/json'
    }));

    if (posterFile) {
      formData.append('posterFile', posterFile);
    }

    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: this.getAuthHeaders(true),
      body: formData,
    });

    return this.handleResponse<MovieDto>(response);
  }

  async delete(id: number): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to delete movie: ${response.status} - ${errorText}`);
    }
  }

  async getPoster(id: number): Promise<Blob> {
    const response = await fetch(`${this.baseUrl}/${id}/poster`, {
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to get poster: ${response.status} - ${errorText}`);
    }

    return response.blob();
  }

  getPosterUrl(id: number): string {
    return `${this.baseUrl}/${id}/poster`;
  }

  getPosterUrlWithTimestamp(id: number): string {
    return `${this.baseUrl}/${id}/poster?t=${Date.now()}`;
  }
}

export const movieApi = new MovieApi();
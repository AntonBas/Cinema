import type {
  MovieCreateRequest,
  MovieUpdateRequest,
  MovieDto,
  MovieResponse,
  PageResponse,
  SearchParams,
  MovieFilters
} from '@/types/Movie';

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
      throw new Error(`HTTP ${response.status}: ${errorText}`);
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

  async search(params: SearchParams): Promise<PageResponse<MovieDto>> {
    const searchParams = new URLSearchParams();

    if (params.query) searchParams.append('query', params.query);
    if (params.page) searchParams.append('page', params.page.toString());
    if (params.size) searchParams.append('size', params.size.toString());
    if (params.genre) searchParams.append('genre', params.genre);
    if (params.status) searchParams.append('status', params.status);

    const response = await fetch(`${this.baseUrl}/search?${searchParams.toString()}`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse<PageResponse<MovieDto>>(response);
  }

  async filter(filters: MovieFilters): Promise<MovieResponse[]> {
    const searchParams = new URLSearchParams();

    if (filters.title) searchParams.append('title', filters.title);
    if (filters.genre) searchParams.append('genre', filters.genre);
    if (filters.status) searchParams.append('status', filters.status);
    if (filters.ageRating) searchParams.append('ageRating', filters.ageRating);

    const response = await fetch(`${this.baseUrl}/filter?${searchParams.toString()}`, {
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

  async update(id: number, movieData: MovieUpdateRequest): Promise<MovieDto> {
    const { posterFile: _, ...updateData } = movieData;

    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(updateData),
    });

    return this.handleResponse<MovieDto>(response);
  }

  async updateWithPoster(id: number, movieData: MovieUpdateRequest, posterFile?: File): Promise<MovieDto> {
    const formData = new FormData();

    const { posterFile: _, ...updateData } = movieData;

    formData.append('movieData', new Blob([JSON.stringify(updateData)], {
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

  async uploadPoster(movieId: number, posterFile: File): Promise<MovieDto> {
    const formData = new FormData();
    formData.append('posterFile', posterFile);

    const response = await fetch(`${this.baseUrl}/${movieId}/poster`, {
      method: 'POST',
      headers: this.getAuthHeaders(true),
      body: formData,
    });

    return this.handleResponse<MovieDto>(response);
  }

  async removePoster(movieId: number): Promise<MovieDto> {
    const response = await fetch(`${this.baseUrl}/${movieId}/poster`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(),
    });

    return this.handleResponse<MovieDto>(response);
  }

  getPosterUrl(id: number): string {
    return `${this.baseUrl}/${id}/poster`;
  }

  getPosterUrlWithTimestamp(id: number): string {
    return `${this.baseUrl}/${id}/poster?t=${Date.now()}`;
  }

  async bulkUpdateStatus(ids: number[], status: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/bulk/status`, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: JSON.stringify({ ids, status }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to bulk update status: ${response.status} - ${errorText}`);
    }
  }

  async bulkDelete(ids: number[]): Promise<void> {
    const response = await fetch(`${this.baseUrl}/bulk`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(),
      body: JSON.stringify({ ids }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to bulk delete: ${response.status} - ${errorText}`);
    }
  }

  async getStatistics(): Promise<{
    total: number;
    currentlyShowing: number;
    upcoming: number;
    archived: number;
  }> {
    const response = await fetch(`${this.baseUrl}/statistics`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse(response);
  }

  async healthCheck(): Promise<{ status: string; timestamp: string }> {
    const response = await fetch(`${this.baseUrl}/health`, {
      headers: this.getAuthHeaders(),
    });
    return this.handleResponse(response);
  }
}

export const movieApi = new MovieApi();
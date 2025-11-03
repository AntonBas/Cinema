import type { SessionDto, SessionRequest } from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

const API_BASE = '/api/sessions';

export const sessionApi = {
    async createSession(request: SessionRequest): Promise<SessionDto> {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });

        if (!response.ok) {
            throw new Error(`Failed to create session: ${response.statusText}`);
        }

        return response.json();
    },

    async getSessionById(id: number): Promise<SessionDto> {
        const response = await fetch(`${API_BASE}/${id}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch session: ${response.statusText}`);
        }

        return response.json();
    },

    async updateSession(id: number, request: SessionRequest): Promise<SessionDto> {
        const response = await fetch(`${API_BASE}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });

        if (!response.ok) {
            throw new Error(`Failed to update session: ${response.statusText}`);
        }

        return response.json();
    },

    async deleteSession(id: number): Promise<void> {
        const response = await fetch(`${API_BASE}/${id}`, {
            method: 'DELETE',
        });

        if (!response.ok) {
            throw new Error(`Failed to delete session: ${response.statusText}`);
        }
    },

    async getAllSessions(params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const urlParams = new URLSearchParams();
        if (params?.page) urlParams.append('page', (params.page - 1).toString());
        if (params?.size) urlParams.append('size', params.size.toString());
        if (params?.sort) urlParams.append('sort', params.sort);
        if (params?.query) urlParams.append('search', params.query);

        const response = await fetch(`${API_BASE}?${urlParams}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch sessions: ${response.statusText}`);
        }

        return response.json();
    },

    async getSessionsByDate(date: string, params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const urlParams = new URLSearchParams();
        if (params?.page) urlParams.append('page', (params.page - 1).toString());
        if (params?.size) urlParams.append('size', params.size.toString());
        if (params?.sort) urlParams.append('sort', params.sort);

        const response = await fetch(`${API_BASE}/date/${date}?${urlParams}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch sessions by date: ${response.statusText}`);
        }

        return response.json();
    },

    async getSessionsByHall(hallId: number, params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const urlParams = new URLSearchParams();
        if (params?.page) urlParams.append('page', (params.page - 1).toString());
        if (params?.size) urlParams.append('size', params.size.toString());
        if (params?.sort) urlParams.append('sort', params.sort);

        const response = await fetch(`${API_BASE}/hall/${hallId}?${urlParams}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch sessions by hall: ${response.statusText}`);
        }

        return response.json();
    },

    async getSessionsByMovie(movieId: number, params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const urlParams = new URLSearchParams();
        if (params?.page) urlParams.append('page', (params.page - 1).toString());
        if (params?.size) urlParams.append('size', params.size.toString());
        if (params?.sort) urlParams.append('sort', params.sort);

        const response = await fetch(`${API_BASE}/movie/${movieId}?${urlParams}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch sessions by movie: ${response.statusText}`);
        }

        return response.json();
    },

    async getAvailableSessions(params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const urlParams = new URLSearchParams();
        if (params?.page) urlParams.append('page', (params.page - 1).toString());
        if (params?.size) urlParams.append('size', params.size.toString());
        if (params?.sort) urlParams.append('sort', params.sort);

        const response = await fetch(`${API_BASE}/available?${urlParams}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch available sessions: ${response.statusText}`);
        }

        return response.json();
    },

    async getUpcomingSessions(days: number = 7, params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const urlParams = new URLSearchParams();
        urlParams.append('days', days.toString());
        if (params?.page) urlParams.append('page', (params.page - 1).toString());
        if (params?.size) urlParams.append('size', params.size.toString());
        if (params?.sort) urlParams.append('sort', params.sort);

        const response = await fetch(`${API_BASE}/upcoming?${urlParams}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch upcoming sessions: ${response.statusText}`);
        }

        return response.json();
    },

    async getTodaySessions(params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const urlParams = new URLSearchParams();
        if (params?.page) urlParams.append('page', (params.page - 1).toString());
        if (params?.size) urlParams.append('size', params.size.toString());
        if (params?.sort) urlParams.append('sort', params.sort);

        const response = await fetch(`${API_BASE}/today?${urlParams}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch today's sessions: ${response.statusText}`);
        }

        return response.json();
    },

    async checkTimeConflict(
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ): Promise<boolean> {
        const params = new URLSearchParams({
            hallId: hallId.toString(),
            startTime: startTime,
            durationMinutes: durationMinutes.toString(),
        });

        if (excludeSessionId) {
            params.append('excludeSessionId', excludeSessionId.toString());
        }

        const response = await fetch(`${API_BASE}/check-conflict?${params}`);

        if (!response.ok) {
            throw new Error(`Failed to check time conflict: ${response.statusText}`);
        }

        return response.json();
    },
};
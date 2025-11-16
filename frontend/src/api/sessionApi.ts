import type { SessionResponse, SessionRequest } from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

const API_URL = '/api/sessions';

const getAuthHeaders = () => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
    };
};

export const sessionApi = {
    createSession: async (request: SessionRequest): Promise<SessionResponse> => {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) throw new Error('Failed to create session');
        return response.json();
    },

    getSessionById: async (id: number): Promise<SessionResponse> => {
        const response = await fetch(`${API_URL}/${id}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch session');
        return response.json();
    },

    updateSession: async (id: number, request: SessionRequest): Promise<SessionResponse> => {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) throw new Error('Failed to update session');
        return response.json();
    },

    deleteSession: async (id: number): Promise<void> => {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to delete session');
    },

    getAllSessions: async (params?: SearchParams): Promise<PageResponse<SessionResponse>> => {
        const searchParams = new URLSearchParams();
        if (params?.query) searchParams.append('search', params.query);
        if (params?.page) searchParams.append('page', params.page.toString());
        if (params?.size) searchParams.append('size', params.size.toString());

        const url = `${API_URL}?${searchParams}`;
        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch sessions');
        return response.json();
    },

    getSessionsByDate: async (date: string, params?: SearchParams): Promise<PageResponse<SessionResponse>> => {
        const searchParams = new URLSearchParams();
        if (params?.page) searchParams.append('page', params.page.toString());
        if (params?.size) searchParams.append('size', params.size.toString());

        const url = `${API_URL}/date/${date}?${searchParams}`;
        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch sessions by date');
        return response.json();
    },

    getSessionsByHall: async (hallId: number, params?: SearchParams): Promise<PageResponse<SessionResponse>> => {
        const searchParams = new URLSearchParams();
        if (params?.page) searchParams.append('page', params.page.toString());
        if (params?.size) searchParams.append('size', params.size.toString());

        const url = `${API_URL}/hall/${hallId}?${searchParams}`;
        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch sessions by hall');
        return response.json();
    },

    getSessionsByMovie: async (movieId: number, params?: SearchParams): Promise<PageResponse<SessionResponse>> => {
        const searchParams = new URLSearchParams();
        if (params?.page) searchParams.append('page', params.page.toString());
        if (params?.size) searchParams.append('size', params.size.toString());

        const url = `${API_URL}/movie/${movieId}?${searchParams}`;
        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch sessions by movie');
        return response.json();
    },

    getAvailableSessions: async (params?: SearchParams): Promise<PageResponse<SessionResponse>> => {
        const searchParams = new URLSearchParams();
        if (params?.page) searchParams.append('page', params.page.toString());
        if (params?.size) searchParams.append('size', params.size.toString());

        const url = `${API_URL}/available?${searchParams}`;
        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch available sessions');
        return response.json();
    },

    getUpcomingSessions: async (days: number = 7, params?: SearchParams): Promise<PageResponse<SessionResponse>> => {
        const searchParams = new URLSearchParams();
        searchParams.append('days', days.toString());
        if (params?.page) searchParams.append('page', params.page.toString());
        if (params?.size) searchParams.append('size', params.size.toString());

        const url = `${API_URL}/upcoming?${searchParams}`;
        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch upcoming sessions');
        return response.json();
    },

    getTodaySessions: async (params?: SearchParams): Promise<PageResponse<SessionResponse>> => {
        const searchParams = new URLSearchParams();
        if (params?.page) searchParams.append('page', params.page.toString());
        if (params?.size) searchParams.append('size', params.size.toString());

        const url = `${API_URL}/today?${searchParams}`;
        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch today sessions');
        return response.json();
    },

    checkTimeConflict: async (
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ): Promise<boolean> => {
        const searchParams = new URLSearchParams();
        searchParams.append('hallId', hallId.toString());
        searchParams.append('startTime', startTime);
        searchParams.append('durationMinutes', durationMinutes.toString());
        if (excludeSessionId) searchParams.append('excludeSessionId', excludeSessionId.toString());

        const url = `${API_URL}/check-conflict?${searchParams}`;
        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to check time conflict');
        return response.json();
    },
};
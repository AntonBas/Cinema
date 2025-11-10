import { api } from '@/services/api';
import type { SessionDto, SessionRequest } from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

export const sessionApi = {
    async createSession(request: SessionRequest): Promise<SessionDto> {
        const response = await api.post('/api/sessions', request);
        return response.data;
    },

    async getSessionById(id: number): Promise<SessionDto> {
        const response = await api.get(`/api/sessions/${id}`);
        return response.data;
    },

    async updateSession(id: number, request: SessionRequest): Promise<SessionDto> {
        const response = await api.put(`/api/sessions/${id}`, request);
        return response.data;
    },

    async deleteSession(id: number): Promise<void> {
        await api.delete(`/api/sessions/${id}`);
    },

    async getAllSessions(params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const response = await api.get('/api/sessions', { params });
        return response.data;
    },

    async getSessionsByDate(date: string, params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const response = await api.get(`/api/sessions/date/${date}`, { params });
        return response.data;
    },

    async getSessionsByHall(hallId: number, params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const response = await api.get(`/api/sessions/hall/${hallId}`, { params });
        return response.data;
    },

    async getSessionsByMovie(movieId: number, params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const response = await api.get(`/api/sessions/movie/${movieId}`, { params });
        return response.data;
    },

    async getAvailableSessions(params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const response = await api.get('/api/sessions/available', { params });
        return response.data;
    },

    async getUpcomingSessions(days: number = 7, params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const response = await api.get('/api/sessions/upcoming', {
            params: { days, ...params }
        });
        return response.data;
    },

    async getTodaySessions(params?: SearchParams): Promise<PageResponse<SessionDto>> {
        const response = await api.get('/api/sessions/today', { params });
        return response.data;
    },

    async checkTimeConflict(
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ): Promise<boolean> {
        const response = await api.get('/api/sessions/check-conflict', {
            params: { hallId, startTime, durationMinutes, excludeSessionId }
        });
        return response.data;
    },
};
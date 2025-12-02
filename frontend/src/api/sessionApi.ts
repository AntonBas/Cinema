import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionRequest,
    SessionFilters
} from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/sessions';

const getAuthHeaders = () => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
    };
};

const createSearchParams = (filters?: Record<string, any>, params?: SearchParams): URLSearchParams => {
    const searchParams = new URLSearchParams();

    if (filters) {
        Object.entries(filters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                searchParams.append(key, value.toString());
            }
        });
    }

    if (params?.page) searchParams.append('page', params.page.toString());
    if (params?.size) searchParams.append('size', params.size.toString());
    if (params?.sort) searchParams.append('sort', params.sort);

    return searchParams;
};

const handleResponse = async <T>(response: Response): Promise<T> => {
    if (!response.ok) throw await handleApiError(response);
    return response.json();
};

const createFetchOptions = (method: string = 'GET', body?: any) => ({
    method,
    headers: getAuthHeaders(),
    ...(body && { body: JSON.stringify(body) }),
});

export const sessionApi = {
    createSession: async (request: SessionRequest): Promise<SessionAdminResponse> => {
        const response = await fetch(API_URL, createFetchOptions('POST', request));
        return handleResponse(response);
    },

    getSessionById: async (id: number): Promise<SessionAdminResponse> => {
        const response = await fetch(`${API_URL}/${id}`, createFetchOptions());
        return handleResponse(response);
    },

    updateSession: async (id: number, request: SessionRequest): Promise<SessionAdminResponse> => {
        const response = await fetch(`${API_URL}/${id}`, createFetchOptions('PUT', request));
        return handleResponse(response);
    },

    deleteSession: async (id: number): Promise<void> => {
        const response = await fetch(`${API_URL}/${id}`, createFetchOptions('DELETE'));
        await handleResponse(response);
    },

    getAllSessions: async (params?: SearchParams): Promise<PageResponse<SessionAdminResponse>> => {
        const searchParams = createSearchParams({ search: params?.query }, params);
        const response = await fetch(`${API_URL}?${searchParams}`, createFetchOptions());
        return handleResponse(response);
    },

    getSessionsByDate: async (date: string, params?: SearchParams): Promise<PageResponse<SessionAdminResponse>> => {
        const response = await fetch(`${API_URL}/date/${date}?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getSessionsByHall: async (hallId: number, params?: SearchParams): Promise<PageResponse<SessionAdminResponse>> => {
        const response = await fetch(`${API_URL}/hall/${hallId}?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getSessionsByMovie: async (movieId: number, params?: SearchParams): Promise<PageResponse<SessionAdminResponse>> => {
        const response = await fetch(`${API_URL}/movie/${movieId}?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getAvailableSessions: async (params?: SearchParams): Promise<PageResponse<SessionAdminResponse>> => {
        const response = await fetch(`${API_URL}/available?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getUpcomingSessions: async (days: number = 7, params?: SearchParams): Promise<PageResponse<SessionAdminResponse>> => {
        const searchParams = createSearchParams({ days }, params);
        const response = await fetch(`${API_URL}/upcoming?${searchParams}`, createFetchOptions());
        return handleResponse(response);
    },

    getTodaySessions: async (params?: SearchParams): Promise<PageResponse<SessionAdminResponse>> => {
        const response = await fetch(`${API_URL}/today?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getFilteredSessions: async (
        filters: SessionFilters,
        params?: SearchParams
    ): Promise<PageResponse<SessionAdminResponse>> => {
        const searchParams = createSearchParams(filters, params);
        const response = await fetch(`${API_URL}/filter?${searchParams}`, createFetchOptions());
        return handleResponse(response);
    },

    checkTimeConflict: async (
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ): Promise<boolean> => {
        const filters: Record<string, any> = { hallId, startTime, durationMinutes };
        if (excludeSessionId) filters.excludeSessionId = excludeSessionId;

        const searchParams = createSearchParams(filters);
        const response = await fetch(`${API_URL}/check-conflict?${searchParams}`, createFetchOptions());
        return handleResponse(response);
    },

    getScheduleSessions: async (params?: SearchParams): Promise<PageResponse<SessionScheduleResponse>> => {
        const response = await fetch(`${API_URL}/schedule?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getScheduleSessionsByDate: async (date: string, params?: SearchParams): Promise<PageResponse<SessionScheduleResponse>> => {
        const response = await fetch(`${API_URL}/schedule/date/${date}?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getScheduleSessionsByMovie: async (movieId: number, params?: SearchParams): Promise<PageResponse<SessionScheduleResponse>> => {
        const response = await fetch(`${API_URL}/schedule/movie/${movieId}?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getAvailableScheduleSessions: async (params?: SearchParams): Promise<PageResponse<SessionScheduleResponse>> => {
        const response = await fetch(`${API_URL}/schedule/available?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },

    getUpcomingScheduleSessions: async (days: number = 7, params?: SearchParams): Promise<PageResponse<SessionScheduleResponse>> => {
        const searchParams = createSearchParams({ days }, params);
        const response = await fetch(`${API_URL}/schedule/upcoming?${searchParams}`, createFetchOptions());
        return handleResponse(response);
    },

    getTodayScheduleSessions: async (params?: SearchParams): Promise<PageResponse<SessionScheduleResponse>> => {
        const response = await fetch(`${API_URL}/schedule/today?${createSearchParams({}, params)}`, createFetchOptions());
        return handleResponse(response);
    },
};
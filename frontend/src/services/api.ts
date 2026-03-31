import axios from 'axios';
import { ApiErrorException } from '@/utils/apiErrorHandler';

export const api = axios.create({
    baseURL: 'http://localhost:8080',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('authToken');

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response) {
            if (error.response.status === 429) {
                const apiErrorException = new ApiErrorException({
                    message: 'Too many requests. Please wait a moment before trying again.',
                    status: 'TOO_MANY_REQUESTS',
                    statusCode: 429,
                    timestamp: new Date().toISOString()
                } as any);
                return Promise.reject(apiErrorException);
            }

            const responseData = error.response.data;

            if (responseData && typeof responseData === 'object' && 'statusCode' in responseData) {
                const apiErrorException = new ApiErrorException(responseData as any);
                return Promise.reject(apiErrorException);
            }

            if (error.response.status === 401) {
                localStorage.removeItem('authToken');
                const currentPath = window.location.pathname;
                if (currentPath !== '/login' && currentPath !== '/register') {
                    window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
                }
            }
        }

        if (error.code === 'NETWORK_ERROR' || error.message === 'Network Error') {
            console.error('Network error - please check your connection');
        }

        if (error.code === 'ECONNABORTED') {
            console.error('Request timeout - please try again');
        }

        return Promise.reject(error);
    }
);

export const setAuthToken = (token: string | null) => {
    if (token) {
        localStorage.setItem('authToken', token);
    } else {
        localStorage.removeItem('authToken');
    }
};

export const getAuthToken = (): string | null => {
    return localStorage.getItem('authToken');
};
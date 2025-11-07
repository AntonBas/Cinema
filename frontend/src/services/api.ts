import axios from 'axios';

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
        if (error.response?.status === 401) {
            localStorage.removeItem('authToken');
            const currentPath = window.location.pathname;
            if (currentPath !== '/login' && currentPath !== '/register') {
                window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
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
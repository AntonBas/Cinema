import axios from 'axios';

export const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    withCredentials: true,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('authToken');

    console.log('Token from storage:', token);

    if (token) {
        if (!config.headers) {
            config.headers = {} as any;
        }
        config.headers['Authorization'] = `Bearer ${token}`;
    }

    console.log('Making request to:', config.url);
    return config;
});

api.interceptors.response.use(
    (response) => {
        console.log('Response received:', {
            status: response.status,
            url: response.config.url,
            data: response.data
        });
        return response;
    },
    (error) => {
        console.error('API error:', {
            url: error.config?.url,
            status: error.response?.status,
            data: error.response?.data
        });

        if (error.response?.status === 401) {
            localStorage.removeItem('authToken');
            window.location.href = '/login';
        }

        return Promise.reject(error);
    }
);
import axios from 'axios';

export const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    withCredentials: true,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    
    if (token) {
        if (!config.headers) {
            config.headers = {} as any;
        }
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    console.log('Request headers:', config.headers);
    return config;
});

api.interceptors.response.use(
    (response) => {
        console.log('Response received:', {
            status: response.status,
            headers: response.headers,
            data: response.data
        });
        return response;
    },
    (error) => {
        console.error('API error:', error.response?.data || error.message);
        return Promise.reject(error);
    }
);
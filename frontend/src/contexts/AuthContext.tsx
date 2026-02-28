import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { authApi } from '@/api/authApi';
import type { UserResponse } from '@/types/user';
import type { LoginRequest, RegisterRequest } from '@/types/auth';

interface AuthContextType {
    user: UserResponse | null;
    loading: boolean;
    isAuthenticated: boolean;
    isAdmin: boolean;
    login: (credentials: LoginRequest) => Promise<void>;
    register: (userData: RegisterRequest) => Promise<UserResponse>;
    logout: () => void;
    refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<UserResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const fetchedRef = useRef(false);

    const token = localStorage.getItem('authToken');
    const isAuthenticated = !!token;
    const isAdmin = user?.userRole === 'ROLE_ADMIN';

    useEffect(() => {
        if (isAuthenticated && !fetchedRef.current) {
            fetchedRef.current = true;
            setLoading(true);

            authApi.getCurrentUser()
                .then(setUser)
                .catch(() => {
                    localStorage.removeItem('authToken');
                    setUser(null);
                })
                .finally(() => setLoading(false));
        } else if (!isAuthenticated) {
            setLoading(false);
        }
    }, [isAuthenticated]);

    const login = async (credentials: LoginRequest) => {
        const response = await authApi.login(credentials);
        localStorage.setItem('authToken', response.token);
        setUser(response.user);
    };

    const register = async (userData: RegisterRequest) => {
        return await authApi.register(userData);
    };

    const logout = () => {
        localStorage.removeItem('authToken');
        setUser(null);
        fetchedRef.current = false;
        window.location.href = '/login';
    };

    const refreshUser = async () => {
        const currentToken = localStorage.getItem('authToken');
        if (currentToken) {
            const userData = await authApi.getCurrentUser();
            setUser(userData);
        }
    };

    return (
        <AuthContext.Provider value={{
            user,
            loading,
            isAuthenticated,
            isAdmin,
            login,
            register,
            logout,
            refreshUser,
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};
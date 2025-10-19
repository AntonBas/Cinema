import React, { createContext, useContext, useState, useEffect } from "react";
import type { User } from '../types/auth';
import { authService } from '../services/authService';
import { userService } from '../services/userService';

interface AuthContextType {
    user: User | null;
    token: string | null;
    login: (email: string, password: string) => Promise<void>;
    register: (userData: any) => Promise<void>;
    logout: () => void;
    isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (token) {
            localStorage.setItem('token', token);
        } else {
            localStorage.removeItem('token');
        }
    }, [token]);

    useEffect(() => {
        const loadUserData = async () => {
            if (token) {
                try {
                    console.log('Loading user data with token:', token);
                    const userData = await userService.getProfile();
                    console.log('User data loaded:', userData);
                    setUser(userData);
                } catch (error) {
                    console.error('Failed to load user data:', error);
                    setToken(null);
                    setUser(null);
                    localStorage.removeItem('token');
                }
            } else {
                setUser(null);
            }
            setIsLoading(false);
        };

        loadUserData();
    }, [token]);

    const login = async (email: string, password: string) => {
        try {
            setIsLoading(true);
            console.log('AuthContext: Starting login process for', email);

            const response = await authService.login({ email, password });

            console.log('AuthContext: Login response received', response);

            if (!response.token) {
                throw new Error('No token received from server');
            }

            setToken(response.token);
            localStorage.setItem('token', response.token);

            console.log('AuthContext: Login completed successfully, token set');

        } catch (error) {
            console.error('AuthContext: Login failed:', error);
            setToken(null);
            setUser(null);
            localStorage.removeItem('token');
            throw error;
        } finally {
            setIsLoading(false);
        }
    };

    const register = async (userData: any) => {
        try {
            await authService.register(userData);
        } catch (error) {
            console.error('Registration failed:', error);
            throw error;
        }
    };

    const logout = () => {
        setToken(null);
        setUser(null);
        localStorage.removeItem('token');
        console.log('AuthContext: User logged out');
    };

    const value: AuthContextType = {
        user,
        token,
        login,
        register,
        logout,
        isLoading
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
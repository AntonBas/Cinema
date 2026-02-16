import React, { createContext, useContext } from 'react';
import { useAuth as useAuthHook } from '@/hooks/features/auth/useAuth';
import type { UserResponse } from '@/types/user';

interface AuthContextType {
    user: UserResponse | null;
    loading: boolean;
    error: boolean;
    isAuthenticated: boolean;
    isAdmin: boolean;
    login: (email: string, password: string) => Promise<void>;
    register: (userData: any) => Promise<any>;
    logout: () => void;
    checkEmail: (email: string) => Promise<boolean>;
    forgotPassword: (email: string) => Promise<void>;
    resetPassword: (token: string, newPassword: string) => Promise<void>;
    verifyEmail: (token: string) => Promise<{ message: string }>;
    confirmEmailChange: (token: string) => Promise<UserResponse>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const auth = useAuthHook();

    return (
        <AuthContext.Provider value={{
            user: auth.user as UserResponse | null,
            loading: auth.loading,
            error: auth.error,
            isAuthenticated: auth.isAuthenticated,
            isAdmin: auth.isAdmin,
            login: async (email: string, password: string) => {
                await auth.login({ email, password });
            },
            register: auth.register,
            logout: auth.logout,
            checkEmail: auth.checkEmail,
            forgotPassword: auth.forgotPassword,
            resetPassword: auth.resetPassword,
            verifyEmail: auth.verifyEmail,
            confirmEmailChange: auth.confirmEmailChange,
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
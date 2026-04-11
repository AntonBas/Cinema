import { useState, useEffect, useCallback } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useSession } from '@/hooks/features/sessions/useSession';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import { useNotification } from '@/hooks/common/useNotification';

export interface DashboardStats {
    totalMovies: number;
    totalHalls: number;
    totalSessions: number;
    totalUsers: number;
    activePromotions: number;
    isLoading: boolean;
}

export const useDashboardStats = () => {
    const [stats, setStats] = useState<DashboardStats>({
        totalMovies: 0,
        totalHalls: 0,
        totalSessions: 0,
        totalUsers: 0,
        activePromotions: 0,
        isLoading: true,
    });

    const { showNotification } = useNotification();
    const { getAdminMovies } = useMovies();
    const { getAllHalls } = useCinemaHalls();
    const { getAdminSessions } = useSession();
    const { getUsers } = useAdminUsers();
    const { getAll: getPromotions } = usePromotion();

    const loadDashboardData = useCallback(async () => {
        try {
            const [moviesRes, hallsRes, sessionsRes, usersRes, promotionsRes] = await Promise.all([
                getAdminMovies({ size: 1 }),
                getAllHalls(),
                getAdminSessions({ size: 1 }),
                getUsers({ size: 1 }),
                getPromotions({ size: 100 }),
            ]);

            setStats({
                totalMovies: moviesRes?.totalElements || 0,
                totalHalls: hallsRes?.length || 0,
                totalSessions: sessionsRes?.totalElements || 0,
                totalUsers: usersRes?.totalElements || 0,
                activePromotions: promotionsRes?.content?.length || 0,
                isLoading: false,
            });
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            showNotification('Failed to load dashboard data', 'error');
            setStats(prev => ({ ...prev, isLoading: false }));
        }
    }, [getAdminMovies, getAllHalls, getAdminSessions, getUsers, getPromotions, showNotification]);

    useEffect(() => {
        loadDashboardData();
    }, [loadDashboardData]);

    return {
        stats,
        isLoading: stats.isLoading,
        refresh: loadDashboardData,
    };
};
import { useState, useEffect, useCallback, useRef } from 'react';
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
    activeScreenings: number;
    todaySessions: number;
    upcomingSessions: number;
    activePromotions: number;
    totalTicketsSold: number;
    totalRevenue: number;
    averageOccupancyRate: number;
    todaysStats: {
        ticketsSold: number;
        revenue: number;
        activeUsers: number;
        sessionsCompleted: number;
        activeMovies: number;
    };
}

export const useDashboardStats = () => {
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [lastUpdated, setLastUpdated] = useState<string>('');
    const isMountedRef = useRef(true);
    const isLoadingRef = useRef(false);

    const { getAdminCurrent, getAdminUpcoming, getAdminArchived } = useMovies();
    const { getAllHalls } = useCinemaHalls();
    const { getSessions } = useSession();
    const { getUsers } = useAdminUsers();
    const { getAll: getPromotions } = usePromotion();
    const { showNotification } = useNotification();

    const loadDashboardData = useCallback(async () => {
        if (isLoadingRef.current || !isMountedRef.current) return;

        isLoadingRef.current = true;

        try {
            setIsLoading(true);

            const today = new Date();
            const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate());
            const todayEnd = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1);

            const [
                currentMoviesResponse,
                upcomingMoviesResponse,
                archivedMoviesResponse,
                hallsResponse,
                sessionsResponse,
                usersResponse,
                promotionsResponse
            ] = await Promise.all([
                getAdminCurrent({ page: 0, size: 1000 }),
                getAdminUpcoming({ page: 0, size: 1000 }),
                getAdminArchived({ page: 0, size: 1000 }),
                getAllHalls(),
                getSessions({ page: 0, size: 1000 }),
                getUsers({ page: 0, size: 1000 }),
                getPromotions()
            ]);

            const movies = [
                ...(currentMoviesResponse?.content || []),
                ...(upcomingMoviesResponse?.content || []),
                ...(archivedMoviesResponse?.content || [])
            ];

            const halls = hallsResponse || [];
            const sessions = sessionsResponse?.content || [];
            const users = usersResponse?.content || [];
            const promotions = promotionsResponse?.content || [];

            const now = new Date();

            const activeSessions = sessions.filter(session => new Date(session.startTime) > now);

            const todaySessions = sessions.filter(session => {
                const sessionTime = new Date(session.startTime);
                return sessionTime >= todayStart && sessionTime < todayEnd;
            });

            const upcomingSessions = sessions.filter(session => {
                const sessionTime = new Date(session.startTime);
                return sessionTime > now && sessionTime <= new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
            });

            const completedSessions = sessions.filter(session => {
                const sessionTime = new Date(session.startTime);
                const movie = movies.find(m => m.id === session.movieId);
                const duration = movie?.durationMinutes || 120;
                const sessionEnd = new Date(sessionTime.getTime() + duration * 60000);
                return sessionEnd <= now;
            });

            const activePromotions = promotions.filter(promo => {
                const endDate = promo.endDate ? new Date(promo.endDate) : null;
                return (!endDate || endDate > now);
            }).length;

            const activeUsersToday = users.filter(user => {
                const lastActivity = new Date(user.lastActivity);
                return lastActivity >= todayStart && lastActivity < todayEnd;
            }).length;

            const activeMovies = movies.filter(movie =>
                movie.status === 'CURRENT' || movie.status === 'UPCOMING'
            );

            const totalRevenue = sessions.reduce((sum, session) => {
                const revenue = typeof session.totalRevenue === 'number' ? session.totalRevenue : 0;
                return sum + revenue;
            }, 0);

            const todayRevenue = todaySessions.reduce((sum, session) => {
                const revenue = typeof session.totalRevenue === 'number' ? session.totalRevenue : 0;
                return sum + revenue;
            }, 0);

            const ticketsSold = sessions.reduce((sum, session) => {
                const sold = typeof session.ticketsSold === 'number' ? session.ticketsSold : 0;
                return sum + sold;
            }, 0);

            const todayTicketsSold = todaySessions.reduce((sum, session) => {
                const sold = typeof session.ticketsSold === 'number' ? session.ticketsSold : 0;
                return sum + sold;
            }, 0);

            const occupancyRates = sessions
                .map(session => {
                    const hall = halls.find(h => h.id === session.hallId);
                    const totalSeats = hall?.capacity || 100;
                    const bookedSeats = typeof session.ticketsSold === 'number' ? session.ticketsSold : 0;
                    return (bookedSeats / totalSeats) * 100;
                })
                .filter(rate => !isNaN(rate));

            const averageOccupancyRate = occupancyRates.length > 0
                ? occupancyRates.reduce((a, b) => a + b, 0) / occupancyRates.length
                : 0;

            if (isMountedRef.current) {
                setStats({
                    totalMovies: movies.length,
                    totalHalls: halls.length,
                    totalSessions: sessions.length,
                    totalUsers: users.length,
                    activeScreenings: activeSessions.length,
                    todaySessions: todaySessions.length,
                    upcomingSessions: upcomingSessions.length,
                    activePromotions,
                    totalTicketsSold: ticketsSold,
                    totalRevenue,
                    averageOccupancyRate: parseFloat(averageOccupancyRate.toFixed(1)),
                    todaysStats: {
                        ticketsSold: todayTicketsSold,
                        revenue: todayRevenue,
                        activeUsers: activeUsersToday,
                        sessionsCompleted: completedSessions.length,
                        activeMovies: activeMovies.length
                    }
                });

                setLastUpdated(new Date().toLocaleTimeString('en-US', {
                    hour: '2-digit',
                    minute: '2-digit'
                }));
            }

        } catch (error) {
            console.error('Error loading dashboard data:', error);
            if (isMountedRef.current) {
                showNotification('Failed to load dashboard data', 'error');
            }
        } finally {
            if (isMountedRef.current) {
                setIsLoading(false);
            }
            isLoadingRef.current = false;
        }
    }, [getAdminCurrent, getAdminUpcoming, getAdminArchived, getAllHalls, getSessions, getUsers, getPromotions, showNotification]);

    useEffect(() => {
        isMountedRef.current = true;

        loadDashboardData();

        const interval = setInterval(() => {
            loadDashboardData();
        }, 300000);

        return () => {
            isMountedRef.current = false;
            clearInterval(interval);
        };
    }, []);

    return { stats, isLoading, lastUpdated, refresh: loadDashboardData };
};
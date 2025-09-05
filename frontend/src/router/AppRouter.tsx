import { createBrowserRouter } from 'react-router-dom';
import Layout from '../components/Layout/Layout';
import LoginPage from '../pages/LoginPage/LoginPage';
import HomePage from '../pages/HomePage/HomePage';
import AdminMoviePage from '../pages/admin/MoviePage/MoviePage';
import AdminPersonPage from '../pages/admin/PersonPage/PersonPage';
import ProtectedRoute from '../components/ProtectedRoute/ProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'admin/movie',
        element: (
          <ProtectedRoute>
            <AdminMoviePage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'admin/person',
        element: (
          <ProtectedRoute>
            <AdminPersonPage />
          </ProtectedRoute>
        ),
      },
    ],
  },
]);

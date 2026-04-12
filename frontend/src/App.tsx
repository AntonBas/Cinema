import { BrowserRouter as Router } from 'react-router-dom';
import { AppRoutes } from '@/routes/AppRoutes';
import { AuthProvider } from '@/context/AuthContext';
import { NotificationProvider } from '@/context/NotificationContext';
import { NotificationContainer } from '@/components/ui';
import '@/components/ui/shared/styles/reset.css';
import './App.css';

function App() {
  return (
    <Router>
      <NotificationProvider>
        <AuthProvider>
          <NotificationContainer />
          <AppRoutes />
        </AuthProvider>
      </NotificationProvider>
    </Router>
  );
}

export default App;
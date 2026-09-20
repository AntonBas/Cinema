import { BrowserRouter as Router } from 'react-router-dom';
import { AppRoutes } from '@/routes/AppRoutes';
import { AuthProvider } from '@/context/AuthProvider';
import { NotificationProvider } from '@/context/NotificationProvider';
import { NotificationContainer } from '@/components/ui';
import { ScrollToTop } from '@/components/layout/ScrollToTop/ScrollToTop';
import '@/components/ui/shared/styles/reset.css';
import './App.css';

function App() {
  return (
    <Router>
      <ScrollToTop />
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
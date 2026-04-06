import { BrowserRouter as Router } from 'react-router-dom';
import { AppRoutes } from '@/routes/AppRoutes';
import { AuthProvider } from '@/context/AuthContext';
import '@/components/ui/shared/styles/reset.css';
import './App.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </Router>
  );
}

export default App;
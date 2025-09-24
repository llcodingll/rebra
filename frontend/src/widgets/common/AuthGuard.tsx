import { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

interface AuthGuardProps {
  children: React.ReactNode;
}

export default function AuthGuard({ children }: AuthGuardProps) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
  const location = useLocation();

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const baseURL = import.meta.env.VITE_API_BASE_URL ||
                     (window.location.hostname === 'localhost' ? 'http://localhost:8080' : '');

      const response = await fetch(`${baseURL}/api/users/me`, {
        credentials: 'include',
      });

      if (response.ok) {
        try {
          const data = await response.json();

          if (data && data.success && data.data && data.data.userId) {
            setIsAuthenticated(true);
          } else {
            setIsAuthenticated(false);
          }
        } catch (parseError) {
          setIsAuthenticated(false);
        }
      } else {
        setIsAuthenticated(false);
      }
    } catch (error) {
      setIsAuthenticated(false);
    }
  };

  if (isAuthenticated === null) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        fontSize: '16px',
        color: '#6b7280'
      }}>
        로딩 중...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/landing" state={{ from: location }} replace />;
  }

  return <>{children}</>;
}
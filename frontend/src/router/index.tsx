import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from '../App';
import Layout from '../widgets/common/Layout';
import AuthGuard from '../widgets/common/AuthGuard';
import LandingPage from '../pages/landing/LandingPage';
import SignupPage from '../pages/signup';
import DashboardPage from '../pages/dashboard/DashboardPage';
import SearchPage from '../pages/search/SearchPage';
import BacktestPage from '../pages/backtest/BacktestPage';
import BacktestCreationPage from '../pages/backtest/BacktestCreationPage';
import BacktestResultsPage from '../pages/backtest/BacktestResultsPage';
import StockDetailPage from '../pages/stock-detail/StockDetailPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      {
        index: true,
        element: <Navigate to='/landing' replace />,
      },
      {
        path: 'landing',
        element: <LandingPage />,
      },
      {
        path: 'signup',
        element: <SignupPage />,
      },
      {
        path: 'dashboard',
        element: (
          <AuthGuard>
            <Layout />
          </AuthGuard>
        ),
        children: [
          {
            index: true,
            element: <DashboardPage />,
          },
        ],
      },
      {
        path: 'search',
        element: (
          <AuthGuard>
            <Layout />
          </AuthGuard>
        ),
        children: [
          {
            index: true,
            element: <SearchPage />,
          },
          {
            path: 'stocks/:symbol',
            element: <StockDetailPage />,
          },
        ],
      },
      {
        path: 'backtest',
        element: (
          <AuthGuard>
            <Layout />
          </AuthGuard>
        ),
        children: [
          {
            index: true,
            element: <BacktestPage />,
          },
          {
            path: 'create',
            element: <BacktestCreationPage />,
          },
          {
            path: 'results/:id',
            element: <BacktestResultsPage />,
          },
        ],
      },
      {
        path: 'stock-test',
        element: (
          <AuthGuard>
            <StockDetailPage />
          </AuthGuard>
        ),
      },
      {
        path: '*',
        element: <Navigate to='/landing' replace />,
      },
    ],
  },
]);
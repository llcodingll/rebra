import { createBrowserRouter, Navigate } from 'react-router-dom';
import { lazy } from 'react';
import App from '../App';
import Layout from '../widgets/common/Layout';
import LandingPage from '../pages/landing/LandingPage';
import SignupPage from '../pages/signup';
import LoginPage from '../pages/login/LoginPage';
const DashboardPage = lazy(() => import('../pages/dashboard/DashboardPage'));
const SearchPage = lazy(() => import('../pages/search/SearchPage'));
const BacktestPage = lazy(() => import('../pages/backtest/BacktestPage'));
const BacktestCreationPage = lazy(() => import('../pages/backtest/BacktestCreationPage'));
const BacktestResultsPage = lazy(() => import('../pages/backtest/BacktestResultsPage'));
const StockDetailPage = lazy(() => import('../pages/stock-detail/StockDetailPage'));
const ProfitStatusPage = lazy(() => import('../pages/dashboard/ProfitStatusPage'));

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      {
        index: true,
        element: <Navigate to='/login' replace />,
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
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: 'dashboard',
        element: <Layout />,
        children: [
          {
            index: true,
            element: <DashboardPage />,
          },
          {
            path: 'search',
            element: <SearchPage />,
          },
          {
            path: 'backtest',
            element: <BacktestPage />,
          },
          {
            path: 'backtest/create',
            element: <BacktestCreationPage />,
          },
          {
            path: 'backtest/results/:id',
            element: <BacktestResultsPage />,
          },
          {
            path: 'stocks/:symbol',
            element: <StockDetailPage />,
          },
          {
            path: 'profit-status',
            element: <ProfitStatusPage />,
          },
        ],
      },
      {
        path: 'stock-test',
        element: <StockDetailPage />,
      },
      {
        path: '*',
        element: <Navigate to='/landing' replace />,
      },
    ],
  },
]);

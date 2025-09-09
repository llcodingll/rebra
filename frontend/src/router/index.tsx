import { createBrowserRouter, Navigate } from 'react-router-dom';
import { lazy } from 'react';
import App from '../App';
import Layout from '../widget/common/Layout';
import LandingPage from '../pages/LandingPage';
import SignupPage from '../pages/signup';
import LoginPage from '../pages/LoginPage';
const DashboardPage = lazy(() => import('../pages/DashboardPage'));
const SearchPage = lazy(() => import('../pages/SearchPage'));
const BacktestPage = lazy(() => import('../pages/BacktestPage'));
const BacktestCreationPage = lazy(() => import('../pages/BacktestCreationPage'));
const BacktestResultsPage = lazy(() => import('../pages/BacktestResultsPage'));
const StockDetailPage = lazy(() => import('../pages/StockDetailPage'));
const ProfitStatusPage = lazy(() => import('../pages/ProfitStatusPage'));

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
        path: '*',
        element: <Navigate to='/landing' replace />,
      },
    ],
  },
]);

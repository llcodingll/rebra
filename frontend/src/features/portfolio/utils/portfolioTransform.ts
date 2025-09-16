import type { Portfolio } from '../../../mocks/portfolio';
import type { PortfolioItem } from '../api/portfolioApi';

export const transformPortfolioData = (apiPortfolios: PortfolioItem[]): Portfolio[] => {
  return apiPortfolios.map(item => ({
    id: item.portfolioId.toString(),
    name: item.name,
    description: item.description,
    stockCount: item.registeredStockCount,
    return: `${item.totalReturnRate > 0 ? '+' : ''}${item.totalReturnRate.toFixed(1)}%`,
    createdDate: item.createdAt ? item.createdAt.split('T')[0] : '',
    returnPositive: item.totalReturnRate > 0,
    accountType: item.accountType,
  }));
};
import type { Portfolio, Stock } from '../../../entities/portfolio';
import type { PortfolioItem, PortfolioDetailResponse } from '../api/types';

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

export const transformPortfolioDetailToStocks = (portfolioDetailData: PortfolioDetailResponse): Stock[] => {
  console.log("=== transformPortfolioDetailToStocks 시작 ===");
  console.log("portfolioDetailData:", portfolioDetailData);

  const registeredStocks: Stock[] = portfolioDetailData.registeredStocks.map(stock => ({
    id: stock.stockCode,
    name: stock.stockName,
    code: stock.stockCode,
    quantity: stock.quantity,
    averagePrice: stock.purchasePrice,
    currentPrice: stock.currentPrice,
    totalValue: stock.quantity * stock.currentPrice,
    profitLoss: (stock.currentPrice - stock.purchasePrice) * stock.quantity,
    profitLossRate: ((stock.currentPrice - stock.purchasePrice) / stock.purchasePrice) * 100,
    type: 'registered' as const,
    targetWeight: stock.targetWeight,
    currentWeight: 0, // 계산 필요
    thresholdPercentage: stock.thresholdPercentage,
  }));

  const unregisteredStocks: Stock[] = portfolioDetailData.unregisteredStocks.map(stock => ({
    id: stock.stockCode,
    name: stock.stockName,
    code: stock.stockCode,
    quantity: stock.quantity,
    averagePrice: stock.purchasePrice,
    currentPrice: stock.currentPrice,
    totalValue: stock.quantity * stock.currentPrice,
    profitLoss: (stock.currentPrice - stock.purchasePrice) * stock.quantity,
    profitLossRate: ((stock.currentPrice - stock.purchasePrice) / stock.purchasePrice) * 100,
    type: 'unregistered' as const,
    targetWeight: 0,
    currentWeight: 0,
    thresholdPercentage: 0,
  }));

  const result = [...registeredStocks, ...unregisteredStocks];
  console.log("변환된 stocks:", result);
  console.log("미등록 주식 개수:", unregisteredStocks.length);

  return result;
};
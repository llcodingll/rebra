import { useState } from 'react';
import { motion } from 'motion/react';
import { TrendingUp } from 'lucide-react';
import styles from './BacktestCreationPage.module.css';
import BacktestSettings from '../../widgets/backtest/BacktestSettings';
import StockSearch from '../../widgets/backtest/StockSearch';
import MyPortfolio from '../../widgets/backtest/MyPortfolio';

interface Stock {
  name: string;
  code: string;
  price: string;
  change: string;
  changeType: 'positive' | 'negative';
  volume: string;
  sector?: string;
}

interface PortfolioItem {
  name: string;
  code: string;
  buyPrice: string;
  quantity: number;
  targetWeight: number;
  threshold: number;
}

const mockStocks: Stock[] = [
  {
    name: '삼성전자',
    code: '005930',
    price: '71,400원',
    change: '+1,200원 (+1.71%)',
    changeType: 'positive',
    volume: '12,345,678',
    sector: '반도체',
  },
  {
    name: 'SK하이닉스',
    code: '000660',
    price: '89,100원',
    change: '+2,100원 (+2.42%)',
    changeType: 'positive',
    volume: '8,765,432',
    sector: '반도체',
  },
  {
    name: '카카오',
    code: '035720',
    price: '48,950원',
    change: '-850원 (-1.71%)',
    changeType: 'negative',
    volume: '5,432,109',
    sector: 'IT서비스',
  },
  {
    name: 'NAVER',
    code: '035420',
    price: '189,500원',
    change: '+3,500원 (+1.88%)',
    changeType: 'positive',
    volume: '2,109,876',
    sector: 'IT서비스',
  },
];

const mockPortfolio: PortfolioItem[] = [
  {
    name: '삼성전자',
    code: '005930',
    buyPrice: '70,000원',
    quantity: 10,
    targetWeight: 40,
    threshold: 5,
  },
  {
    name: 'SK하이닉스',
    code: '000660',
    buyPrice: '85,000원',
    quantity: 5,
    targetWeight: 30,
    threshold: 5,
  },
];

interface BacktestCreationPageProps {
  onBack?: () => void;
}

// 가격 문자열을 숫자로 변환하는 유틸리티 함수
const parsePrice = (priceString: string): number => {
  return parseInt(priceString.replace(/[^0-9]/g, '')) || 0;
};

// 숫자를 가격 문자열로 변환하는 유틸리티 함수
const formatPrice = (price: number): string => {
  return `${price.toLocaleString()}원`;
};

// 평가금액 계산 함수
const calculateValue = (buyPrice: string, quantity: number): number => {
  return parsePrice(buyPrice) * quantity;
};

export default function BacktestCreationPage({ onBack }: BacktestCreationPageProps = {}) {
  const [selectedPortfolio, setSelectedPortfolio] = useState('');
  const [backtestName, setBacktestName] = useState('');
  const [rebalancingPeriod] = useState('월간');
  const [startDate, setStartDate] = useState('2023-01');
  const [endDate, setEndDate] = useState('2024-12');
  const [searchTerm, setSearchTerm] = useState('');
  const [portfolioItems, setPortfolioItems] = useState<PortfolioItem[]>(mockPortfolio);

  const filteredStocks = mockStocks.filter(
    (stock) => stock.name.toLowerCase().includes(searchTerm.toLowerCase()) || stock.code.includes(searchTerm)
  );

  const handleAddToPortfolio = (stock: Stock) => {
    const existingItem = portfolioItems.find((item) => item.code === stock.code);
    if (existingItem) return;

    const newItem: PortfolioItem = {
      name: stock.name,
      code: stock.code,
      buyPrice: stock.price,
      quantity: 1,
      targetWeight: 10,
      threshold: 5,
    };

    setPortfolioItems([...portfolioItems, newItem]);
  };

  const handleRemoveFromPortfolio = (code: string) => {
    setPortfolioItems(portfolioItems.filter((item) => item.code !== code));
  };

  const handleRunBacktest = () => {
    console.log('백테스트 실행:', {
      name: backtestName,
      period: rebalancingPeriod,
      startDate,
      endDate,
      portfolio: portfolioItems,
    });
  };

  const totalValue = portfolioItems.reduce((sum, item) => {
    return sum + calculateValue(item.buyPrice, item.quantity);
  }, 0);

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <BacktestSettings
          backtestName={backtestName}
          setBacktestName={setBacktestName}
          rebalancingPeriod={rebalancingPeriod}
          startDate={startDate}
          setStartDate={setStartDate}
          endDate={endDate}
          setEndDate={setEndDate}
          onRunBacktest={handleRunBacktest}
          isRunDisabled={!backtestName || portfolioItems.length === 0}
        />

        <div className={styles.contentGrid}>
          <StockSearch
            searchTerm={searchTerm}
            setSearchTerm={setSearchTerm}
            filteredStocks={filteredStocks}
            portfolioItems={portfolioItems}
            onAddToPortfolio={handleAddToPortfolio}
          />

          <MyPortfolio
            portfolioItems={portfolioItems}
            setPortfolioItems={setPortfolioItems}
            selectedPortfolio={selectedPortfolio}
            totalValue={totalValue}
            onRemoveFromPortfolio={handleRemoveFromPortfolio}
            parsePrice={parsePrice}
            formatPrice={formatPrice}
            calculateValue={calculateValue}
          />
        </div>
      </div>
    </div>
  );
}

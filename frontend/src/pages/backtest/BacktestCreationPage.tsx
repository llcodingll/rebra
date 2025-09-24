import { useState, useEffect, useMemo, useRef, useCallback } from 'react';
import { useBlocker, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'motion/react';
import { TrendingUp } from 'lucide-react';
import styles from './BacktestCreationPage.module.css';
import BacktestSettings from '../../widgets/backtest/BacktestSettings';
import StockSearch from '../../widgets/backtest/StockSearch';
import MyPortfolio from '../../widgets/backtest/MyPortfolio';
import { searchStocksForBacktest, createBacktest, type BacktestCreateRequest } from '../../features/backtest/api/backtestApi';


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

// API에서 받은 데이터를 UI용 Stock 형태로 변환하는 함수
const transformApiDataToStock = (apiData: any): Stock => {
  const changeRate = apiData.changeRate || 0;
  const changeType = changeRate >= 0 ? 'positive' : 'negative';
  const changeAmount = Math.round(apiData.closePrice * changeRate / 100);

  return {
    name: apiData.name,
    code: apiData.ticker,
    price: `${apiData.closePrice?.toLocaleString() || 0}원`,
    change: `${changeRate >= 0 ? '+' : ''}${changeAmount.toLocaleString()}원 (${changeRate >= 0 ? '+' : ''}${changeRate.toFixed(2)}%)`,
    changeType,
    volume: apiData.volume?.toLocaleString() || '0',
    sector: '',
  };
};


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

  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [selectedPortfolio, setSelectedPortfolio] = useState('');
  const [backtestName, setBacktestName] = useState('');
  const [rebalancingType, setRebalancingType] = useState<'THRESHOLD' | 'PERIODIC'>('THRESHOLD');
  const [rebalancingPeriod] = useState('월간');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isBacktestExecuted, setIsBacktestExecuted] = useState(false);

  // 스크롤 위치 참조를 위한 ref들
  const contentGridRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' });
  }, []);

  // 3단계 완료 후 주식 검색 영역으로 자동 스크롤
  useEffect(() => {
    if (startDate && endDate && contentGridRef.current) {
      setTimeout(() => {
        contentGridRef.current?.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      }, 300);
    }
  }, [startDate, endDate]);

  const [searchTerm, setSearchTerm] = useState('');
  const [portfolioItems, setPortfolioItems] = useState<PortfolioItem[]>([]);

  // 다시 간단한 debounce로 변경 (안정적임)
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm.trim());
    }, 500);

    return () => clearTimeout(timer);
  }, [searchTerm]);

  // API로 주식 검색 (debounce된 검색어 사용)
  const queryKey = useMemo(() =>
    ['stockSearch', debouncedSearchTerm.toLowerCase(), startDate],
    [debouncedSearchTerm, startDate]
  );

  const { data: stockSearchData, isLoading: isSearchLoading, error: searchError } = useQuery({
    queryKey,
    queryFn: async () => {
      if (!debouncedSearchTerm.trim() || !startDate) return [];


      const result = await searchStocksForBacktest(debouncedSearchTerm.trim(), startDate);
      if (result.success) {
        // 새로운 응답 구조에 맞게 수정
        const responseData = result.data as any;

        // 거래일이 아닌 경우 메시지를 예외로 던짐
        if (!responseData.tradingDay) {
          const originalMessage = responseData.message || '거래일이 아닙니다';
          // 사용자 친화적인 메시지로 변환
          const friendlyMessage = originalMessage.includes('주말/공휴일')
            ? originalMessage.replace('은(는) 거래일이 아닙니다 (주말/공휴일)', '은(는) 주말 또는 공휴일이어서 거래일이 아닙니다')
            : originalMessage;
          throw new Error(friendlyMessage);
        }

        // 검색 결과가 없는 경우
        if (!responseData.data || responseData.data.length === 0) {
          if (responseData.message) {
            throw new Error(responseData.message);
          }
          return [];
        }

        return responseData.data.map(transformApiDataToStock);
      } else {
        throw new Error(result.error.message);
      }
    },
    enabled: Boolean(debouncedSearchTerm.trim() && startDate && debouncedSearchTerm.length >= 1),
    staleTime: 60000, // 1분
    gcTime: 300000, // 5분
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    retry: false, // 재시도 비활성화
  });

  const filteredStocks = stockSearchData || [];

  // debounce 중인지 확인
  const isDebouncing = searchTerm.trim() !== debouncedSearchTerm;

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

  // 백테스트 생성 mutation
  const createBacktestMutation = useMutation({
    mutationFn: createBacktest,
    onSuccess: (result) => {
      if (result.success) {
        // BacktestSettings에서 페이지 이동 처리
      } else {
        setIsBacktestExecuted(false);
      }
    },
    onError: (error: any) => {
      setIsBacktestExecuted(false);
    }
  });

  const handleRunBacktest = async () => {
    // 실행 시작 시 블락 해제
    setIsBacktestExecuted(true);

    // 포트폴리오 아이템들을 백엔드 형식으로 변환
    // 1. 비중 정규화 (총합을 100%로 맞춤)
    const totalWeight = portfolioItems.reduce((sum, item) => sum + item.targetWeight, 0);

    const stocks = portfolioItems.map(item => {
      // 정규화된 비중 계산
      const normalizedWeight = totalWeight > 0 ? (item.targetWeight / totalWeight) * 100 : 0;

      const stockData: any = {
        ticker: item.code,
        name: item.name,
        weight: normalizedWeight, // 정규화된 비중 사용
        shares: item.quantity
      };

      // 임계값 기반 리밸런싱일 때만 thresholdPercentage 추가
      if (rebalancingType === 'THRESHOLD') {
        stockData.thresholdPercentage = item.threshold / 100;
      }

      return stockData;
    });

    const request: BacktestCreateRequest = {
      testName: backtestName,
      startDate,
      endDate,
      rebalancingType,
      rebalancingPeriod: rebalancingType === 'PERIODIC' ? 'MONTHLY' : undefined,
      stocks
    };

    createBacktestMutation.mutate(request);
  };

  const handleNavigateToBacktestList = () => {
    navigate('/backtest');
  };

  const totalValue = portfolioItems.reduce((sum, item) => {
    return sum + calculateValue(item.buyPrice, item.quantity);
  }, 0);

  const blocker = useBlocker(
    ({ currentLocation, nextLocation }) =>
      currentLocation.pathname.includes('/backtest/create') &&
      currentLocation.pathname !== nextLocation.pathname &&
      !isBacktestExecuted &&
      !window.confirm('변경사항이 저장되지 않습니다. 정말로 페이지를 떠나시겠습니까?')
  );

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <BacktestSettings
          backtestName={backtestName}
          setBacktestName={setBacktestName}
          rebalancingType={rebalancingType}
          setRebalancingType={setRebalancingType}
          rebalancingPeriod={rebalancingPeriod}
          startDate={startDate}
          setStartDate={setStartDate}
          endDate={endDate}
          setEndDate={setEndDate}
          onRunBacktest={handleRunBacktest}
          isRunDisabled={!backtestName || !startDate || !endDate || portfolioItems.length === 0 || createBacktestMutation.isPending}
          onNavigateToBacktestList={handleNavigateToBacktestList}
          isCreating={createBacktestMutation.isPending}
          portfolioCount={portfolioItems.length}
        />

        {startDate && endDate && (
          <div ref={contentGridRef} className={styles.contentGrid}>
            <StockSearch
              searchTerm={searchTerm}
              setSearchTerm={setSearchTerm}
              filteredStocks={filteredStocks}
              portfolioItems={portfolioItems}
              onAddToPortfolio={handleAddToPortfolio}
              startDate={startDate}
              endDate={endDate}
              isLoading={isSearchLoading || isDebouncing}
              searchError={searchError}
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
              rebalancingType={rebalancingType}
            />
          </div>
        )}
      </div>
    </div>
  );
}

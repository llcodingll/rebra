export interface MarketIndex {
  indexName: string;
  currentPrice: string;
  changeAmount: string;
  changeRate: string;
}

export interface MarketIndexResponse {
  dataDate: string;
  requestDate: string;
  indices: MarketIndex[];
}
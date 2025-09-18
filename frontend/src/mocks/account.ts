export interface Account {
  accountId: number;
  accountNumber: string;
  accountType: 'REAL' | 'MOCK';
  brokerName: string;
  connectionStatus: 'CONNECTED' | 'DISCONNECTED';
  registeredAt: string;
}
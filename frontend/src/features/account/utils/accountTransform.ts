import type { Account } from '../../../mocks/account';
import type { AccountItem } from '../api/accountApi';

export const transformAccountData = (apiAccounts: AccountItem[]): Account[] => {
  return apiAccounts
    .filter(account => account.connected)
    .map(account => ({
      accountId: account.accountId,
      accountNumber: account.accountNumber,
      accountType: account.accountType as 'REAL' | 'MOCK',
      brokerName: account.brokerName,
      connectionStatus: 'CONNECTED' as const,
      registeredAt: account.registeredAt,
    }));
};
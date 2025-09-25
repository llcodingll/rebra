import { create } from 'zustand';

interface AccountState {
  accountId: number | null;
  setAccountId: (accountId: number) => void;
  clearAccountId: () => void;
}

const ACCOUNT_STORAGE_KEY = 'accountId';

// sessionStorage에서 초기값 로드
const getStoredAccountId = (): number | null => {
  try {
    const stored = sessionStorage.getItem(ACCOUNT_STORAGE_KEY);
    return stored ? Number(stored) : null;
  } catch (error) {
    console.error('Failed to load accountId from sessionStorage:', error);
    return null;
  }
};

// sessionStorage에 저장
const setStoredAccountId = (accountId: number | null): void => {
  try {
    if (accountId !== null) {
      sessionStorage.setItem(ACCOUNT_STORAGE_KEY, accountId.toString());
    } else {
      sessionStorage.removeItem(ACCOUNT_STORAGE_KEY);
    }
  } catch (error) {
    console.error('Failed to save accountId to sessionStorage:', error);
  }
};

export const useAccountStore = create<AccountState>((set) => ({
  accountId: getStoredAccountId(),
  setAccountId: (accountId: number) => {
    setStoredAccountId(accountId);
    set({ accountId });
  },
  clearAccountId: () => {
    setStoredAccountId(null);
    set({ accountId: null });
  },
}));

import { create } from 'zustand';

interface AccountState {
  accountId: number | null;
  setAccountId: (accountId: number) => void;
  clearAccountId: () => void;
}

export const useAccountStore = create<AccountState>((set) => ({
  accountId: null,
  setAccountId: (accountId: number) => set({ accountId }),
  clearAccountId: () => set({ accountId: null }),
}));
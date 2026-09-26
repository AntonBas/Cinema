import { useCallback, useRef } from "react";
import { useApi } from "@/hooks/common/useApi";
import { bonusApi } from "@/api/bonusApi";
import { DEFAULT_PAGE_SIZE_COMPACT } from "@/utils/paginationUtils";
import type {
  BonusBalanceResponse,
  BonusTransactionResponse,
} from "@/types/bonus";
import type { PageResponse } from "@/types/pagination";

export const useAdminUserBonus = (userId: number) => {
  const balanceApi = useApi<BonusBalanceResponse>();
  const transactionsApi = useApi<PageResponse<BonusTransactionResponse>>();

  const balanceExecuteRef = useRef(balanceApi.execute);
  const transactionsExecuteRef = useRef(transactionsApi.execute);
  balanceExecuteRef.current = balanceApi.execute;
  transactionsExecuteRef.current = transactionsApi.execute;

  const getBalance = useCallback(() => {
    return balanceExecuteRef.current(
      () => bonusApi.admin.getUserBalance(userId),
      { showErrorNotification: false },
    );
  }, [userId]);

  const getTransactions = useCallback(
    (page: number = 0) => {
      return transactionsExecuteRef.current(() =>
        bonusApi.admin.getUserTransactions(userId, {
          page,
          size: DEFAULT_PAGE_SIZE_COMPACT,
        }),
      );
    },
    [userId],
  );

  return {
    balance: balanceApi.data,
    balanceLoading: balanceApi.loading,
    transactions: transactionsApi.data?.content || [],
    transactionsPage: transactionsApi.data,
    transactionsLoading: transactionsApi.loading,
    getBalance,
    getTransactions,
  };
};

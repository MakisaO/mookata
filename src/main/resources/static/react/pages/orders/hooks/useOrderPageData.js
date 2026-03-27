import { useAsync } from "../../../lib/hooks.js";
import { ordersService } from "../../../services/ordersService.js";

export function useOrderPageData(tableId, reloadKey) {
  return useAsync(() => ordersService.orderPage(tableId), [tableId, reloadKey]);
}

export function useOrderHistory(page, size) {
  return useAsync(() => ordersService.history(new URLSearchParams({ page, size }).toString()), [page, size]);
}

export function useOrderDetail(id) {
  return useAsync(() => ordersService.detail(id), [id]);
}

export function useCheckoutPage(tableId, reloadKey) {
  return useAsync(() => ordersService.checkoutPage(tableId), [tableId, reloadKey]);
}

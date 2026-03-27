import { fetchJson } from "../lib/api.js";

export const ordersService = {
  orderPage: (tableId) => fetchJson(`/api/orders/table/${tableId}`),
  create: (payload) => fetchJson("/api/orders", { method: "POST", body: JSON.stringify(payload) }),
  history: (search) => fetchJson(`/api/orders/history?${search}`),
  detail: (id) => fetchJson(`/api/orders/history/${id}`),
  checkoutPage: (tableId) => fetchJson(`/api/payments/checkout/table/${tableId}`),
  checkout: (tableId) => fetchJson(`/api/payments/checkout/table/${tableId}`, { method: "POST" }),
};

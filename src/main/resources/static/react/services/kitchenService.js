import { fetchJson } from "../lib/api.js";

export const kitchenService = {
  dashboard: () => fetchJson("/api/kitchen"),
  cookItem: (detailId) => fetchJson(`/api/kitchen/items/${detailId}/cook`, { method: "POST" }),
  serveItem: (detailId) => fetchJson(`/api/kitchen/items/${detailId}/serve`, { method: "POST" }),
  updateOrder: (orderId) => fetchJson(`/api/kitchen/orders/${orderId}/mass-update`, { method: "POST" }),
};

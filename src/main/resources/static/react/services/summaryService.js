import { fetchJson } from "../lib/api.js";

export const summaryService = {
  dashboard: () => fetchJson("/api/summary"),
  productSales: (id) => fetchJson(`/api/summary/product/${id}`),
};

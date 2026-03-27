import { fetchJson } from "../lib/api.js";

export const promotionsService = {
  list: () => fetchJson("/api/promotions"),
  products: () => fetchJson("/api/products"),
  getById: (id) => fetchJson(`/api/promotions/${id}`),
  create: (payload) => fetchJson("/api/promotions", { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload) => fetchJson(`/api/promotions/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  remove: (id) => fetchJson(`/api/promotions/${id}`, { method: "DELETE" }),
};

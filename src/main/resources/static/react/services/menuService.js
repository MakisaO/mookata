import { fetchJson } from "../lib/api.js";

export const menuService = {
  list: (search) => fetchJson(`/api/menu?${search}`),
  categories: () => fetchJson("/api/categories"),
  getProduct: (id) => fetchJson(`/api/products/${id}`),
  createProduct: (payload) => fetchJson("/api/products", { method: "POST", body: JSON.stringify(payload) }),
  updateProduct: (id, payload) => fetchJson(`/api/products/${id}`, { method: "POST", body: JSON.stringify(payload) }),
  deleteProduct: (id) => fetchJson(`/api/products/${id}/delete`, { method: "POST" }),
};

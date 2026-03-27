import { fetchJson } from "../lib/api.js";

export const tablesService = {
  list: () => fetchJson("/api/tables"),
  getById: (id) => fetchJson(`/api/tables/${id}`),
  create: (payload) => fetchJson("/api/tables", { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload) => fetchJson(`/api/tables/${id}`, { method: "POST", body: JSON.stringify(payload) }),
  reset: (id) => fetchJson(`/api/tables/${id}/reset`, { method: "POST" }),
  remove: (id) => fetchJson(`/api/tables/${id}/delete`, { method: "POST" }),
};

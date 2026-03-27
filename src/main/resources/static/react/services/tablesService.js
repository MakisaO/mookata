import { fetchJson } from "../lib/api.js";

export const tablesService = {
  list: () => fetchJson("/api/tables"),
  getById: (id) => fetchJson(`/api/tables/${id}`),
  create: (payload) => fetchJson("/api/tables", { method: "POST", body: JSON.stringify(payload) }),
  update: (id, payload) => fetchJson(`/api/tables/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  reset: (id) => fetchJson(`/api/tables/${id}/reset`, { method: "PATCH" }),
  remove: (id) => fetchJson(`/api/tables/${id}`, { method: "DELETE" }),
};

import { html } from "../lib/runtime.js";

export function EmptyState({ text }) {
  return html`<div className="app-card p-4 text-center text-muted">${text}</div>`;
}

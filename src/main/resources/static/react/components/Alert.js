import { html } from "../lib/runtime.js";

export function Alert({ error, success }) {
  return html`
    <div className="mb-3">
      ${success ? html`<div className="alert alert-success mb-2">${success}</div>` : null}
      ${error ? html`<div className="alert alert-danger mb-0">${error}</div>` : null}
    </div>
  `;
}

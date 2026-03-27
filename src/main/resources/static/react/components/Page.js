import { html } from "../lib/runtime.js";

export function Page({ title, eyebrow, actions, children, fluid = false }) {
  return html`
    <div className="app-shell">
      <div className=${fluid ? "container-fluid app-page" : "app-page"}>
        <div className="page-panel">
          <div className="d-flex justify-content-between align-items-start mb-4 gap-3 flex-wrap">
            <div>
              ${eyebrow ? html`<div className="page-heading">${eyebrow}</div>` : null}
              <h2 className="page-title">${title}</h2>
            </div>
            <div className="d-flex gap-2 flex-wrap">${actions}</div>
          </div>
          ${children}
        </div>
      </div>
    </div>
  `;
}

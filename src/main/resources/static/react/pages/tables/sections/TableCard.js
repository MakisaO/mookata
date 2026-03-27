import { html } from "../../../lib/runtime.js";
import { getTableActions, normalizeTableStatus, tableStatusLabel } from "../../../lib/table-status.js";

export function TableCard({ table, onReset, onDelete }) {
  const status = normalizeTableStatus(table.status);
  const actions = getTableActions(table.tableId, table.status);

  return html`
    <div className="col-md-4 col-lg-3">
      <div className=${`app-card table-card ${status} p-3 h-100`}>
        <div className="fw-bold fs-4 mb-2 text-center">โต๊ะ ${table.tableId}</div>
        <div className="badge text-bg-dark mb-3">${tableStatusLabel(table.status)}</div>
        <div className="d-grid gap-2">
          ${actions.map((action, index) => {
            if (action.kind === "label") return html`<div className=${action.className} key=${index}>${action.text}</div>`;
            if (action.kind === "link") return html`<a href=${action.href} className=${action.className} key=${index}>${action.text}</a>`;
            if (action.action === "reset") return html`<button className=${action.className} key=${index} onClick=${() => onReset(table.tableId)}>${action.text}</button>`;
            return html`<button className=${action.className} key=${index} onClick=${() => onDelete(table.tableId)}>${action.text}</button>`;
          })}
        </div>
      </div>
    </div>
  `;
}

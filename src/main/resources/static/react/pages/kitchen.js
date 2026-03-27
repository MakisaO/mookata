import { html, useState } from "../lib/runtime.js";
import { formatDateTime } from "../lib/format.js";
import { useAsync } from "../lib/hooks.js";
import { kitchenService } from "../services/kitchenService.js";
import { Alert } from "../components/Alert.js";
import { EmptyState } from "../components/EmptyState.js";
import { Page } from "../components/Page.js";

export function KitchenPage() {
  const [reloadKey, setReloadKey] = useState(0);
  const [message, setMessage] = useState({ error: "", success: "" });
  const { loading, error, data } = useAsync(() => kitchenService.dashboard(), [reloadKey]);

  async function action(handler) {
    try {
      const result = await handler();
      setMessage({ success: result.message, error: "" });
      setReloadKey((value) => value + 1);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page} title="เธซเนเธญเธเธเธฃเธฑเธง" eyebrow="เธเธฑเธ”เธเธฒเธฃเธชเธ–เธฒเธเธฐเธเธฒเธฃเธ—เธณเธญเธฒเธซเธฒเธฃ" actions=${html`<a href="/" className="btn btn-outline-secondary">เธซเธเนเธฒเธซเธฅเธฑเธ</a>`}>
      <${Alert} error=${error || message.error} success=${message.success} />
      ${loading
        ? html`<${EmptyState} text="เธเธณเธฅเธฑเธเนเธซเธฅเธ”เธเธดเธงเธเธฃเธฑเธง..." />`
        : html`
            <div className="row g-4">
              ${(data?.rounds || []).map((round) => html`
                <div className="col-xl-6" key=${round.orderId}>
                  <div className="app-card p-4 h-100">
                    <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
                      <div>
                        <h4 className="fw-bold mb-1">เธญเธญเน€เธ”เธญเธฃเน #${round.orderId}</h4>
                        <div className="text-muted">เนเธ•เนเธฐ ${round.tableId ?? "-"} | ${formatDateTime(round.orderDate)}</div>
                      </div>
                      <button className="btn btn-dark" onClick=${() => action(() => kitchenService.updateOrder(round.orderId))}>${round.actionLabel}</button>
                    </div>
                    ${(round.items || []).map((item) => html`
                      <div className="border-top py-2" key=${item.detailId}>
                        <div className="d-flex justify-content-between align-items-center gap-3 flex-wrap">
                          <div>
                            <div className="fw-semibold">${item.productName}</div>
                            <div className="text-muted small">เธเธณเธเธงเธ ${item.quantity} | ${item.itemStatus}</div>
                          </div>
                          <div className="d-flex gap-2">
                            ${item.itemStatus === "ordered" ? html`<button className="btn btn-sm btn-warning" onClick=${() => action(() => kitchenService.cookItem(item.detailId))}>เน€เธฃเธดเนเธกเธ—เธณ</button>` : null}
                            ${item.itemStatus === "cooking" ? html`<button className="btn btn-sm btn-success" onClick=${() => action(() => kitchenService.serveItem(item.detailId))}>เน€เธชเธดเธฃเนเธ</button>` : null}
                          </div>
                        </div>
                      </div>
                    `)}
                  </div>
                </div>
              `)}
            </div>
          `}
    <//>
  `;
}

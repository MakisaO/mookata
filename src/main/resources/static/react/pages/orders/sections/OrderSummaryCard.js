import { html } from "../../../lib/runtime.js";
import { formatMoney } from "../../../lib/format.js";

export function OrderSummaryCard({ aggregatedOrders }) {
  return html`
    <div className="app-card p-4 sticky-side">
      <h5 className="fw-bold mb-3">เธฃเธฒเธขเธเธฒเธฃเธ—เธตเนเธชเธฑเนเธเนเธฅเนเธง</h5>
      ${(aggregatedOrders || []).length
        ? html`${aggregatedOrders.map((item, index) => html`
            <div className="border-bottom py-2" key=${`${item.product?.productId || index}-${index}`}>
              <div className="fw-semibold">${item.product?.productName}</div>
              <div className="small text-muted">เธเธณเธเธงเธ ${item.quantity} x ${formatMoney(item.unitPrice)}</div>
            </div>
          `)}`
        : html`<div className="text-muted">เธขเธฑเธเนเธกเนเธกเธตเธฃเธฒเธขเธเธฒเธฃเธญเธฒเธซเธฒเธฃ</div>`}
    </div>
  `;
}

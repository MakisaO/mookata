import { html } from "../../../lib/runtime.js";
import { formatMoney } from "../../../lib/format.js";

export function OrderSummaryCard({ aggregatedOrders }) {
  return html`
    <div className="app-card p-4 sticky-side">
      <h5 className="fw-bold mb-3">รายการที่สั่งแล้ว</h5>
      ${(aggregatedOrders || []).length
        ? html`${aggregatedOrders.map((item, index) => html`
            <div className="border-bottom py-2" key=${`${item.product?.productId || index}-${index}`}>
              <div className="fw-semibold">${item.product?.productName}</div>
              <div className="small text-muted">จำนวน ${item.quantity} x ${formatMoney(item.unitPrice)} บาท</div>
            </div>
          `)}`
        : html`<div className="text-muted">ยังไม่มีรายการอาหารในโต๊ะนี้</div>`}
    </div>
  `;
}

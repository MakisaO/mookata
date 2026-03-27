import { html, useState } from "../../lib/runtime.js";
import { formatDateTime, formatMoney } from "../../lib/format.js";
import { normalizeTableStatus, tableStatusLabel } from "../../lib/table-status.js";
import { useAsync } from "../../lib/hooks.js";
import { ordersService } from "../../services/ordersService.js";
import { tablesService } from "../../services/tablesService.js";
import { Alert } from "../../components/Alert.js";
import { EmptyState } from "../../components/EmptyState.js";
import { Page } from "../../components/Page.js";
import { useCheckoutPage, useOrderDetail, useOrderHistory, useOrderPageData } from "./hooks/useOrderPageData.js";
import { OrderSummaryCard } from "./sections/OrderSummaryCard.js";

export function OrderSelectPage() {
  const { loading, error, data: tables } = useAsync(() => tablesService.list(), []);

  return html`
    <${Page}
      title="เน€เธฅเธทเธญเธเนเธ•เนเธฐเธชเธณเธซเธฃเธฑเธเธชเธฑเนเธเธญเธฒเธซเธฒเธฃ"
      eyebrow="เน€เธฃเธดเนเธกเธญเธญเน€เธ”เธญเธฃเน"
      actions=${html`
        <a href="/" className="btn btn-outline-secondary">เธซเธเนเธฒเธซเธฅเธฑเธ</a>
        <a href="/tables" className="btn btn-primary">เธเธฑเธ”เธเธฒเธฃเนเธ•เนเธฐ</a>
      `}
    >
      <${Alert} error=${error} />
      <div className="row g-3">
        ${loading
          ? html`<div className="col-12"><${EmptyState} text="เธเธณเธฅเธฑเธเนเธซเธฅเธ”เธเนเธญเธกเธนเธฅเนเธ•เนเธฐ..." /></div>`
          : (tables || []).map((table) => html`
              <div className="col-md-4 col-lg-3" key=${table.tableId}>
                <a href=${`/orders/${table.tableId}`} className="text-decoration-none text-reset">
                  <div className=${`app-card table-card ${normalizeTableStatus(table.status)} p-4 h-100`}>
                    <div className="fw-bold fs-4 text-center mb-2">เนเธ•เนเธฐ ${table.tableId}</div>
                    <div className="badge text-bg-dark mb-3">${tableStatusLabel(table.status)}</div>
                    <div className="text-center text-muted">เน€เธเนเธฒเธชเธนเนเธซเธเนเธฒเธชเธฑเนเธเธญเธฒเธซเธฒเธฃ</div>
                  </div>
                </a>
              </div>
            `)}
      </div>
    <//>
  `;
}

export function OrderPage({ tableId }) {
  const [reloadKey, setReloadKey] = useState(0);
  const [quantities, setQuantities] = useState({});
  const [message, setMessage] = useState({ error: "", success: "" });
  const { loading, error, data } = useOrderPageData(tableId, reloadKey);

  async function submitOrder(event) {
    event.preventDefault();
    const payload = Object.entries(quantities).reduce((result, [productId, quantity]) => {
      const parsed = Number(quantity);
      if (parsed > 0) result[productId] = parsed;
      return result;
    }, {});

    if (!Object.keys(payload).length) {
      setMessage({ success: "", error: "เธเธฃเธธเธ“เธฒเน€เธฅเธทเธญเธเน€เธกเธเธนเธญเธขเนเธฒเธเธเนเธญเธข 1 เธฃเธฒเธขเธเธฒเธฃ" });
      return;
    }

    try {
      const result = await ordersService.create({ tableId: Number(tableId), quantities: payload });
      setMessage({ success: result.message, error: "" });
      setQuantities({});
      setReloadKey((value) => value + 1);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page}
      title=${`เธเธณเธฅเธฑเธเธชเธฑเนเธเธญเธฒเธซเธฒเธฃ เนเธ•เนเธฐ ${tableId}`}
      eyebrow="เน€เธเธดเนเธกเธฃเธฒเธขเธเธฒเธฃเธญเธฒเธซเธฒเธฃ"
      actions=${html`
        <a href="/tables" className="btn btn-outline-secondary">เน€เธเธฅเธตเนเธขเธเนเธ•เนเธฐ</a>
        <a href=${`/payments/checkout/table/${tableId}`} className="btn btn-success">เนเธเธซเธเนเธฒเธเธณเธฃเธฐเน€เธเธดเธ</a>
      `}
      fluid=${true}
    >
      <${Alert} error=${error || message.error} success=${message.success} />
      ${loading
        ? html`<${EmptyState} text="เธเธณเธฅเธฑเธเนเธซเธฅเธ”เน€เธกเธเธน..." />`
        : html`
            <div className="row g-4">
              <div className="col-lg-8">
                <div className="row g-3">
                  ${(data?.products || []).map((product) => html`
                    <div className="col-md-6 col-xl-4" key=${product.productId}>
                      <div className="app-card p-3 h-100">
                        <div className="fw-bold mb-1">${product.productName}</div>
                        <div className="text-muted small mb-2">${product.productDetail || "-"}</div>
                        <div className="text-danger fw-bold mb-3">${formatMoney(product.productPrice)} เธเธฒเธ—</div>
                        <input className="form-control" type="number" min="0" value=${quantities[product.productId] || ""} onChange=${(e) => setQuantities({ ...quantities, [product.productId]: e.target.value })} placeholder="เธเธณเธเธงเธ" />
                      </div>
                    </div>
                  `)}
                </div>
                <div className="mt-4"><button className="btn btn-primary" onClick=${submitOrder}>เธขเธทเธเธขเธฑเธเธเธฒเธฃเธชเธฑเนเธเธญเธฒเธซเธฒเธฃ</button></div>
              </div>
              <div className="col-lg-4">
                <${OrderSummaryCard} aggregatedOrders=${data?.aggregatedOrders} />
              </div>
            </div>
          `}
    <//>
  `;
}

export function OrderHistoryPage() {
  const search = new URLSearchParams(window.location.search);
  const [page, setPage] = useState(Number(search.get("page") || 0));
  const size = Number(search.get("size") || 10);
  const { loading, error, data } = useOrderHistory(page, size);

  return html`
    <${Page} title="เธเธฃเธฐเธงเธฑเธ•เธดเธเธฒเธฃเธเธณเธฃเธฐเน€เธเธดเธ" eyebrow="เธเธฃเธฐเธงเธฑเธ•เธดเธเธดเธฅ" actions=${html`<a href="/" className="btn btn-secondary">เธเธฅเธฑเธเธซเธเนเธฒเธซเธฅเธฑเธ</a>`}>
      <${Alert} error=${error} />
      <div className="metric-strip p-4 mb-4">
        <div className="row g-3 align-items-center text-center">
          <div className="col-md-6">
            <div className="small opacity-75">เธเธณเธเธงเธเธญเธญเน€เธ”เธญเธฃเนเธ—เธฑเนเธเธซเธกเธ”</div>
            <div className="fs-2 fw-bold">${data?.totalItems || 0}</div>
          </div>
          <div className="col-md-1 d-none d-md-flex justify-content-center"><div className="metric-divider h-100"></div></div>
          <div className="col-md-5">
            <div className="small opacity-75">เธฃเธฒเธขเนเธ”เนเธฃเธงเธกเธ—เธฑเนเธเธซเธกเธ”</div>
            <div className="fs-2 fw-bold">${formatMoney(data?.grandTotal)} เธฟ</div>
          </div>
        </div>
      </div>
      <div className="app-card p-4">
        ${loading
          ? html`<div className="text-center py-4">เธเธณเธฅเธฑเธเนเธซเธฅเธ”เธเธฃเธฐเธงเธฑเธ•เธดเธเธดเธฅ...</div>`
          : html`
              <div className="table-responsive">
                <table className="table align-middle">
                  <thead>
                    <tr>
                      <th>เธงเธฑเธเธ—เธตเน/เน€เธงเธฅเธฒ</th>
                      <th>เนเธ•เนเธฐ</th>
                      <th>เธฃเธฒเธขเธเธฒเธฃเธญเธฒเธซเธฒเธฃ</th>
                      <th>เธขเธญเธ”เธชเธธเธ—เธเธด</th>
                      <th>เธเธฒเธฃเธเธฑเธ”เธเธฒเธฃ</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${(data?.orders || []).map((order) => html`
                      <tr key=${order.orderId}>
                        <td>${formatDateTime(order.orderDate)}</td>
                        <td><span className="badge text-bg-dark">เนเธ•เนเธฐ ${order.tableId ?? "-"}</span></td>
                        <td>${(order.items || []).join(", ") || "-"}</td>
                        <td className="fw-bold text-success">${formatMoney(order.totalAmount)} เธฟ</td>
                        <td><a href=${`/orders/history/${order.orderId}`} className="btn btn-sm btn-outline-primary">เธ”เธนเธฃเธฒเธขเธฅเธฐเน€เธญเธตเธขเธ”</a></td>
                      </tr>
                    `)}
                  </tbody>
                </table>
              </div>
              <div className="d-flex justify-content-center gap-2 flex-wrap">
                ${Array.from({ length: data?.totalPages || 0 }, (_, idx) => idx).map((idx) => html`<button className=${`btn btn-sm ${idx === page ? "btn-primary" : "btn-outline-primary"}`} onClick=${() => setPage(idx)}>${idx + 1}</button>`)}
              </div>
            `}
      </div>
    <//>
  `;
}

export function OrderDetailPage({ id }) {
  const { loading, error, data } = useOrderDetail(id);
  return html`
    <${Page} title=${`เธฃเธฒเธขเธฅเธฐเน€เธญเธตเธขเธ”เธเธดเธฅ #${id}`} eyebrow="เธฃเธฒเธขเธเธฒเธฃเธญเธฒเธซเธฒเธฃเนเธเธเธดเธฅ" actions=${html`<a href="/orders/history" className="btn btn-outline-secondary">เธเธฅเธฑเธ</a>`}>
      <${Alert} error=${error} />
      ${loading
        ? html`<${EmptyState} text="เธเธณเธฅเธฑเธเนเธซเธฅเธ”เธฃเธฒเธขเธฅเธฐเน€เธญเธตเธขเธ”เธเธดเธฅ..." />`
        : html`
            <div className="row g-4">
              <div className="col-lg-4">
                <div className="app-card p-4">
                  <div className="mb-2">เนเธ•เนเธฐ: <strong>${data?.tableId ?? "-"}</strong></div>
                  <div className="mb-2">เธงเธฑเธเธ—เธตเน: <strong>${formatDateTime(data?.orderDate)}</strong></div>
                  <div className="mb-2">เธชเธ–เธฒเธเธฐ: <strong>${data?.orderStatus || "-"}</strong></div>
                  <div>เธขเธญเธ”เธฃเธงเธก: <strong>${formatMoney(data?.totalAmount)} เธฟ</strong></div>
                </div>
              </div>
              <div className="col-lg-8">
                <div className="app-card p-4">
                  <div className="table-responsive">
                    <table className="table align-middle">
                      <thead><tr><th>เน€เธกเธเธน</th><th>เธเธณเธเธงเธ</th><th>เธฃเธฒเธเธฒเธ•เนเธญเธซเธเนเธงเธข</th><th>เธฃเธฒเธเธฒเธฃเธงเธก</th></tr></thead>
                      <tbody>
                        ${(data?.items || []).map((item, index) => html`<tr key=${`${item.productName}-${index}`}><td>${item.productName}</td><td>${item.quantity}</td><td>${formatMoney(item.unitPrice)}</td><td>${formatMoney(item.lineTotal)}</td></tr>`)}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          `}
    <//>
  `;
}

export function CheckoutPage({ tableId }) {
  const [message, setMessage] = useState({ error: "", success: "" });
  const { loading, error, data } = useCheckoutPage(tableId, message.success);

  async function processPayment() {
    if (!window.confirm("เธขเธทเธเธขเธฑเธเธเธณเธฃเธฐเน€เธเธดเธเนเธ•เนเธฐเธเธตเนเนเธเนเธซเธฃเธทเธญเนเธกเน")) return;
    try {
      const result = await ordersService.checkout(tableId);
      setMessage({ success: result.message, error: "" });
      window.setTimeout(() => {
        window.location.href = "/tables";
      }, 800);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page}
      title=${`เธเธณเธฃเธฐเน€เธเธดเธ เนเธ•เนเธฐ ${tableId}`}
      eyebrow="เธชเธฃเธธเธเธฃเธฒเธขเธเธฒเธฃเธเนเธญเธเธเธณเธฃเธฐเน€เธเธดเธ"
      actions=${html`
        <a href=${`/orders/${tableId}`} className="btn btn-outline-secondary">เธเธฅเธฑเธเนเธเธซเธเนเธฒเธชเธฑเนเธเธญเธฒเธซเธฒเธฃ</a>
        <a href="/tables" className="btn btn-primary">เธซเธเนเธฒเนเธ•เนเธฐ</a>
      `}
    >
      <${Alert} error=${error || message.error} success=${message.success} />
      ${loading
        ? html`<${EmptyState} text="เธเธณเธฅเธฑเธเนเธซเธฅเธ”เธเนเธญเธกเธนเธฅเธเธณเธฃเธฐเน€เธเธดเธ..." />`
        : html`
            <div className="row g-4">
              <div className="col-lg-8">
                <div className="app-card overflow-hidden">
                  <div className="checkout-header p-4"><h4 className="fw-bold mb-0">เธฃเธฒเธขเธเธฒเธฃเธญเธฒเธซเธฒเธฃเธ—เธตเนเธชเธฑเนเธ</h4></div>
                  <div className="p-4">
                    <div className="table-responsive">
                      <table className="table align-middle">
                        <thead><tr><th>เน€เธกเธเธน</th><th>เธเธณเธเธงเธ</th><th>เธฃเธฒเธเธฒเธ•เนเธญเธซเธเนเธงเธข</th><th>เธฃเธงเธก</th></tr></thead>
                        <tbody>
                          ${(data?.aggregatedOrders || []).map((item, index) => html`<tr key=${`${item.product?.productId || index}-${index}`}><td>${item.product?.productName}</td><td>${item.quantity}</td><td>${formatMoney(item.unitPrice)}</td><td>${formatMoney(item.totalPrice)}</td></tr>`)}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>
              <div className="col-lg-4">
                <div className="app-card p-4">
                  <div className="d-flex justify-content-between mb-2"><span>เธขเธญเธ”เธเนเธญเธเธฅเธ”</span><strong>${formatMoney(data?.originalTotal)} เธฟ</strong></div>
                  <div className="d-flex justify-content-between mb-2"><span>เธชเนเธงเธเธฅเธ”</span><strong className="text-danger">-${formatMoney(data?.discount)} เธฟ</strong></div>
                  ${(data?.promoMessages || []).length ? html`<ul className="small text-success ps-3">${data.promoMessages.map((item, index) => html`<li key=${index}>${item}</li>`)}</ul>` : null}
                  <hr />
                  <div className="d-flex justify-content-between fs-4 fw-bold mb-3"><span>เธขเธญเธ”เธชเธธเธ—เธเธด</span><span>${formatMoney(data?.finalTotal)} เธฟ</span></div>
                  <button className="btn btn-success w-100" onClick=${processPayment}>เธขเธทเธเธขเธฑเธเธเธฒเธฃเธเธณเธฃเธฐเน€เธเธดเธ</button>
                </div>
              </div>
            </div>
          `}
    <//>
  `;
}

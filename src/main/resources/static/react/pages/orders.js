import { html, useState } from "../lib/runtime.js";
import { fetchJson } from "../lib/api.js";
import { formatDateTime, formatMoney } from "../lib/format.js";
import { normalizeTableStatus, tableStatusLabel } from "../lib/table-status.js";
import { useAsync } from "../lib/hooks.js";
import { Alert } from "../components/Alert.js";
import { EmptyState } from "../components/EmptyState.js";
import { Page } from "../components/Page.js";

export function OrderSelectPage() {
  const { loading, error, data: tables } = useAsync(() => fetchJson("/api/tables"), []);

  return html`
    <${Page}
      title="เลือกโต๊ะสำหรับสั่งอาหาร"
      eyebrow="เริ่มออเดอร์"
      actions=${html`
        <a href="/" className="btn btn-outline-secondary">หน้าหลัก</a>
        <a href="/tables" className="btn btn-primary">จัดการโต๊ะ</a>
      `}
    >
      <${Alert} error=${error} />
      <div className="row g-3">
        ${loading
          ? html`<div className="col-12"><${EmptyState} text="กำลังโหลดข้อมูลโต๊ะ..." /></div>`
          : (tables || []).map((table) => html`
              <div className="col-md-4 col-lg-3" key=${table.tableId}>
                <a href=${`/orders/${table.tableId}`} className="text-decoration-none text-reset">
                  <div className=${`app-card table-card ${normalizeTableStatus(table.status)} p-4 h-100`}>
                    <div className="fw-bold fs-4 text-center mb-2">โต๊ะ ${table.tableId}</div>
                    <div className="badge text-bg-dark mb-3">${tableStatusLabel(table.status)}</div>
                    <div className="text-center text-muted">เข้าสู่หน้าสั่งอาหาร</div>
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
  const { loading, error, data } = useAsync(() => fetchJson(`/api/orders/table/${tableId}`), [tableId, reloadKey]);

  async function submitOrder(event) {
    event.preventDefault();
    const payload = Object.entries(quantities).reduce((result, [productId, quantity]) => {
      const parsed = Number(quantity);
      if (parsed > 0) result[productId] = parsed;
      return result;
    }, {});

    if (!Object.keys(payload).length) {
      setMessage({ success: "", error: "กรุณาเลือกเมนูอย่างน้อย 1 รายการ" });
      return;
    }

    try {
      const result = await fetchJson("/api/orders", {
        method: "POST",
        body: JSON.stringify({ tableId: Number(tableId), quantities: payload }),
      });
      setMessage({ success: result.message, error: "" });
      setQuantities({});
      setReloadKey((value) => value + 1);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page}
      title=${`กำลังสั่งอาหาร โต๊ะ ${tableId}`}
      eyebrow="เพิ่มรายการอาหาร"
      actions=${html`
        <a href="/tables" className="btn btn-outline-secondary">เปลี่ยนโต๊ะ</a>
        <a href=${`/payments/checkout/table/${tableId}`} className="btn btn-success">ไปหน้าชำระเงิน</a>
      `}
      fluid=${true}
    >
      <${Alert} error=${error || message.error} success=${message.success} />
      ${loading
        ? html`<${EmptyState} text="กำลังโหลดเมนู..." />`
        : html`
            <div className="row g-4">
              <div className="col-lg-8">
                <div className="row g-3">
                  ${(data?.products || []).map((product) => html`
                    <div className="col-md-6 col-xl-4" key=${product.productId}>
                      <div className="app-card p-3 h-100">
                        <div className="fw-bold mb-1">${product.productName}</div>
                        <div className="text-muted small mb-2">${product.productDetail || "-"}</div>
                        <div className="text-danger fw-bold mb-3">${formatMoney(product.productPrice)} บาท</div>
                        <input className="form-control" type="number" min="0" value=${quantities[product.productId] || ""} onChange=${(e) => setQuantities({ ...quantities, [product.productId]: e.target.value })} placeholder="จำนวน" />
                      </div>
                    </div>
                  `)}
                </div>
                <div className="mt-4"><button className="btn btn-primary" onClick=${submitOrder}>ยืนยันการสั่งอาหาร</button></div>
              </div>
              <div className="col-lg-4">
                <div className="app-card p-4 sticky-side">
                  <h5 className="fw-bold mb-3">รายการที่สั่งแล้ว</h5>
                  ${(data?.aggregatedOrders || []).length
                    ? html`${data.aggregatedOrders.map((item, index) => html`
                        <div className="border-bottom py-2" key=${`${item.product?.productId || index}-${index}`}>
                          <div className="fw-semibold">${item.product?.productName}</div>
                          <div className="small text-muted">จำนวน ${item.quantity} x ${formatMoney(item.unitPrice)}</div>
                        </div>
                      `)}`
                    : html`<div className="text-muted">ยังไม่มีรายการอาหาร</div>`}
                </div>
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
  const { loading, error, data } = useAsync(() => {
    const params = new URLSearchParams({ page, size });
    window.history.replaceState(null, "", `/orders/history?${params.toString()}`);
    return fetchJson(`/api/orders/history?${params.toString()}`);
  }, [page, size]);

  return html`
    <${Page} title="ประวัติการชำระเงิน" eyebrow="ประวัติบิล" actions=${html`<a href="/" className="btn btn-secondary">กลับหน้าหลัก</a>`}>
      <${Alert} error=${error} />
      <div className="metric-strip p-4 mb-4">
        <div className="row g-3 align-items-center text-center">
          <div className="col-md-6">
            <div className="small opacity-75">จำนวนออเดอร์ทั้งหมด</div>
            <div className="fs-2 fw-bold">${data?.totalItems || 0}</div>
          </div>
          <div className="col-md-1 d-none d-md-flex justify-content-center"><div className="metric-divider h-100"></div></div>
          <div className="col-md-5">
            <div className="small opacity-75">รายได้รวมทั้งหมด</div>
            <div className="fs-2 fw-bold">${formatMoney(data?.grandTotal)} ฿</div>
          </div>
        </div>
      </div>
      <div className="app-card p-4">
        ${loading
          ? html`<div className="text-center py-4">กำลังโหลดประวัติบิล...</div>`
          : html`
              <div className="table-responsive">
                <table className="table align-middle">
                  <thead>
                    <tr>
                      <th>วันที่/เวลา</th>
                      <th>โต๊ะ</th>
                      <th>รายการอาหาร</th>
                      <th>ยอดสุทธิ</th>
                      <th>การจัดการ</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${(data?.orders || []).map((order) => html`
                      <tr key=${order.orderId}>
                        <td>${formatDateTime(order.orderDate)}</td>
                        <td><span className="badge text-bg-dark">โต๊ะ ${order.tableId ?? "-"}</span></td>
                        <td>${(order.items || []).join(", ") || "-"}</td>
                        <td className="fw-bold text-success">${formatMoney(order.totalAmount)} ฿</td>
                        <td><a href=${`/orders/history/${order.orderId}`} className="btn btn-sm btn-outline-primary">ดูรายละเอียด</a></td>
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
  const { loading, error, data } = useAsync(() => fetchJson(`/api/orders/history/${id}`), [id]);
  return html`
    <${Page} title=${`รายละเอียดบิล #${id}`} eyebrow="รายการอาหารในบิล" actions=${html`<a href="/orders/history" className="btn btn-outline-secondary">กลับ</a>`}>
      <${Alert} error=${error} />
      ${loading
        ? html`<${EmptyState} text="กำลังโหลดรายละเอียดบิล..." />`
        : html`
            <div className="row g-4">
              <div className="col-lg-4">
                <div className="app-card p-4">
                  <div className="mb-2">โต๊ะ: <strong>${data?.tableId ?? "-"}</strong></div>
                  <div className="mb-2">วันที่: <strong>${formatDateTime(data?.orderDate)}</strong></div>
                  <div className="mb-2">สถานะ: <strong>${data?.orderStatus || "-"}</strong></div>
                  <div>ยอดรวม: <strong>${formatMoney(data?.totalAmount)} ฿</strong></div>
                </div>
              </div>
              <div className="col-lg-8">
                <div className="app-card p-4">
                  <div className="table-responsive">
                    <table className="table align-middle">
                      <thead><tr><th>เมนู</th><th>จำนวน</th><th>ราคาต่อหน่วย</th><th>ราคารวม</th></tr></thead>
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
  const { loading, error, data } = useAsync(() => fetchJson(`/api/payments/checkout/table/${tableId}`), [tableId, message.success]);

  async function processPayment() {
    if (!window.confirm("ยืนยันชำระเงินโต๊ะนี้ใช่หรือไม่")) return;
    try {
      const result = await fetchJson(`/api/payments/checkout/table/${tableId}`, { method: "POST" });
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
      title=${`ชำระเงิน โต๊ะ ${tableId}`}
      eyebrow="สรุปรายการก่อนชำระเงิน"
      actions=${html`
        <a href=${`/orders/${tableId}`} className="btn btn-outline-secondary">กลับไปหน้าสั่งอาหาร</a>
        <a href="/tables" className="btn btn-primary">หน้าโต๊ะ</a>
      `}
    >
      <${Alert} error=${error || message.error} success=${message.success} />
      ${loading
        ? html`<${EmptyState} text="กำลังโหลดข้อมูลชำระเงิน..." />`
        : html`
            <div className="row g-4">
              <div className="col-lg-8">
                <div className="app-card overflow-hidden">
                  <div className="checkout-header p-4"><h4 className="fw-bold mb-0">รายการอาหารที่สั่ง</h4></div>
                  <div className="p-4">
                    <div className="table-responsive">
                      <table className="table align-middle">
                        <thead><tr><th>เมนู</th><th>จำนวน</th><th>ราคาต่อหน่วย</th><th>รวม</th></tr></thead>
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
                  <div className="d-flex justify-content-between mb-2"><span>ยอดก่อนลด</span><strong>${formatMoney(data?.originalTotal)} ฿</strong></div>
                  <div className="d-flex justify-content-between mb-2"><span>ส่วนลด</span><strong className="text-danger">-${formatMoney(data?.discount)} ฿</strong></div>
                  ${(data?.promoMessages || []).length ? html`<ul className="small text-success ps-3">${data.promoMessages.map((item, index) => html`<li key=${index}>${item}</li>`)}</ul>` : null}
                  <hr />
                  <div className="d-flex justify-content-between fs-4 fw-bold mb-3"><span>ยอดสุทธิ</span><span>${formatMoney(data?.finalTotal)} ฿</span></div>
                  <button className="btn btn-success w-100" onClick=${processPayment}>ยืนยันการชำระเงิน</button>
                </div>
              </div>
            </div>
          `}
    <//>
  `;
}

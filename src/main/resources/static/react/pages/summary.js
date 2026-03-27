import { html } from "../lib/runtime.js";
import { formatDateTime, formatMoney } from "../lib/format.js";
import { useAsync } from "../lib/hooks.js";
import { summaryService } from "../services/summaryService.js";
import { Alert } from "../components/Alert.js";
import { EmptyState } from "../components/EmptyState.js";
import { Page } from "../components/Page.js";

export function SummaryPage() {
  const { loading, error, data } = useAsync(() => summaryService.dashboard(), []);

  function ProductList({ items, emptyText }) {
    return (items || []).length
      ? html`${items.map((item) => html`<a href=${`/summary/product/${item.productId}`} className="list-group-item list-group-item-action d-flex justify-content-between align-items-center" key=${item.productId}><span>${item.productName}</span><span className="badge text-bg-dark">${item.quantity}</span></a>`)}`
      : html`<div className="text-muted">${emptyText}</div>`;
  }

  return html`
    <${Page} title="สรุปยอดขาย (Sales Summary)" eyebrow="ภาพรวมการขาย" actions=${html`<a href="/" className="btn btn-secondary">กลับหน้าหลัก</a>`}>
      <${Alert} error=${error} />
      ${loading
        ? html`<${EmptyState} text="กำลังโหลดสรุปยอดขาย..." />`
        : html`
            <div className="row g-4 mb-4">
              <div className="col-md-6"><div className="app-card p-4 h-100"><div className="stat-card-icon icon-green"><i className="bi bi-currency-dollar"></i></div><div className="text-muted">รายได้รวมทั้งหมด</div><div className="fs-1 fw-bold text-success">${formatMoney(data?.totalRevenue)} ฿</div></div></div>
              <div className="col-md-6"><div className="app-card p-4 h-100"><div className="stat-card-icon icon-blue"><i className="bi bi-receipt"></i></div><div className="text-muted">จำนวนออเดอร์ที่ชำระแล้ว</div><div className="fs-1 fw-bold text-primary">${data?.totalOrders || 0}</div></div></div>
            </div>
            <div className="row g-4">
              <div className="col-lg-6"><div className="app-card p-4 h-100"><h4 className="fw-bold mb-3">5 อันดับเมนูขายดี</h4><div className="list-group list-group-flush"><${ProductList} items=${data?.topProducts} emptyText="ยังไม่มีข้อมูลเมนูขายดี" /></div></div></div>
              <div className="col-lg-6"><div className="app-card p-4 h-100"><h4 className="fw-bold mb-3">5 อันดับเมนูขายน้อย</h4><div className="list-group list-group-flush"><${ProductList} items=${data?.leastProducts} emptyText="ยังไม่มีข้อมูลเมนูขายน้อย" /></div></div></div>
            </div>
          `}
    <//>
  `;
}

export function ProductSalesPage({ id }) {
  const { loading, error, data } = useAsync(() => summaryService.productSales(id), [id]);
  return html`
    <${Page} title=${data?.productName || `สินค้า ${id}`} eyebrow="ประวัติการขายสินค้า" actions=${html`<a href="/summary" className="btn btn-outline-secondary">กลับ</a>`}>
      <${Alert} error=${error} />
      ${loading
        ? html`<${EmptyState} text="กำลังโหลดประวัติการขาย..." />`
        : html`
            <div className="row g-4 mb-4">
              <div className="col-md-4"><div className="app-card p-4 h-100"><div className="text-muted">จำนวนที่ขายได้</div><div className="fs-2 fw-bold">${data?.totalSold || 0}</div></div></div>
              <div className="col-md-8"><div className="app-card p-4 h-100"><div className="text-muted">รายได้รวม</div><div className="fs-2 fw-bold">${formatMoney(data?.totalRevenue)} ฿</div><div className="text-muted mt-2">${data?.productDetail || "-"}</div></div></div>
            </div>
            <div className="app-card p-4">
              <div className="table-responsive">
                <table className="table align-middle">
                  <thead><tr><th>ออเดอร์</th><th>โต๊ะ</th><th>วันที่</th><th>จำนวน</th><th>ราคาต่อหน่วย</th><th>รวม</th></tr></thead>
                  <tbody>${(data?.salesHistory || []).map((item, index) => html`<tr key=${`${item.orderId}-${index}`}><td>#${item.orderId}</td><td>${item.tableId ?? "-"}</td><td>${formatDateTime(item.orderDate)}</td><td>${item.quantity}</td><td>${formatMoney(item.unitPrice)}</td><td>${formatMoney(item.lineTotal)}</td></tr>`)}</tbody>
                </table>
              </div>
            </div>
          `}
    <//>
  `;
}

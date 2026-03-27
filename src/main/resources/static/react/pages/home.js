import { html } from "../lib/runtime.js";

export function HomePage() {
  const tiles = [
    ["/tables", "tile-tables", "bi-shop", "จัดการร้านค้า"],
    ["/menu", "tile-menu", "bi-book", "จัดการเมนู"],
    ["/kitchen", "tile-kitchen", "bi-fire", "ห้องครัว (ออเดอร์)"],
    ["/promotion", "tile-promo", "bi-tags", "จัดการโปรโมชั่น"],
    ["/orders/history", "tile-history", "bi-clock-history", "ประวัติบิล"],
    ["/summary", "tile-summary", "bi-bar-chart-line", "สรุปยอดขาย"],
  ];

  return html`
    <div className="app-shell">
      <div className="app-page">
        <div className="page-panel">
          <div className="dashboard-hero">
            <div className="dashboard-logo"><span className="logo-star">STAR</span></div>
            <h1 className="fw-bold text-dark mb-2">The Star MooKaTa</h1>
            <p className="dashboard-subtitle mb-0">ระบบจัดการร้านอาหารและจุดชำระเงิน (POS)</p>
          </div>
          <div className="row g-3 dashboard-grid">
            ${tiles.map(
              ([href, colorClass, icon, label]) => html`
                <div className="col-md-4 col-sm-6" key=${href}>
                  <a href=${href} className=${`dashboard-tile ${colorClass}`}>
                    <i className=${`bi ${icon} fs-1`}></i>
                    <span className="fs-5 fw-semibold text-center px-2">${label}</span>
                  </a>
                </div>
              `
            )}
          </div>
        </div>
      </div>
    </div>
  `;
}

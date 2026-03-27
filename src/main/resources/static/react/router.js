import { html, useEffect } from "./lib/runtime.js";
import { Page } from "./components/Page.js";
import { EmptyState } from "./components/EmptyState.js";
import { HomePage } from "./pages/home/index.js";
import { TablesListPage, TableFormPage } from "./pages/tables/index.js";
import { MenuFormPage, MenuListPage } from "./pages/menu/index.js";
import { CheckoutPage, OrderDetailPage, OrderHistoryPage, OrderPage, OrderSelectPage } from "./pages/orders/index.js";
import { PromotionFormPage, PromotionsListPage } from "./pages/promotions/index.js";
import { KitchenPage } from "./pages/kitchen/index.js";
import { ProductSalesPage, SummaryPage } from "./pages/summary/index.js";

function NotFoundPage() {
  return html`
    <${Page} title="ไม่พบหน้าที่ต้องการ" actions=${html`<a href="/" className="btn btn-primary">กลับหน้าหลัก</a>`}>
      <${EmptyState} text="เส้นทางนี้ยังไม่ได้ผูกกับหน้า React" />
    <//>
  `;
}

function RedirectPage({ to }) {
  useEffect(() => {
    window.location.replace(to);
  }, [to]);
  return html`<${Page} title="กำลังเปลี่ยนหน้า"><${EmptyState} text="กำลังนำทาง..." /><//>`;
}

export function resolveRoute(pathname) {
  const routes = [
    { pattern: /^\/$/, render: () => html`<${HomePage} />` },
    { pattern: /^\/tables$/, render: () => html`<${TablesListPage} />` },
    { pattern: /^\/tables\/new$/, render: () => html`<${TableFormPage} />` },
    { pattern: /^\/tables\/edit\/(\d+)$/, render: ([, id]) => html`<${TableFormPage} id=${id} />` },
    { pattern: /^\/tables\/(\d+)$/, render: ([, id]) => html`<${RedirectPage} to=${`/orders/${id}`} />` },
    { pattern: /^\/menu$/, render: () => html`<${MenuListPage} />` },
    { pattern: /^\/menu\/new$/, render: () => html`<${MenuFormPage} />` },
    { pattern: /^\/menu\/edit\/(\d+)$/, render: ([, id]) => html`<${MenuFormPage} id=${id} />` },
    { pattern: /^\/orders\/new$/, render: () => html`<${OrderSelectPage} />` },
    { pattern: /^\/orders\/history$/, render: () => html`<${OrderHistoryPage} />` },
    { pattern: /^\/orders\/history\/(\d+)$/, render: ([, id]) => html`<${OrderDetailPage} id=${id} />` },
    { pattern: /^\/orders\/(\d+)$/, render: ([, tableId]) => html`<${OrderPage} tableId=${tableId} />` },
    { pattern: /^\/payments\/checkout\/table\/(\d+)$/, render: ([, tableId]) => html`<${CheckoutPage} tableId=${tableId} />` },
    { pattern: /^\/promotion$/, render: () => html`<${PromotionsListPage} />` },
    { pattern: /^\/promotion\/new$/, render: () => html`<${PromotionFormPage} />` },
    { pattern: /^\/promotion\/edit\/(\d+)$/, render: ([, id]) => html`<${PromotionFormPage} id=${id} />` },
    { pattern: /^\/kitchen$/, render: () => html`<${KitchenPage} />` },
    { pattern: /^\/summary$/, render: () => html`<${SummaryPage} />` },
    { pattern: /^\/summary\/product\/(\d+)$/, render: ([, id]) => html`<${ProductSalesPage} id=${id} />` },
  ];

  for (const route of routes) {
    const match = pathname.match(route.pattern);
    if (match) return route.render(match);
  }

  return html`<${NotFoundPage} />`;
}

import { html, useState } from "../lib/runtime.js";
import { formatDateTime } from "../lib/format.js";
import { useAsync } from "../lib/hooks.js";
import { kitchenService } from "../services/kitchenService.js";
import { Alert } from "../components/Alert.js";
import { EmptyState } from "../components/EmptyState.js";
import { Page } from "../components/Page.js";

const statusLabels = {
  ordered: "รอเริ่มทำ",
  cooking: "กำลังทำ",
  served: "เสิร์ฟแล้ว",
};

function actionLabel(label) {
  if (label === "Start All") return "เริ่มทำทั้งหมด";
  if (label === "Move Ordered To Cooking") return "ย้ายรายการที่รอไปกำลังทำ";
  if (label === "Serve All") return "เสิร์ฟทั้งหมด";
  return label || "อัปเดตสถานะ";
}

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
    <${Page}
      title="ห้องครัว"
      eyebrow="จัดการคิวออเดอร์ที่ยังไม่เสิร์ฟ"
      actions=${html`<a href="/" className="btn btn-outline-secondary">กลับหน้าหลัก</a>`}
    >
      <${Alert} error=${error || message.error} success=${message.success} />
      ${loading
        ? html`<${EmptyState} text="กำลังโหลดรายการออเดอร์..." />`
        : !(data?.rounds || []).length
          ? html`<${EmptyState} text="ยังไม่มีออเดอร์ที่ต้องทำในครัว" />`
          : html`
              <div className="row g-4">
                ${(data.rounds || []).map((round) => html`
                  <div className="col-xl-6" key=${round.orderId}>
                    <div className="app-card p-4 h-100">
                      <div className="d-flex justify-content-between align-items-start gap-3 mb-3 flex-wrap">
                        <div>
                          <h4 className="fw-bold mb-1">ออเดอร์ #${round.orderId}</h4>
                          <div className="text-muted">โต๊ะ ${round.tableId ?? "-"} | ${formatDateTime(round.orderDate)}</div>
                        </div>
                        <button className="btn btn-dark" onClick=${() => action(() => kitchenService.updateOrder(round.orderId))}>
                          ${actionLabel(round.actionLabel)}
                        </button>
                      </div>

                      <div className="d-grid gap-2">
                        ${(round.items || []).map((item) => html`
                          <div className="border rounded-3 p-3" key=${item.detailId}>
                            <div className="d-flex justify-content-between align-items-center gap-3 flex-wrap">
                              <div>
                                <div className="fw-semibold">${item.productName}</div>
                                <div className="text-muted small">จำนวน ${item.quantity} | สถานะ ${statusLabels[item.itemStatus] || item.itemStatus}</div>
                              </div>
                              <div className="d-flex gap-2">
                                ${item.itemStatus === "ordered" ? html`<button className="btn btn-sm btn-warning" onClick=${() => action(() => kitchenService.cookItem(item.detailId))}>เริ่มทำ</button>` : null}
                                ${item.itemStatus === "cooking" ? html`<button className="btn btn-sm btn-success" onClick=${() => action(() => kitchenService.serveItem(item.detailId))}>เสิร์ฟแล้ว</button>` : null}
                              </div>
                            </div>
                          </div>
                        `)}
                      </div>
                    </div>
                  </div>
                `)}
              </div>
            `}
    <//>
  `;
}

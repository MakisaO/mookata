import { html, useEffect, useState } from "../lib/runtime.js";
import { formatDateTime, toDateTimeLocal } from "../lib/format.js";
import { useAsync } from "../lib/hooks.js";
import { promotionsService } from "../services/promotionsService.js";
import { Alert } from "../components/Alert.js";
import { EmptyState } from "../components/EmptyState.js";
import { Page } from "../components/Page.js";

function promoTypeLabel(type) {
  if (type === "percent") return "ส่วนลดเปอร์เซ็นต์";
  if (type === "baht") return "ส่วนลดเงินบาท";
  if (type === "add") return "ของแถม";
  return type || "-";
}

function promotionBenefit(promo) {
  if (promo.type === "percent") return `ลด ${promo.percent ?? promo.value ?? 0}%`;
  if (promo.type === "baht") return `ลด ${promo.value ?? 0} บาท`;
  if (promo.type === "add") return `แถม ${promo.freeProduct?.productName || "-"} x${promo.quantity || 0}`;
  return "-";
}

function promotionCondition(promo) {
  const conditions = [];
  if (promo.minspend != null) conditions.push(`ขั้นต่ำ ${promo.minspend} บาท`);
  if (promo.reqProduct?.productName) conditions.push(`ซื้อ ${promo.reqProduct.productName} x${promo.reqQuantity || 1}`);
  return conditions.length ? conditions.join(" | ") : "ไม่มีเงื่อนไขพิเศษ";
}

export function PromotionsListPage() {
  const [reloadKey, setReloadKey] = useState(0);
  const [message, setMessage] = useState({ error: "", success: "" });
  const { loading, error, data } = useAsync(() => promotionsService.list(), [reloadKey]);

  async function removePromotion(id) {
    if (!window.confirm("ลบโปรโมชั่นนี้ใช่หรือไม่")) return;
    try {
      const result = await promotionsService.remove(id);
      setMessage({ success: result.message || "ลบโปรโมชั่นเรียบร้อยแล้ว", error: "" });
      setReloadKey((value) => value + 1);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page}
      title="จัดการโปรโมชั่น"
      eyebrow="ดู แก้ไข และลบโปรโมชั่นที่ใช้งานในระบบ"
      actions=${html`
        <a href="/" className="btn btn-outline-secondary">กลับหน้าหลัก</a>
        <a href="/promotion/new" className="btn btn-success">+ เพิ่มโปรโมชั่น</a>
      `}
    >
      <${Alert} error=${error || message.error} success=${message.success} />
      ${loading
        ? html`<${EmptyState} text="กำลังโหลดโปรโมชั่น..." />`
        : !(data || []).length
          ? html`<${EmptyState} text="ยังไม่มีโปรโมชั่นในระบบ" />`
          : html`
              <div className="row g-3">
                ${(data || []).map((promo) => html`
                  <div className="col-lg-4" key=${promo.promotionId}>
                    <div className="app-card p-3 h-100">
                      <div className="d-flex justify-content-between align-items-start gap-2 mb-2">
                        <div className="fw-bold fs-5">${promo.name}</div>
                        <span className="promo-chip">${promoTypeLabel(promo.type)}</span>
                      </div>
                      <div className="text-muted small mb-3">${promo.detail || "-"}</div>
                      <div className="promo-meta mb-2"><strong>สิทธิประโยชน์:</strong> ${promotionBenefit(promo)}</div>
                      <div className="promo-meta mb-2"><strong>เงื่อนไข:</strong> ${promotionCondition(promo)}</div>
                      <div className="promo-meta mb-3"><strong>ช่วงเวลา:</strong> ${formatDateTime(promo.startDate)} - ${formatDateTime(promo.endDate)}</div>
                      <div className="d-flex gap-2">
                        <a href=${`/promotion/edit/${promo.promotionId}`} className="btn btn-sm btn-warning flex-fill">แก้ไข</a>
                        <button className="btn btn-sm btn-outline-danger flex-fill" onClick=${() => removePromotion(promo.promotionId)}>ลบ</button>
                      </div>
                    </div>
                  </div>
                `)}
              </div>
            `}
    <//>
  `;
}

export function PromotionFormPage({ id }) {
  const isEdit = Boolean(id);
  const { data: products } = useAsync(() => promotionsService.products(), []);
  const { data: promo } = useAsync(() => (isEdit ? promotionsService.getById(id) : Promise.resolve(null)), [id]);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    name: "",
    detail: "",
    type: "percent",
    startDate: "",
    endDate: "",
    value: "",
    percent: "",
    quantity: "",
    minspend: "",
    reqQuantity: "",
    freeProductId: "",
    reqProductId: "",
  });

  useEffect(() => {
    if (!promo) return;
    setForm({
      name: promo.name || "",
      detail: promo.detail || "",
      type: promo.type || "percent",
      startDate: toDateTimeLocal(promo.startDate),
      endDate: toDateTimeLocal(promo.endDate),
      value: promo.value ?? "",
      percent: promo.percent ?? "",
      quantity: promo.quantity ?? "",
      minspend: promo.minspend ?? "",
      reqQuantity: promo.reqQuantity ?? "",
      freeProductId: promo.freeProduct?.productId || "",
      reqProductId: promo.reqProduct?.productId || "",
    });
  }, [promo]);

  async function submit(event) {
    event.preventDefault();
    try {
      const payload = {
        promotionId: isEdit ? Number(id) : null,
        name: form.name,
        detail: form.detail,
        type: form.type,
        startDate: form.startDate || null,
        endDate: form.endDate || null,
        value: form.type === "baht" ? (form.value || null) : null,
        percent: form.type === "percent" ? (form.percent || null) : null,
        quantity: form.type === "add" ? (form.quantity || null) : null,
        minspend: form.minspend || null,
        reqQuantity: form.reqProductId ? (form.reqQuantity || null) : null,
        freeProduct: form.type === "add" && form.freeProductId ? { productId: Number(form.freeProductId) } : null,
        reqProduct: form.reqProductId ? { productId: Number(form.reqProductId) } : null,
      };
      if (isEdit) await promotionsService.update(id, payload);
      else await promotionsService.create(payload);
      window.location.href = "/promotion";
    } catch (err) {
      setError(err.message);
    }
  }

  async function remove() {
    if (!window.confirm("ลบโปรโมชั่นนี้ใช่หรือไม่")) return;
    try {
      await promotionsService.remove(id);
      window.location.href = "/promotion";
    } catch (err) {
      setError(err.message);
    }
  }

  const showPercent = form.type === "percent";
  const showBaht = form.type === "baht";
  const showAdd = form.type === "add";

  return html`
    <${Page}
      title=${isEdit ? "แก้ไขโปรโมชั่น" : "เพิ่มโปรโมชั่นใหม่"}
      eyebrow="กำหนดประเภท ส่วนลด เงื่อนไข และช่วงเวลาใช้งาน"
      actions=${html`<a href="/promotion" className="btn btn-outline-secondary">กลับหน้ารายการ</a>`}
    >
      <div className="app-card p-4">
        <${Alert} error=${error} />
        <form onSubmit=${submit}>
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label">ชื่อโปรโมชั่น</label>
              <input className="form-control" required value=${form.name} onChange=${(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="col-md-6">
              <label className="form-label">ประเภทโปรโมชั่น</label>
              <select className="form-select" value=${form.type} onChange=${(e) => setForm({ ...form, type: e.target.value })}>
                <option value="percent">ส่วนลดเปอร์เซ็นต์</option>
                <option value="baht">ส่วนลดเงินบาท</option>
                <option value="add">ของแถม</option>
              </select>
            </div>
            <div className="col-12">
              <label className="form-label">รายละเอียด</label>
              <textarea className="form-control" rows="2" value=${form.detail} onChange=${(e) => setForm({ ...form, detail: e.target.value })}></textarea>
            </div>
            <div className="col-md-6">
              <label className="form-label">วันเริ่มต้น</label>
              <input className="form-control" type="datetime-local" value=${form.startDate} onChange=${(e) => setForm({ ...form, startDate: e.target.value })} />
            </div>
            <div className="col-md-6">
              <label className="form-label">วันสิ้นสุด</label>
              <input className="form-control" type="datetime-local" value=${form.endDate} onChange=${(e) => setForm({ ...form, endDate: e.target.value })} />
            </div>

            ${showPercent ? html`
              <div className="col-md-6">
                <label className="form-label">ส่วนลด (%)</label>
                <input className="form-control" type="number" min="0" step="0.01" value=${form.percent} onChange=${(e) => setForm({ ...form, percent: e.target.value })} />
              </div>
            ` : null}

            ${showBaht ? html`
              <div className="col-md-6">
                <label className="form-label">ส่วนลด (บาท)</label>
                <input className="form-control" type="number" min="0" step="0.01" value=${form.value} onChange=${(e) => setForm({ ...form, value: e.target.value })} />
              </div>
            ` : null}

            ${showAdd ? html`
              <div className="col-md-6">
                <label className="form-label">สินค้าแถม</label>
                <select className="form-select" value=${form.freeProductId} onChange=${(e) => setForm({ ...form, freeProductId: e.target.value })}>
                  <option value="">เลือกสินค้าแถม</option>
                  ${(products || []).map((product) => html`<option value=${product.productId}>${product.productName}</option>`)}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">จำนวนของแถม</label>
                <input className="form-control" type="number" min="0" value=${form.quantity} onChange=${(e) => setForm({ ...form, quantity: e.target.value })} />
              </div>
            ` : null}

            <div className="col-md-6">
              <label className="form-label">ยอดขั้นต่ำ (บาท)</label>
              <input className="form-control" type="number" min="0" step="0.01" value=${form.minspend} onChange=${(e) => setForm({ ...form, minspend: e.target.value })} />
            </div>
            <div className="col-md-6">
              <label className="form-label">สินค้าที่กำหนดเงื่อนไข</label>
              <select className="form-select" value=${form.reqProductId} onChange=${(e) => setForm({ ...form, reqProductId: e.target.value })}>
                <option value="">ไม่กำหนด</option>
                ${(products || []).map((product) => html`<option value=${product.productId}>${product.productName}</option>`)}
              </select>
            </div>
            <div className="col-md-6">
              <label className="form-label">จำนวนสินค้าที่ต้องซื้อ</label>
              <input className="form-control" type="number" min="0" value=${form.reqQuantity} onChange=${(e) => setForm({ ...form, reqQuantity: e.target.value })} />
            </div>
          </div>

          <div className="d-flex gap-2 mt-4 flex-wrap">
            <button className="btn btn-success" type="submit">บันทึกข้อมูล</button>
            ${isEdit ? html`<button className="btn btn-outline-danger" type="button" onClick=${remove}>ลบโปรโมชั่น</button>` : null}
          </div>
        </form>
      </div>
    <//>
  `;
}

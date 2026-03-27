import { html, useEffect, useState } from "../lib/runtime.js";
import { formatDateTime, formatMoney, toDateTimeLocal } from "../lib/format.js";
import { useAsync } from "../lib/hooks.js";
import { promotionsService } from "../services/promotionsService.js";
import { Alert } from "../components/Alert.js";
import { Page } from "../components/Page.js";

export function PromotionsListPage() {
  const [reloadKey, setReloadKey] = useState(0);
  const [message, setMessage] = useState({ error: "", success: "" });
  const { loading, error, data } = useAsync(() => promotionsService.list(), [reloadKey]);

  async function removePromotion(id) {
    if (!window.confirm("ลบโปรโมชั่นนี้ใช่หรือไม่")) return;
    try {
      const result = await promotionsService.remove(id);
      setMessage({ success: result.message, error: "" });
      setReloadKey((value) => value + 1);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page} title="จัดการโปรโมชั่น" eyebrow="โปรโมชั่นทั้งหมด" actions=${html`<a href="/" className="btn btn-outline-secondary">หน้าหลัก</a><a href="/promotion/new" className="btn btn-success">+ เพิ่มโปรโมชั่นใหม่</a>`}>
      <${Alert} error=${error || message.error} success=${message.success} />
      <div className="row g-3">
        ${loading
          ? html`<div className="col-12 text-center py-4">กำลังโหลดโปรโมชั่น...</div>`
          : (data || []).map((promo) => html`
              <div className="col-lg-4" key=${promo.promotionId}>
                <div className="app-card p-3 h-100">
                  <div className="d-flex justify-content-between align-items-start gap-2 mb-2">
                    <div className="fw-bold fs-5">${promo.name}</div>
                    <span className="promo-chip">${promo.type}</span>
                  </div>
                  <div className="text-muted small mb-3">${promo.detail || "-"}</div>
                  <div className="promo-meta mb-2">เงื่อนไขขั้นต่ำ ${promo.minspend || 0} บาท</div>
                  <div className="promo-meta mb-2">${formatDateTime(promo.startDate)} - ${formatDateTime(promo.endDate)}</div>
                  <div className="d-flex gap-2">
                    <a href=${`/promotion/edit/${promo.promotionId}`} className="btn btn-sm btn-warning flex-fill">แก้ไข</a>
                    <button className="btn btn-sm btn-danger flex-fill" onClick=${() => removePromotion(promo.promotionId)}>ลบ</button>
                  </div>
                </div>
              </div>
            `)}
      </div>
    <//>
  `;
}

export function PromotionFormPage({ id }) {
  const isEdit = Boolean(id);
  const { data: products } = useAsync(() => promotionsService.products(), []);
  const { data: promo } = useAsync(() => (isEdit ? promotionsService.getById(id) : Promise.resolve(null)), [id]);
  const [error, setError] = useState("");
  const [form, setForm] = useState({ name: "", detail: "", type: "percent", startDate: "", endDate: "", value: "", percent: "", quantity: "", minspend: "", reqQuantity: "", freeProductId: "", reqProductId: "" });

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
        value: form.value || null,
        percent: form.percent || null,
        quantity: form.quantity || null,
        minspend: form.minspend || null,
        reqQuantity: form.reqQuantity || null,
        freeProduct: form.freeProductId ? { productId: Number(form.freeProductId) } : null,
        reqProduct: form.reqProductId ? { productId: Number(form.reqProductId) } : null,
      };
      if (isEdit) await promotionsService.update(id, payload);
      else await promotionsService.create(payload);
      window.location.href = "/promotion";
    } catch (err) {
      setError(err.message);
    }
  }

  return html`
    <${Page} title=${isEdit ? "แก้ไขโปรโมชั่น" : "เพิ่มโปรโมชั่นใหม่"} eyebrow="ฟอร์มโปรโมชั่น" actions=${html`<a href="/promotion" className="btn btn-outline-secondary">กลับ</a>`}>
      <div className="app-card p-4">
        <${Alert} error=${error} />
        <form onSubmit=${submit}>
          <div className="row g-3">
            <div className="col-md-6"><label className="form-label">ชื่อโปรโมชั่น</label><input className="form-control" value=${form.name} onChange=${(e) => setForm({ ...form, name: e.target.value })} required /></div>
            <div className="col-md-6"><label className="form-label">ประเภท</label><select className="form-select" value=${form.type} onChange=${(e) => setForm({ ...form, type: e.target.value })}><option value="percent">เปอร์เซ็นต์</option><option value="baht">บาท</option><option value="add">ของแถม</option></select></div>
            <div className="col-12"><label className="form-label">รายละเอียด</label><textarea className="form-control" rows="2" value=${form.detail} onChange=${(e) => setForm({ ...form, detail: e.target.value })}></textarea></div>
            <div className="col-md-6"><label className="form-label">วันเริ่มต้น</label><input className="form-control" type="datetime-local" value=${form.startDate} onChange=${(e) => setForm({ ...form, startDate: e.target.value })} /></div>
            <div className="col-md-6"><label className="form-label">วันสิ้นสุด</label><input className="form-control" type="datetime-local" value=${form.endDate} onChange=${(e) => setForm({ ...form, endDate: e.target.value })} /></div>
            <div className="col-md-6"><label className="form-label">สินค้าแถม</label><select className="form-select" value=${form.freeProductId} onChange=${(e) => setForm({ ...form, freeProductId: e.target.value })}><option value="">-- ไม่ระบุ --</option>${(products || []).map((product) => html`<option value=${product.productId}>${product.productName}</option>`)}</select></div>
            <div className="col-md-6"><label className="form-label">สินค้าที่กำหนด</label><select className="form-select" value=${form.reqProductId} onChange=${(e) => setForm({ ...form, reqProductId: e.target.value })}><option value="">-- ไม่ระบุ --</option>${(products || []).map((product) => html`<option value=${product.productId}>${product.productName}</option>`)}</select></div>
          </div>
          <div className="mt-4"><button className="btn btn-success" type="submit">บันทึก</button></div>
        </form>
      </div>
    <//>
  `;
}

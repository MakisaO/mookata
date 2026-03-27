import { html, useEffect, useState } from "../lib/runtime.js";
import { fetchJson } from "../lib/api.js";
import { useAsync } from "../lib/hooks.js";
import { getTableActions, normalizeTableStatus, tableStatusLabel } from "../lib/table-status.js";
import { Alert } from "../components/Alert.js";
import { EmptyState } from "../components/EmptyState.js";
import { Page } from "../components/Page.js";

export function TablesListPage() {
  const [reloadKey, setReloadKey] = useState(0);
  const [message, setMessage] = useState({ error: "", success: "" });
  const { loading, error, data: tables } = useAsync(() => fetchJson("/api/tables"), [reloadKey]);

  async function resetTable(id) {
    if (!window.confirm("คืนสถานะโต๊ะนี้เป็นว่างใช่หรือไม่")) return;
    try {
      const result = await fetchJson(`/api/tables/${id}/reset`, { method: "POST" });
      setMessage({ success: result.message, error: "" });
      setReloadKey((value) => value + 1);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  async function deleteTable(id) {
    if (!window.confirm("ลบโต๊ะนี้ใช่หรือไม่")) return;
    try {
      const result = await fetchJson(`/api/tables/${id}/delete`, { method: "POST" });
      setMessage({ success: result.message, error: "" });
      setReloadKey((value) => value + 1);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page}
      title="โต๊ะในร้าน (Table Management)"
      eyebrow="จัดการสถานะโต๊ะ"
      actions=${html`
        <a href="/" className="btn btn-outline-secondary">หน้าหลัก</a>
        <a href="/tables/new" className="btn btn-success">+ เพิ่มโต๊ะใหม่</a>
      `}
    >
      <${Alert} error=${error || message.error} success=${message.success} />
      <div className="row g-3">
        ${loading
          ? html`<div className="col-12"><${EmptyState} text="กำลังโหลดข้อมูลโต๊ะ..." /></div>`
          : (tables || []).map((table) => {
              const status = normalizeTableStatus(table.status);
              const actions = getTableActions(table.tableId, table.status);
              return html`
                <div className="col-md-4 col-lg-3" key=${table.tableId}>
                  <div className=${`app-card table-card ${status} p-3 h-100`}>
                    <div className="fw-bold fs-4 mb-2 text-center">โต๊ะ ${table.tableId}</div>
                    <div className="badge text-bg-dark mb-3">${tableStatusLabel(table.status)}</div>
                    <div className="d-grid gap-2">
                      ${actions.map((action, index) => {
                        if (action.kind === "label") return html`<div className=${action.className} key=${index}>${action.text}</div>`;
                        if (action.kind === "link") return html`<a href=${action.href} className=${action.className} key=${index}>${action.text}</a>`;
                        if (action.action === "reset") return html`<button className=${action.className} key=${index} onClick=${() => resetTable(table.tableId)}>${action.text}</button>`;
                        return html`<button className=${action.className} key=${index} onClick=${() => deleteTable(table.tableId)}>${action.text}</button>`;
                      })}
                    </div>
                  </div>
                </div>
              `;
            })}
      </div>
    <//>
  `;
}

export function TableFormPage({ id }) {
  const isEdit = Boolean(id);
  const { data } = useAsync(() => (isEdit ? fetchJson(`/api/tables/${id}`) : Promise.resolve({ status: "available" })), [id]);
  const [form, setForm] = useState({ status: "available" });
  const [error, setError] = useState("");

  useEffect(() => {
    if (data) setForm({ status: normalizeTableStatus(data.status) });
  }, [data]);

  async function submit(event) {
    event.preventDefault();
    try {
      await fetchJson(isEdit ? `/api/tables/${id}` : "/api/tables", { method: "POST", body: JSON.stringify(form) });
      window.location.href = "/tables";
    } catch (err) {
      setError(err.message);
    }
  }

  return html`
    <${Page}
      title=${isEdit ? `แก้ไขสถานะโต๊ะ ${id}` : "เพิ่มโต๊ะใหม่"}
      eyebrow="ฟอร์มจัดการโต๊ะ"
      actions=${html`<a href="/tables" className="btn btn-outline-secondary">กลับ</a>`}
    >
      <div className="app-card p-4" style=${{ maxWidth: "520px" }}>
        <${Alert} error=${error} />
        <form onSubmit=${submit}>
          <label className="form-label">สถานะโต๊ะ</label>
          <select className="form-select mb-3" value=${form.status} onChange=${(e) => setForm({ status: e.target.value })}>
            <option value="available">ว่าง</option>
            <option value="unavailable">มีลูกค้า</option>
            <option value="reserved">จองแล้ว</option>
            <option value="maintenance">ซ่อมบำรุง</option>
          </select>
          <button className="btn btn-success" type="submit">บันทึก</button>
        </form>
      </div>
    <//>
  `;
}

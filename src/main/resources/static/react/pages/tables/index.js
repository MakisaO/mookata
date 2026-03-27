import { html, useEffect, useState } from "../../lib/runtime.js";
import { normalizeTableStatus } from "../../lib/table-status.js";
import { tablesService } from "../../services/tablesService.js";
import { Alert } from "../../components/Alert.js";
import { EmptyState } from "../../components/EmptyState.js";
import { Page } from "../../components/Page.js";
import { useTablesPage } from "./hooks/useTablesPage.js";
import { TableCard } from "./sections/TableCard.js";

export function TablesListPage() {
  const [reloadKey, setReloadKey] = useState(0);
  const [message, setMessage] = useState({ error: "", success: "" });
  const { loading, error, data: tables } = useTablesPage(reloadKey);

  async function resetTable(id) {
    if (!window.confirm("คืนสถานะโต๊ะนี้เป็นว่างใช่หรือไม่")) return;
    try {
      const result = await tablesService.reset(id);
      setMessage({ success: result.message, error: "" });
      setReloadKey((value) => value + 1);
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  async function deleteTable(id) {
    if (!window.confirm("ลบโต๊ะนี้ใช่หรือไม่")) return;
    try {
      const result = await tablesService.remove(id);
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
          : (tables || []).map((table) => html`<${TableCard} key=${table.tableId} table=${table} onReset=${resetTable} onDelete=${deleteTable} />`)}
      </div>
    <//>
  `;
}

export function TableFormPage({ id }) {
  const isEdit = Boolean(id);
  const { data } = useTablesPage("__form__" + id);
  const [form, setForm] = useState({ status: "available" });
  const [error, setError] = useState("");

  useEffect(() => {
    if (isEdit && Array.isArray(data)) return;
  }, [data, isEdit]);

  useEffect(() => {
    if (!isEdit) return;
    tablesService.getById(id).then((table) => {
      setForm({ status: normalizeTableStatus(table.status) });
    }).catch((err) => setError(err.message));
  }, [id, isEdit]);

  async function submit(event) {
    event.preventDefault();
    try {
      if (isEdit) await tablesService.update(id, form);
      else await tablesService.create(form);
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

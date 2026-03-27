import { html, useEffect, useState } from "../lib/runtime.js";
import { formatMoney } from "../lib/format.js";
import { useAsync } from "../lib/hooks.js";
import { menuService } from "../services/menuService.js";
import { Alert } from "../components/Alert.js";
import { EmptyState } from "../components/EmptyState.js";
import { Page } from "../components/Page.js";

function statusLabel(status) {
  return status === "available" ? "พร้อมขาย" : "ไม่พร้อมขาย";
}

export function MenuListPage() {
  const initialSearch = new URLSearchParams(window.location.search);
  const [page, setPage] = useState(Number(initialSearch.get("page") || 1));
  const [keyword, setKeyword] = useState(initialSearch.get("keyword") || "");
  const [sortField, setSortField] = useState(initialSearch.get("sortField") || "productId");
  const [sortDir, setSortDir] = useState(initialSearch.get("sortDir") || "asc");
  const [message, setMessage] = useState({ error: "", success: "" });

  const { loading, error, data } = useAsync(() => {
    const search = new URLSearchParams({ page, sortField, sortDir });
    if (keyword) search.set("keyword", keyword);
    window.history.replaceState(null, "", `/menu?${search.toString()}`);
    return menuService.list(search.toString());
  }, [page, keyword, sortField, sortDir]);

  function toggleSort(field) {
    if (sortField === field) setSortDir(sortDir === "asc" ? "desc" : "asc");
    else {
      setSortField(field);
      setSortDir("asc");
    }
    setPage(1);
  }

  async function deleteProduct(id, name) {
    if (!window.confirm(`ลบเมนู "${name}" ใช่หรือไม่`)) return;
    try {
      const result = await menuService.deleteProduct(id);
      setMessage({ success: result.message || "ลบเมนูเรียบร้อยแล้ว", error: "" });
      window.location.reload();
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page}
      title="จัดการเมนูอาหาร"
      eyebrow="เพิ่ม แก้ไข และดูรายการเมนูทั้งหมด"
      actions=${html`
        <a href="/" className="btn btn-outline-secondary">กลับหน้าหลัก</a>
        <a href="/menu/new" className="btn btn-success">+ เพิ่มเมนูใหม่</a>
      `}
    >
      <${Alert} error=${error || message.error} success=${message.success} />
      <div className="app-card p-4">
        <div className="d-flex flex-wrap gap-3 justify-content-between align-items-center mb-3">
          <input
            className="form-control"
            style=${{ maxWidth: "360px" }}
            placeholder="ค้นหาชื่อเมนู..."
            value=${keyword}
            onChange=${(e) => {
              setKeyword(e.target.value);
              setPage(1);
            }}
          />
          <div className="text-muted small">ทั้งหมด ${data?.totalItems || 0} รายการ</div>
        </div>

        ${loading
          ? html`<${EmptyState} text="กำลังโหลดรายการเมนู..." />`
          : !(data?.products || []).length
            ? html`<${EmptyState} text="ไม่พบเมนูตามคำค้นหา" />`
            : html`
                <div className="table-responsive">
                  <table className="table align-middle">
                    <thead>
                      <tr>
                        <th><button className="btn btn-link p-0 text-decoration-none" onClick=${() => toggleSort("categories.categoriesName")}>หมวดหมู่</button></th>
                        <th><button className="btn btn-link p-0 text-decoration-none" onClick=${() => toggleSort("productName")}>ชื่อเมนู</button></th>
                        <th>รายละเอียด</th>
                        <th><button className="btn btn-link p-0 text-decoration-none" onClick=${() => toggleSort("productPrice")}>ราคา</button></th>
                        <th><button className="btn btn-link p-0 text-decoration-none" onClick=${() => toggleSort("productStatus")}>สถานะ</button></th>
                        <th className="text-end">จัดการ</th>
                      </tr>
                    </thead>
                    <tbody>
                      ${(data.products || []).map((product) => html`
                        <tr key=${product.productId}>
                          <td>${product.categories?.categoriesName || "-"}</td>
                          <td className="fw-semibold">${product.productName}</td>
                          <td>${product.productDetail || "-"}</td>
                          <td>${formatMoney(product.productPrice)} บาท</td>
                          <td>
                            <span className=${`badge ${product.productStatus === "available" ? "text-bg-success" : "text-bg-secondary"}`}>
                              ${statusLabel(product.productStatus)}
                            </span>
                          </td>
                          <td className="text-end">
                            <div className="d-inline-flex gap-2">
                              <a href=${`/menu/edit/${product.productId}`} className="btn btn-sm btn-warning">แก้ไข</a>
                              <button className="btn btn-sm btn-outline-danger" onClick=${() => deleteProduct(product.productId, product.productName)}>ลบ</button>
                            </div>
                          </td>
                        </tr>
                      `)}
                    </tbody>
                  </table>
                </div>
                ${(data?.totalPages || 0) > 1
                  ? html`
                      <div className="d-flex justify-content-center gap-2 flex-wrap mt-3">
                        ${Array.from({ length: data.totalPages }, (_, index) => index + 1).map((pageNumber) => html`
                          <button className=${`btn btn-sm ${pageNumber === page ? "btn-primary" : "btn-outline-primary"}`} onClick=${() => setPage(pageNumber)}>
                            ${pageNumber}
                          </button>
                        `)}
                      </div>
                    `
                  : null}
              `}
      </div>
    <//>
  `;
}

export function MenuFormPage({ id }) {
  const isEdit = Boolean(id);
  const { data: categories } = useAsync(() => menuService.categories(), []);
  const { data: product } = useAsync(() => (isEdit ? menuService.getProduct(id) : Promise.resolve(null)), [id]);
  const [form, setForm] = useState({
    categories: { categoriesId: "" },
    productName: "",
    productDetail: "",
    productPrice: "",
    productStatus: "available",
  });
  const [message, setMessage] = useState({ error: "", success: "" });

  useEffect(() => {
    if (!product) return;
    setForm({
      categories: { categoriesId: product.categories?.categoriesId || "" },
      productName: product.productName || "",
      productDetail: product.productDetail || "",
      productPrice: product.productPrice || "",
      productStatus: product.productStatus || "available",
    });
  }, [product]);

  async function submit(event) {
    event.preventDefault();
    try {
      const payload = {
        ...form,
        productPrice: Number(form.productPrice),
        categories: { categoriesId: Number(form.categories.categoriesId) },
      };
      if (isEdit) await menuService.updateProduct(id, payload);
      else await menuService.createProduct(payload);
      window.location.href = "/menu";
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  async function remove() {
    if (!window.confirm("ลบเมนูนี้ใช่หรือไม่")) return;
    try {
      await menuService.deleteProduct(id);
      window.location.href = "/menu";
    } catch (err) {
      setMessage({ success: "", error: err.message });
    }
  }

  return html`
    <${Page}
      title=${isEdit ? "แก้ไขเมนูอาหาร" : "เพิ่มเมนูใหม่"}
      eyebrow="กรอกข้อมูลเมนูให้ครบก่อนบันทึก"
      actions=${html`<a href="/menu" className="btn btn-outline-secondary">กลับหน้ารายการ</a>`}
    >
      <div className="app-card p-4" style=${{ maxWidth: "720px" }}>
        <${Alert} error=${message.error} success=${message.success} />
        <form onSubmit=${submit}>
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label">หมวดหมู่</label>
              <select className="form-select" required value=${form.categories.categoriesId} onChange=${(e) => setForm({ ...form, categories: { categoriesId: e.target.value } })}>
                <option value="">เลือกหมวดหมู่</option>
                ${(categories || []).map((cat) => html`<option value=${cat.categoriesId}>${cat.categoriesName}</option>`)}
              </select>
            </div>
            <div className="col-md-6">
              <label className="form-label">สถานะ</label>
              <select className="form-select" value=${form.productStatus} onChange=${(e) => setForm({ ...form, productStatus: e.target.value })}>
                <option value="available">พร้อมขาย</option>
                <option value="unavailable">ไม่พร้อมขาย</option>
              </select>
            </div>
            <div className="col-12">
              <label className="form-label">ชื่อเมนู</label>
              <input className="form-control" required value=${form.productName} onChange=${(e) => setForm({ ...form, productName: e.target.value })} />
            </div>
            <div className="col-12">
              <label className="form-label">รายละเอียด</label>
              <textarea className="form-control" rows="3" value=${form.productDetail} onChange=${(e) => setForm({ ...form, productDetail: e.target.value })}></textarea>
            </div>
            <div className="col-md-4">
              <label className="form-label">ราคา</label>
              <input className="form-control" type="number" min="0" step="0.01" required value=${form.productPrice} onChange=${(e) => setForm({ ...form, productPrice: e.target.value })} />
            </div>
          </div>

          <div className="d-flex gap-2 mt-4 flex-wrap">
            <button className="btn btn-success" type="submit">บันทึกข้อมูล</button>
            ${isEdit ? html`<button className="btn btn-outline-danger" type="button" onClick=${remove}>ลบเมนู</button>` : null}
          </div>
        </form>
      </div>
    <//>
  `;
}

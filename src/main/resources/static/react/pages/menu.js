import { html, useEffect, useState } from "../lib/runtime.js";
import { formatMoney } from "../lib/format.js";
import { useAsync } from "../lib/hooks.js";
import { menuService } from "../services/menuService.js";
import { Alert } from "../components/Alert.js";
import { Page } from "../components/Page.js";

export function MenuListPage() {
  const initialSearch = new URLSearchParams(window.location.search);
  const [page, setPage] = useState(Number(initialSearch.get("page") || 1));
  const [keyword, setKeyword] = useState(initialSearch.get("keyword") || "");
  const [sortField, setSortField] = useState(initialSearch.get("sortField") || "productId");
  const [sortDir, setSortDir] = useState(initialSearch.get("sortDir") || "asc");

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

  return html`
    <${Page}
      title="รายการเมนูอาหารในร้าน"
      eyebrow="จัดการเมนู"
      actions=${html`
        <a href="/" className="btn btn-outline-secondary">กลับหน้าหลัก</a>
        <a href="/menu/new" className="btn btn-success">+ เพิ่มเมนูอาหารใหม่</a>
      `}
    >
      <${Alert} error=${error} />
      <div className="app-card p-4">
        <input className="form-control mb-3" style=${{ maxWidth: "320px" }} placeholder="ค้นหาชื่อเมนู..." value=${keyword} onChange=${(e) => { setKeyword(e.target.value); setPage(1); }} />
        ${loading
          ? html`<div className="text-center py-4">กำลังโหลดเมนู...</div>`
          : html`
              <div className="table-responsive">
                <table className="table align-middle">
                  <thead>
                    <tr>
                      <th><button onClick=${() => toggleSort("categories.categoriesName")}>หมวดหมู่</button></th>
                      <th><button onClick=${() => toggleSort("productName")}>ชื่อเมนู</button></th>
                      <th>รายละเอียด</th>
                      <th><button onClick=${() => toggleSort("productPrice")}>ราคา</button></th>
                      <th><button onClick=${() => toggleSort("productStatus")}>สถานะ</button></th>
                      <th>จัดการ</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${(data?.products || []).map((product) => html`
                      <tr key=${product.productId}>
                        <td>${product.categories?.categoriesName || "-"}</td>
                        <td className="fw-semibold">${product.productName}</td>
                        <td>${product.productDetail || "-"}</td>
                        <td>${formatMoney(product.productPrice)}</td>
                        <td className="text-success fw-bold">${product.productStatus === "available" ? "พร้อมขาย" : "ไม่พร้อมขาย"}</td>
                        <td><a href=${`/menu/edit/${product.productId}`} className="btn btn-sm btn-warning">แก้ไข</a></td>
                      </tr>
                    `)}
                  </tbody>
                </table>
              </div>
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
  const [error, setError] = useState("");

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
      const payload = { ...form, categories: { categoriesId: Number(form.categories.categoriesId) } };
      if (isEdit) await menuService.updateProduct(id, payload);
      else await menuService.createProduct(payload);
      window.location.href = "/menu";
    } catch (err) {
      setError(err.message);
    }
  }

  return html`
    <${Page}
      title=${isEdit ? "แก้ไขเมนู" : "เพิ่มเมนูใหม่"}
      eyebrow="ฟอร์มเมนู"
      actions=${html`<a href="/menu" className="btn btn-outline-secondary">กลับ</a>`}
    >
      <div className="app-card p-4" style=${{ maxWidth: "680px" }}>
        <${Alert} error=${error} />
        <form onSubmit=${submit}>
          <label className="form-label">หมวดหมู่</label>
          <select className="form-select mb-3" value=${form.categories.categoriesId} onChange=${(e) => setForm({ ...form, categories: { categoriesId: e.target.value } })}>
            <option value="">เลือกหมวดหมู่</option>
            ${(categories || []).map((cat) => html`<option value=${cat.categoriesId}>${cat.categoriesName}</option>`)}
          </select>
          <label className="form-label">ชื่อเมนู</label>
          <input className="form-control mb-3" value=${form.productName} onChange=${(e) => setForm({ ...form, productName: e.target.value })} />
          <label className="form-label">รายละเอียด</label>
          <textarea className="form-control mb-3" rows="3" value=${form.productDetail} onChange=${(e) => setForm({ ...form, productDetail: e.target.value })}></textarea>
          <label className="form-label">ราคา</label>
          <input className="form-control mb-3" type="number" step="0.01" value=${form.productPrice} onChange=${(e) => setForm({ ...form, productPrice: e.target.value })} />
          <label className="form-label">สถานะ</label>
          <select className="form-select mb-3" value=${form.productStatus} onChange=${(e) => setForm({ ...form, productStatus: e.target.value })}>
            <option value="available">พร้อมขาย</option>
            <option value="unavailable">ไม่พร้อมขาย</option>
          </select>
          <button className="btn btn-success" type="submit">บันทึก</button>
        </form>
      </div>
    <//>
  `;
}

export function normalizeTableStatus(status) {
  if (status === "occupied") return "unavailable";
  return status || "unknown";
}

export function tableStatusLabel(status) {
  switch (normalizeTableStatus(status)) {
    case "available":
      return "ว่าง";
    case "unavailable":
      return "มีลูกค้า";
    case "reserved":
      return "จองแล้ว";
    case "maintenance":
      return "ซ่อมบำรุง";
    default:
      return "ไม่ทราบสถานะ";
  }
}

export function getTableActions(tableId, status) {
  const normalized = normalizeTableStatus(status);

  if (normalized === "maintenance") {
    return [
      { kind: "label", className: "table-service-off", text: "ไม่พร้อมบริการ" },
      { kind: "link", href: `/tables/edit/${tableId}`, className: "btn btn-warning", text: "จัดการสถานะ" },
      { kind: "danger", action: "delete", className: "btn btn-danger", text: "ลบโต๊ะ" },
    ];
  }

  if (normalized === "reserved") {
    return [
      { kind: "link", href: `/orders/${tableId}`, className: "btn btn-primary", text: "สั่งอาหาร" },
      { kind: "link", href: `/tables/edit/${tableId}`, className: "btn btn-warning", text: "จัดการสถานะ" },
      { kind: "link", href: `/payments/checkout/table/${tableId}`, className: "btn btn-success", text: "ชำระเงิน" },
      { kind: "secondary", action: "reset", className: "btn btn-secondary", text: "คืนโต๊ะว่าง" },
      { kind: "danger", action: "delete", className: "btn btn-danger", text: "ลบโต๊ะ" },
    ];
  }

  if (normalized === "unavailable") {
    return [
      { kind: "link", href: `/orders/${tableId}`, className: "btn btn-primary", text: "สั่งอาหาร" },
      { kind: "link", href: `/tables/edit/${tableId}`, className: "btn btn-warning", text: "จัดการสถานะ" },
      { kind: "link", href: `/payments/checkout/table/${tableId}`, className: "btn btn-success", text: "ชำระเงิน" },
      { kind: "secondary", action: "reset", className: "btn btn-secondary", text: "คืนโต๊ะว่าง" },
    ];
  }

  return [
    { kind: "link", href: `/orders/${tableId}`, className: "btn btn-primary", text: "สั่งอาหาร" },
    { kind: "link", href: `/tables/edit/${tableId}`, className: "btn btn-warning", text: "จัดการสถานะ" },
    { kind: "danger", action: "delete", className: "btn btn-danger", text: "ลบโต๊ะ" },
  ];
}

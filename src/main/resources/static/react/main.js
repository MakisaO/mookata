import { createRoot, html } from "./lib/runtime.js";
import { resolveRoute } from "./router.js";

createRoot(document.getElementById("root")).render(resolveRoute(window.location.pathname));

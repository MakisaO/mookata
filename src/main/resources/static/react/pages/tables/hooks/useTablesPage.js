import { useAsync } from "../../../lib/hooks.js";
import { tablesService } from "../../../services/tablesService.js";

export function useTablesPage(reloadKey) {
  return useAsync(() => tablesService.list(), [reloadKey]);
}

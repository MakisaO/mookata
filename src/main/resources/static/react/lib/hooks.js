import { useEffect, useState } from "./runtime.js";

export function useAsync(loader, deps) {
  const [state, setState] = useState({ loading: true, error: "", data: null });

  useEffect(() => {
    let active = true;
    setState({ loading: true, error: "", data: null });
    Promise.resolve()
      .then(loader)
      .then((data) => active && setState({ loading: false, error: "", data }))
      .catch((error) => active && setState({ loading: false, error: error.message, data: null }));

    return () => {
      active = false;
    };
  }, deps);

  return state;
}

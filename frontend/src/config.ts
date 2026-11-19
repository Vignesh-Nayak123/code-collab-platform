// Backend endpoints, resolved from build-time env vars (see .env.example).
// Unset -> same origin, which is what the vite dev proxy and the nginx image expect.

const stripTrailingSlash = (url: string) => url.replace(/\/+$/, '');

/** REST base URL, e.g. https://api.example.com/api/v1 or /api/v1. */
export const API_BASE_URL = stripTrailingSlash(import.meta.env.VITE_API_URL?.trim() || '/api/v1');

/** SockJS endpoint. SockJS requires an http(s) URL and upgrades to ws(s) itself. */
export function getWebSocketUrl(): string {
  const configured = import.meta.env.VITE_WS_URL?.trim();
  if (!configured) {
    return `${window.location.protocol}//${window.location.host}/ws`;
  }
  return stripTrailingSlash(configured.replace(/^ws(s?):\/\//i, 'http$1://'));
}

/**
 * Free hosts (e.g. Render) put idle backends to sleep, so the first request can
 * take a minute. When the API is on another origin, ping it as soon as the app
 * loads so it's warming up while the user types a room name.
 */
export function warmUpBackend(): void {
  if (!/^https?:\/\//i.test(API_BASE_URL)) return;
  const healthUrl = API_BASE_URL.replace(/\/v\d+$/, '') + '/health';
  fetch(healthUrl, { method: 'GET', mode: 'cors' }).catch(() => {
    // Best effort only.
  });
}

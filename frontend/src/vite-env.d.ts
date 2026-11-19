/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** REST API base incl. /api/v1, e.g. https://backend.onrender.com/api/v1. Unset = same origin. */
  readonly VITE_API_URL?: string;
  /** SockJS endpoint, e.g. https://backend.onrender.com/ws. Unset = same origin /ws. */
  readonly VITE_WS_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_CAPGO_MAESTRO_CI: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

import { defineConfig } from 'vite';

const maestroCi = process.env.CAPGO_MAESTRO_CI === '1';

export default defineConfig({
  define: {
    'import.meta.env.VITE_CAPGO_MAESTRO_CI': JSON.stringify(maestroCi ? 'true' : 'false'),
  },
  server: {
    open: true,
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
});

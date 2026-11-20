import type { CapacitorConfig } from '@capacitor/cli';

import pkg from './package.json';

const maestroCi = process.env.CAPGO_MAESTRO_CI === '1';

const config: CapacitorConfig = {
  appId: 'app.capgo.age.range',
  appName: '@capgo/capacitor-age-range',
  webDir: 'dist',
  plugins: {
    SplashScreen: {
      launchAutoHide: true,
    },
    CapacitorUpdater: {
      appId: 'app.capgo.age.range',
      autoUpdate: maestroCi ? false : true,
      autoSplashscreen: maestroCi ? false : true,
      directUpdate: maestroCi ? false : 'always',
      defaultChannel: 'production',
      version: pkg.version,
    },
  },
};

export default config;

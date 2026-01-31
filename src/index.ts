import { registerPlugin } from '@capacitor/core';

import type { AgeRangePlugin } from './definitions';

const AgeRange = registerPlugin<AgeRangePlugin>('AgeRange', {
  web: () => import('./web').then((m) => new m.AgeRangeWeb()),
});

export * from './definitions';
export { AgeRange };

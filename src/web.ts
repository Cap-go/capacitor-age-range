import { WebPlugin } from '@capacitor/core';

import type { AgeRangePlugin, AgeRangeResult, RequestAgeRangeOptions } from './definitions';

export class AgeRangeWeb extends WebPlugin implements AgeRangePlugin {
  async requestAgeRange(_options?: RequestAgeRangeOptions): Promise<AgeRangeResult> {
    throw this.unimplemented('requestAgeRange is not available on the web.');
  }

  async getPluginVersion(): Promise<{ version: string }> {
    return { version: 'web' };
  }
}

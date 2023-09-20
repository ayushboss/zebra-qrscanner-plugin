import { WebPlugin } from '@capacitor/core';

import type { QrScannerPluginPlugin } from './definitions';

export class QrScannerPluginWeb extends WebPlugin implements QrScannerPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

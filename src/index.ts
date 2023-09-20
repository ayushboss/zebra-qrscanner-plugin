import { registerPlugin } from '@capacitor/core';

import type { QrScannerPluginPlugin } from './definitions';

const QrScannerPlugin = registerPlugin<QrScannerPluginPlugin>('QrScannerPlugin', {
  web: () => import('./web').then(m => new m.QrScannerPluginWeb()),
});

export * from './definitions';
export { QrScannerPlugin };

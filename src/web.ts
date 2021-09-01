import { WebPlugin } from '@capacitor/core';
import { QrScannerPluginPlugin } from './definitions';

export class QrScannerPluginWeb extends WebPlugin implements QrScannerPluginPlugin {
  constructor() {
    super({
      name: 'QrScannerPlugin',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

const QrScannerPlugin = new QrScannerPluginWeb();

export { QrScannerPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(QrScannerPlugin);

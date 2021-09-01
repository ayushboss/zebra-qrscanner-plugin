declare module '@capacitor/core' {
  interface PluginRegistry {
    QrScannerPlugin: QrScannerPluginPlugin;
  }
}

export interface QrScannerPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}

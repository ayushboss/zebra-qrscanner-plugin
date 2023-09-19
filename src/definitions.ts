export interface QrScannerPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}

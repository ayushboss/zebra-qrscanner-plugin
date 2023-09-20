package com.seatzapp.qrscannerplugin;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.getcapacitor.JSObject;
import com.getcapacitor.CapacitorPlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;
import java.util.ArrayList;

@CapacitorPlugin
public class QrScannerPlugin extends Plugin implements IDcsSdkApiDelegate {

    public static SDKHandler sdkHandler;
    ArrayList<DCSScannerInfo> mScannerInfoList = new ArrayList<>();
    static int connectedScannerID = -1;

    public void load() {
        sdkHandler = new SDKHandler(getContext());
        sdkHandler.dcssdkSetDelegate(this);

        DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);

        int notifications_mask = 0;

        // We would like to subscribe to all scanner available/not-available events
        notifications_mask |=
            DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value |
            DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
        // We would like to subscribe to all scanner connection events
        notifications_mask |=
            DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value |
            DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
        // We would like to subscribe to all barcode events
        notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value;
        // subscribe to events set in notification mask
        sdkHandler.dcssdkSubsribeForEvents(notifications_mask);

        sdkHandler.dcssdkEnableAvailableScannersDetection(true);
    }

    @PluginMethod
    public void currentScanner(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("currentScanner", connectedScannerID);
        call.resolve(ret);
    }

    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo availableScanner) {
        mScannerInfoList.add(availableScanner);
        if (mScannerInfoList.size() > 0) {
            DCSScannerInfo reader = mScannerInfoList.get(0);
            dcssdkEventCommunicationSessionEstablished(reader);
        }
    }

    @Override
    public void dcssdkEventScannerDisappeared(int scannerID) {
        connectedScannerID = -1;
        JSObject ret = new JSObject();
        ret.put("newScannerId", connectedScannerID);
        notifyListeners("newScannerDetected", ret);
    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo activeScanner) {
        connectedScannerID = activeScanner.getScannerID();
        connectToScanner();
        JSObject ret = new JSObject();
        ret.put("newScannerId", connectedScannerID);
        notifyListeners("newScannerDetected", ret);
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int scannerID) {
        JSObject ret = new JSObject();
        ret.put("scannerId", scannerID);
        notifyListeners("scannerDisconnected", ret);
    }

    @Override
    public void dcssdkEventBarcode(final byte[] barcodeData, final int barcodeType, final int fromScannerID) {
        String code = new String(barcodeData);
        dataHandler.obtainMessage(Constants.BARCODE_RECEIVED, code).sendToTarget();
        JSObject ret = new JSObject();
        ret.put("code", code);
        notifyListeners("barcodeDetected", ret);
    }

    @Override
    public void dcssdkEventImage(byte[] bytes, int i) {}

    @Override
    public void dcssdkEventVideo(byte[] bytes, int i) {}

    @Override
    public void dcssdkEventBinaryData(byte[] bytes, int i) {}

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent var1) {}

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {}

    private final Handler dataHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.BARCODE_RECEIVED:
                    String code = (String) msg.obj;
                    break;
            }
        }
    };

    public void connectToScanner() {
        new ScannerConnectionAsyncTask(connectedScannerID).execute();
    }

    private class ScannerConnectionAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        private int scannerId;

        public ScannerConnectionAsyncTask(int scannerId) {
            this.scannerId = scannerId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            DCSSDKDefs.DCSSDK_RESULT result = DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE;
            if (sdkHandler != null) {
                result = sdkHandler.dcssdkEstablishCommunicationSession(scannerId);
            }

            if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS) {
                return true;
            } else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE) {
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            Intent returnIntent = new Intent();
            if (b) {
                returnIntent.putExtra(Constants.SCANNER_ID, scannerId);
            }
        }
    }
}

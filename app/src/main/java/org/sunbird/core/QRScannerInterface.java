package org.sunbird.core;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;

import java.io.UnsupportedEncodingException;
import java.util.List;

import in.juspay.mystique.DynamicUI;
import in.juspay.widget.qrscanner.com.google.zxing.BinaryBitmap;
import in.juspay.widget.qrscanner.com.google.zxing.LuminanceSource;
import in.juspay.widget.qrscanner.com.google.zxing.MultiFormatReader;
import in.juspay.widget.qrscanner.com.google.zxing.RGBLuminanceSource;
import in.juspay.widget.qrscanner.com.google.zxing.Reader;
import in.juspay.widget.qrscanner.com.google.zxing.Result;
import in.juspay.widget.qrscanner.com.google.zxing.ResultPoint;
import in.juspay.widget.qrscanner.com.google.zxing.client.android.BeepManager;
import in.juspay.widget.qrscanner.com.google.zxing.common.HybridBinarizer;
import in.juspay.widget.qrscanner.com.journeyapps.barcodescanner.BarcodeCallback;
import in.juspay.widget.qrscanner.com.journeyapps.barcodescanner.BarcodeResult;
import in.juspay.widget.qrscanner.com.journeyapps.barcodescanner.CaptureManager;
import in.juspay.widget.qrscanner.com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Created by JUSPAY\nikith.shetty on 31/10/17.
 */

public class QRScannerInterface {

    private static final String LOG_TAG = QRScannerInterface.class.getSimpleName();
    private static final int CAMERA_ACCESS_PERMISSION_REQ_CODE = 101;
    private Activity activity;
    private DynamicUI dynamicUI;
    private CaptureManager captureManager;
    private BeepManager beepManager;
    private String frameIDStr;


    public QRScannerInterface(final Activity activity, final DynamicUI dynamicUI) {
        this.activity = activity;

        this.dynamicUI = dynamicUI;
    }

    /**
     * Method for opening the QR scanner
     * @param frameIDStr - the Frame ID in which the scanner is opened
     */
    @JavascriptInterface
    public void openQRScanner(final String frameIDStr) {

        Log.d(LOG_TAG,"JSInterface openQRScanner Called");

        this.frameIDStr = frameIDStr;

        if (!isPermissionGranted()) {
            Log.d(LOG_TAG,"Requesting for Camera Access Permission.");
//            ActivityCompat.requestPermissions(activity,
//                    new String[]{Manifest.permission.CAMERA},
//                    CAMERA_ACCESS_PERMISSION_REQ_CODE);
        } else {
            openQRScanner();
        }
    }

    /**
     * Must be called to release the Camera
     */
    @JavascriptInterface
    public void closeQRScanner() throws NullPointerException{
        Log.d(LOG_TAG,"JSInterface closeQRScanner Called");

        if (captureManager != null) {
            Log.d(LOG_TAG,"JSInterface closeQRScanner 2 Called");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    captureManager.onPause();
                    captureManager.onDestroy();
                    captureManager = null;
                }
            });

        } else {

            Log.d(LOG_TAG,"JSInterface closeQRScanner 3 Called");
            Log.e(LOG_TAG, "ERROR: CaptureManager NULL!!");

        }
    }

    /**
     * Method to be called to release the Camera on Activity's onPause and onDestroy
     * @param event - event name
     */
    @JavascriptInterface
    public void captureManager(final String event) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (captureManager != null && !TextUtils.isEmpty(event)) {
                    switch (event) {
                        case "onResume":
                            captureManager.onResume();
                            break;
                        case "onPause":
                            captureManager.onPause();
                            break;
                        case "onDestroy":
                            captureManager.onDestroy();
                            break;
                    }
                }
            }
        });
    }

    public void onRequestPermissionResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (dynamicUI == null) {
            Log.e(LOG_TAG, "ERROR: Empty Dynamic UI!!");
            return;
        }
        switch (requestCode) {
            case CAMERA_ACCESS_PERMISSION_REQ_CODE :
                if (isPermissionGranted()) {
                    Log.d(LOG_TAG, "Camera Access Permission Granted.");
                    openQRScanner();
                }
                else {
                    Log.e(LOG_TAG, "ERROR: Camera Access Permission Not Granted!!");
                }
                break;
        }

    }

    public String scanQRImage(Bitmap bMap) throws UnsupportedEncodingException {
        String contents = null;
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        }
        catch (Exception e) {
            String wrongvpa=  "upi://pay?pa=invalid&pn=invalid image&tn=&am=00&cu=INR";
            dynamicUI.addJsToWebView("window.BarcodeResult(\"" + new String(Base64.encode(wrongvpa.toString().getBytes("UTF-8"), Base64.NO_WRAP),"UTF-8") + "\");");
            return contents;
        }
        dynamicUI.addJsToWebView("window.BarcodeResult(\"" + new String(Base64.encode(contents.toString().getBytes("UTF-8"), Base64.NO_WRAP),"UTF-8") + "\");");
        return contents;

    }

    public void openQRScanner() {
        if(captureManager != null) captureManager.onDestroy();
        if (!TextUtils.isEmpty(frameIDStr)) {
            final int frameID = Integer.parseInt(frameIDStr);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final DecoratedBarcodeView barcodeView = new DecoratedBarcodeView(activity);

                    final FrameLayout.LayoutParams layoutParams =
                            new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT);

                    barcodeView.setLayoutParams(layoutParams);
                    FrameLayout barcodeFrame = (FrameLayout) activity.findViewById(frameID);
                    if (barcodeFrame == null) {
                        return;
                    }
                    barcodeFrame.addView(barcodeView);
                    captureManager = new CaptureManager(activity, barcodeView);
                    captureManager.setBarcodeCallBack(new BarcodeCallback() {
                        @Override
                        public void barcodeResult(final BarcodeResult barcodeResult) {
                            if (beepManager == null)
                                beepManager = new BeepManager(activity);
                            beepManager.setBeepEnabled(true);
                            beepManager.playBeepSound();
                            if (dynamicUI != null) {
                                try {
                                    dynamicUI.addJsToWebView("window.BarcodeResult(\"" + new String(Base64.encode(barcodeResult.toString().getBytes("UTF-8"), Base64.NO_WRAP),"UTF-8") + "\");");
                                } catch (UnsupportedEncodingException e) {
                                    //Exception Unsupported Encoding Exception
                                }
                            }
                        }

                        @Override
                        public void possibleResultPoints(List<ResultPoint> list) {
                        }
                    });
                    captureManager.onResume();
                    captureManager.decode();
                }
            });
        }
        else {
            Log.e(LOG_TAG, "ERROR: Frame ID null!!");
        }

    }

    private boolean isPermissionGranted() {

        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

}


package com.example.campaignplus.campaign;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.util.List;

public class QRCodeScannerActivity extends Activity {

    private DecoratedBarcodeView barcodeView;
    private final String TAG = "QRCodeScannerActivity";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    0);
        }

        // Initialize the barcode scanner with modern zxing-android-embedded
        barcodeView = new DecoratedBarcodeView(this);
        setContentView(barcodeView);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Set up continuous decoding callback
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result == null || result.getResult() == null) {
                    return;
                }
                
                Intent intent = new Intent();
                intent.putExtra("QR_CODE", result.getText());
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Optional: Handle possible result points for visualization
            }
        });
        
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}

package com.example.turgo;

import android.app.Activity;
import android.graphics.Bitmap;

import androidx.activity.result.ActivityResultLauncher;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRCmanager {
    public static void scanCode(ActivityResultLauncher<ScanOptions> barLauncher){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }
    public static Bitmap generateQR(String text){
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 400, 400);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            return bitmap;
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

}

package com.example.turgo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class Tool {
    public static Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap;
        if(drawable instanceof BitmapDrawable){
            bitmap = ((BitmapDrawable)drawable).getBitmap();
        }else {
            // If the Drawable is not a BitmapDrawable (e.g., VectorDrawable), create a Bitmap from it
            bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );

            // Create a Canvas to draw the Drawable onto the Bitmap
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }
    public static Drawable getDrawableFromId(Context context, int id){
        return ContextCompat.getDrawable(context, id);
    }
}

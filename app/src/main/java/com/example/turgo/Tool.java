package com.example.turgo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

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

    public static boolean isTimeOccupied(LocalTime start, LocalDate date,  Meeting meeting){
        if(date == meeting.getDateOfMeeting()){
            if(start.isAfter(meeting.getStartTimeChange()) && start.isBefore(meeting.getEndTimeChange()) || start.equals(meeting.getStartTimeChange()) || start.equals(meeting.getEndTimeChange())){
                return false;
            }
        }
        return true;
    }
    public static boolean isTimeAvailable(LocalTime start, LocalTime end, LocalTime comparedToStart, LocalTime comparedToEnd){
        return !((start.equals(comparedToStart) || (start.isAfter(comparedToStart) && start.isBefore(comparedToEnd))) && (end.isBefore(comparedToEnd) || end.equals(comparedToEnd) && end.isAfter(comparedToStart)));
    }
    public static boolean isTimeOccupied(LocalTime start, LocalTime end, DayOfWeek day, Schedule schedule){
        if(day == schedule.getDay()){
            LocalTime comparedToStart = schedule.getMeetingStart();
            LocalTime comparedToEnd = schedule.getMeetingEnd();
            return !isTimeAvailable(start, end, comparedToStart, comparedToEnd);
        }
        return false;
    }
    public static boolean isShorter(Duration duration1, Duration duration2){
        return duration1.compareTo(duration2) < 0;
    }
    public static boolean isLonger(Duration duration1, Duration duration2){
        return duration1.compareTo(duration2) > 0;
    }
}

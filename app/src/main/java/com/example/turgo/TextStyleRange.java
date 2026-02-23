package com.example.turgo;

import android.graphics.Typeface;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.UUID;

public class TextStyleRange implements Serializable, RequireUpdate<TextStyleRange, TextStyleRange, TextStyleRangeRepository>, FirebaseClass<TextStyleRange> {
    public String id;
    public int start;
    public int end;
    public boolean bold;
    public boolean italic;
    public boolean underline;

    public TextStyleRange(){
        id = UUID.randomUUID().toString();
    }
    public TextStyleRange(int start, int end, boolean bold, boolean italic, boolean underline){
        id = UUID.randomUUID().toString();
        this.start = start;
        this.end = end;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }
    @Exclude
    public Object getStyleSpanFromTSR() {
        // No style at all
        if (!bold && !italic && !underline) {
            return null;
        }

        // Underline only
        if (!bold && !italic) {
            return new UnderlineSpan();
        }

        // Text style only
        if (bold && italic && !underline) {
            return new StyleSpan(Typeface.BOLD_ITALIC);
        }
        if (bold && !italic && !underline) {
            return new StyleSpan(Typeface.BOLD);
        }
        if (!bold && !underline) {
            return new StyleSpan(Typeface.ITALIC);
        }

        // Mixed text style + underline => return multiple spans
        if (bold && italic) {
            return new Object[] {
                    new StyleSpan(Typeface.BOLD_ITALIC),
                    new UnderlineSpan()
            };
        }
        if (bold) {
            return new Object[] {
                    new StyleSpan(Typeface.BOLD),
                    new UnderlineSpan()
            };
        }
        // (!bold && italic && underline)
        return new Object[] {
                new StyleSpan(Typeface.ITALIC),
                new UnderlineSpan()
        };
    }

    @Override
    public void importObjectData(TextStyleRange from) {
        this.id = from.id;
        this.start = from.start;
        this.end = from.end;
        this.bold = from.bold;
        this.italic = from.italic;
        this.underline = from.underline;
    }

    @Override
    public void convertToNormal(ObjectCallBack<TextStyleRange> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        objectCallBack.onObjectRetrieved(this);
    }

    @Override
    @Exclude
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.TEXT_STYLE_RANGE;
    }

    @Override
    @Exclude
    public Class<TextStyleRangeRepository> getRepositoryClass() {
        return TextStyleRangeRepository.class;
    }

    @Override
    @Exclude
    public Class<TextStyleRange> getFirebaseClass() {
        return TextStyleRange.class;
    }

    @Override
    @Exclude
    public String getID() {
        return id;
    }
}

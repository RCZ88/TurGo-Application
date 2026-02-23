package com.example.turgo;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class RichBody implements Serializable, RequireUpdate<RichBody, RichBodyFirebase, RichBodyRepository>{
    public String id;
    public String text;
    public ArrayList<TextStyleRange>spans;

    public RichBody(){
        id = UUID.randomUUID().toString();
    };

    public RichBody(String text, ArrayList<TextStyleRange>spans){
        id = UUID.randomUUID().toString();
        this.text = text;
        this.spans = spans;
    }

    public static RichBody extractRichBody(EditText etBody){
        Editable editable = etBody.getText();
        String plain = editable.toString();
        int n = plain.length();

        boolean[] bold = new boolean[n];
        boolean[] italic = new boolean[n];
        boolean[] underline = new boolean[n];

        StyleSpan[]styleSpans = editable.getSpans(0, n, StyleSpan.class);
        for(StyleSpan s :styleSpans){
            int st = Math.max(0, editable.getSpanStart(s));
            int en = Math.min(n, editable.getSpanEnd(s));
            int style = s.getStyle();
            boolean hasBold = (style == Typeface.BOLD || style == Typeface.BOLD_ITALIC);
            boolean hasItalic = (style == Typeface.ITALIC || style == Typeface.BOLD_ITALIC);

            for (int i = st; i < en; i++) {
                if(hasBold) bold[i] = true;
                if(hasItalic)italic[i] = true;
            }
        }

        UnderlineSpan[]uSpans = editable.getSpans(0, n, UnderlineSpan.class);
        for(UnderlineSpan s :uSpans){
            int st = Math.max(0, editable.getSpanStart(s));
            int en = Math.min(n, editable.getSpanEnd(s));
            for (int i = st; i < en; i++) underline[i] = true;
        }

        ArrayList<TextStyleRange> output = new ArrayList<>();

        int i = 0;
        while (i < n) {
            StyleCombination cur = new StyleCombination(bold[i], italic[i], underline[i]);

            int j = i + 1;
            while (j < n) {
                StyleCombination next = new StyleCombination(bold[j], italic[j], underline[j]);
                if (!StyleCombination.same(cur, next)) break;
                j++;
            }

            // [i, j)  -> end is EXCLUSIVE
            if (cur.rich()) {
                TextStyleRange tsr = new TextStyleRange(i, j, cur.bold, cur.italic, cur.underline);
                output.add(tsr);
            }

            i = j;
        }
        return new RichBody(plain, output);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<TextStyleRange> getSpans() {
        return spans;
    }

    public void setSpans(ArrayList<TextStyleRange> spans) {
        this.spans = spans;
    }

    public static void formatTv(TextView tv, RichBody rb){
        if (tv == null || rb == null) return;
        String text = rb.text;
        if (text == null) text = "";

        SpannableString spannableString = new SpannableString(text);
        if (rb.spans != null) for (TextStyleRange span : rb.spans) {
            if (span == null) continue;
            int start = Math.max(0, Math.min(span.start, spannableString.length()));
            int end = Math.max(start, Math.min(span.end, spannableString.length()));

            Object style = span.getStyleSpanFromTSR();
            if (style == null) continue;
            if(style instanceof Object[]){
                for(Object s : (Object[])style){
                    spannableString.setSpan(s, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }else{
                spannableString.setSpan(style, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        tv.setText(spannableString);
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.RICH_BODY;
    }

    @Override
    public Class<RichBodyRepository> getRepositoryClass() {
        return RichBodyRepository.class;
    }

    @Override
    public Class<RichBodyFirebase> getFirebaseClass() {
        return RichBodyFirebase.class;
    }

    @Override
    @Exclude
    public String getID() {
        return id;
    }
}

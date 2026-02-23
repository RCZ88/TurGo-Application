package com.example.turgo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class SimpleSpinnerAdapter<T> extends BaseAdapter {

    private final Context context;
    private final List<T> items;
    private final StringExtractor<T> extractor;

    public interface StringExtractor<T> {
        String extractName(T item);
    }

    public SimpleSpinnerAdapter(Context context, List<T> items, StringExtractor<T> extractor) {
        this.context = context;
        this.items = items;
        this.extractor = extractor;
    }

    @Override
    public int getCount() { return items.size(); }

    @Override
    public Object getItem(int position) { return items.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_simple_spinner, parent, false);
        }

        TextView tvText = convertView.findViewById(R.id.tv_spinner_text);
        tvText.setText(extractor.extractName(items.get(position)));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent); // Reuse the same simple view
    }
}

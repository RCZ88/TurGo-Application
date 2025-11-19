package com.example.turgo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User>{

    public UserAdapter(@NonNull Context context, ArrayList<User> users) {
        super(context, android.R.layout.simple_dropdown_item_1line);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        User user = getItem(position);
        if (user != null) {
            view.setText(user.getFullName()); // display username
        }
        return view;
    }
}

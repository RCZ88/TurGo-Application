package com.example.turgo;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LoadingBottomSheet extends BottomSheetDialogFragment {
    private String message;

    public static LoadingBottomSheet newInstance(String message) {
        LoadingBottomSheet fragment = new LoadingBottomSheet();
        Bundle args = new Bundle();
        args.putString("message", message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        if (getArguments() != null) {
            message = getArguments().getString("message");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_loading, container, false);
        TextView messageText = view.findViewById(R.id.tv_loading_message);
        messageText.setText(message);
        return view;
    }
}

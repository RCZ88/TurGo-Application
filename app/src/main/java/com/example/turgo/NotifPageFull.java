package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotifPageFull extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnClearAll;
    private LinearLayout llEmptyState;
    private RecyclerView rvNotifications;

    private NotifAdapter notifAdapter;
    private ArrayList<NotificationFirebase> notifications = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notif_page_full);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_NPF_parent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadNotifications();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        btnClearAll = findViewById(R.id.btn_npf_clearAll);
        llEmptyState = findViewById(R.id.ll_npf_notifsEmpty);
        rvNotifications = findViewById(R.id.rv_npf_notifications);
    }

    private void setupRecyclerView() {
        notifAdapter = new NotifAdapter(notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notifAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearAll.setOnClickListener(v -> {
            notifications.clear();
            notifAdapter.notifyDataSetChanged();
            updateEmptyState();
        });
    }

    private void loadNotifications() {
        // TODO: Replace this with your Firebase / repository logic
        // For now, this just updates the empty state
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (notifications.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
        }
    }
}

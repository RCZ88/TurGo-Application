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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class NotifPageFull extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnClearAll;
    private LinearLayout llEmptyState;
    private RecyclerView rvNotifications;
    private DatabaseReference currentUserRef;

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
            clearAllNotifications();
        });
    }

    private void loadNotifications() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (!Tool.boolOf(uid)) {
            updateEmptyState();
            return;
        }
        FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.USER_ID_ROLES.getPath())
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String role = null;
                        if (snapshot.hasChild("role")) {
                            role = snapshot.child("role").getValue(String.class);
                        }
                        if (!Tool.boolOf(role)) {
                            Object raw = snapshot.getValue();
                            if (raw instanceof String) {
                                role = (String) raw;
                            }
                        }
                        DatabaseReference userRef = resolveUserRef(uid, role);
                        if (userRef == null) {
                            updateEmptyState();
                            return;
                        }
                        currentUserRef = userRef;
                        loadNotificationsFromUserRef(userRef);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        updateEmptyState();
                    }
                });
    }

    private DatabaseReference resolveUserRef(String uid, String roleRaw) {
        if (!Tool.boolOf(uid) || !Tool.boolOf(roleRaw)) {
            return null;
        }
        String role = roleRaw.trim().toUpperCase();
        if ("STUDENT".equals(role)) {
            return FirebaseDatabase.getInstance().getReference(FirebaseNode.STUDENT.getPath()).child(uid);
        }
        if ("TEACHER".equals(role)) {
            return FirebaseDatabase.getInstance().getReference(FirebaseNode.TEACHER.getPath()).child(uid);
        }
        if ("PARENT".equals(role)) {
            return FirebaseDatabase.getInstance().getReference(FirebaseNode.PARENT.getPath()).child(uid);
        }
        if ("ADMIN".equals(role)) {
            return FirebaseDatabase.getInstance().getReference(FirebaseNode.ADMIN.getPath()).child(uid);
        }
        return null;
    }

    private void loadNotificationsFromUserRef(DatabaseReference userRef) {
        Set<String> ids = new LinkedHashSet<>();
        userRef.child("notitficationIDs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot typoSnapshot) {
                collectIds(typoSnapshot, ids);
                userRef.child("notificationIDs").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot fixedSnapshot) {
                        collectIds(fixedSnapshot, ids);
                        userRef.child("notifications").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot notificationsSnapshot) {
                                collectIds(notificationsSnapshot, ids);
                                resolveNotificationObjects(ids);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                resolveNotificationObjects(ids);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        userRef.child("notifications").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot notificationsSnapshot) {
                                collectIds(notificationsSnapshot, ids);
                                resolveNotificationObjects(ids);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                resolveNotificationObjects(ids);
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                updateEmptyState();
            }
        });
    }

    private void collectIds(DataSnapshot snapshot, Set<String> ids) {
        if (snapshot == null) {
            return;
        }
        for (DataSnapshot child : snapshot.getChildren()) {
            String id = child.getValue(String.class);
            if (Tool.boolOf(id)) {
                ids.add(id);
            }
        }
    }

    private void resolveNotificationObjects(Set<String> ids) {
        notifications.clear();
        notifAdapter.notifyDataSetChanged();
        if (ids == null || ids.isEmpty()) {
            updateEmptyState();
            return;
        }
        final int[] pending = {ids.size()};
        Notification<?> n = new Notification<>();
        for (String notifId : ids) {
            n.retrieveOnce(new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(NotificationFirebase object) {
                    if (object != null && !containsNotification(object.getID())) {
                        notifications.add(object);
                        notifAdapter.notifyItemInserted(notifications.size() - 1);
                    }
                    pending[0]--;
                    if (pending[0] <= 0) {
                        updateEmptyState();
                    }
                }

                @Override
                public void onError(DatabaseError error) {
                    pending[0]--;
                    if (pending[0] <= 0) {
                        updateEmptyState();
                    }
                }
            }, notifId);
        }
    }

    private boolean containsNotification(String notificationId) {
        if (!Tool.boolOf(notificationId)) {
            return false;
        }
        for (NotificationFirebase n : notifications) {
            if (n != null && notificationId.equals(n.getID())) {
                return true;
            }
        }
        return false;
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

    private void clearAllNotifications() {
        notifications.clear();
        notifAdapter.notifyDataSetChanged();
        updateEmptyState();
        if (currentUserRef == null) {
            return;
        }
        currentUserRef.child("notitficationIDs").setValue(new ArrayList<String>());
        currentUserRef.child("notificationIDs").setValue(new ArrayList<String>());
        currentUserRef.child("notifications").setValue(new ArrayList<String>());
    }
}

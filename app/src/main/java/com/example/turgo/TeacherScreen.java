package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityTeacherScreenBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class TeacherScreen extends AppCompatActivity {

    private Teacher teacher;
    private ActivityTeacherScreenBinding binding;

    private MailSmallAdapter mailSmallAdapter;
    private NotifAdapter notifAdapter;

    private ArrayList<NotificationFirebase> notifs = new ArrayList<>();
    private ArrayList<Mail> inbox = new ArrayList<>();

    private Toolbar topAppBar;
    private LinearLayout ll_mailEmpty, ll_notifEmpty;
    private PopupWindow mailPopupWindow, notifPopupWindow;
    private RecyclerView rv_MailDropDown, rv_NotifDropdown;
    private ImageButton notifFullPage;
    private BottomNavigationView bottomNav;
    private FirebaseUser fbUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTeacherScreenBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        NotificationPermissionHelper.requestIfNeeded(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topAppBar = findViewById(R.id.tb_ts_topAppBar);
        ll_mailEmpty = findViewById(R.id.ll_MDD_EmptyState);
        ll_notifEmpty = findViewById(R.id.ll_NDD_EmptyState);
        bottomNav = findViewById(R.id.nv_ts_BottomNavigation);
        setScreenInteractionEnabled(false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();

        if (fbUser == null) {
            Log.e("TeacherScreen", "Firebase user is null");
            finish();
            return;
        }

        TeacherRepository tr = new TeacherRepository(fbUser.getUid());
        Tool.prepareUserObjectForScreen(tr)
                .addOnSuccessListener(teacherObj -> {
                    teacher = teacherObj;
                    if (Tool.boolOf(teacher.getID())) {
                        PushTokenManager.syncKnownRoleWithLatestToken(teacher.getID(), "TEACHER");
                    }
                    continueAfterTeacherNotNull();
                })
                .addOnFailureListener(e -> {
                    Log.e("TeacherScreen", "Failed to prepare Teacher object", e);
                    finish();
                });
    }

    private void continueAfterTeacherNotNull() {
        setScreenInteractionEnabled(true);
        prepareActivityUI();
        loadDashboard();
    }

    private void setScreenInteractionEnabled(boolean enabled) {
        if (bottomNav != null) {
            bottomNav.setEnabled(enabled);
            bottomNav.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void prepareActivityUI() {
        initializeUI();
        prepareObjects();

        if (teacher != null && fbUser != null) {
            UserPresenceManager.startTracking(fbUser.getUid());
        }
    }

    private void loadDashboard() {
        Tool.loadFragment(this, getContainerId(), new TeacherDashboard());
    }

    private void initializeUI() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(teacher.getFullName());
        }

        setupBottomNavigation(bottomNav);
    }

    private void setupBottomNavigation(BottomNavigationView bnv) {
        bnv.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.dest_teacherDashboard) {
                loadDashboard();
                return true;
            }
            if (itemId == R.id.dest_teacherAllCourse) {
                Tool.loadFragment(this, getContainerId(), new TeacherAllCourse());
                return true;
            }
            if (itemId == R.id.dest_teacherCreateTask) {
                Tool.loadFragment(this, getContainerId(), new TeacherCreateTask());
                return true;
            }
            if (itemId == R.id.dest_teacherViewSchedules) {
                Tool.loadFragment(this, getContainerId(), new TeacherScheduleList());
                return true;
            }
            if (itemId == R.id.dest_teacherProfile) {
                Tool.loadFragment(this, getContainerId(), new Profile(teacher));
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topbar_user_menu, menu);
        return true;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("MissingInflatedId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Log.d("TeacherScreen", "Item Clicked: (" + itemId + ")");

        if (itemId == R.id.action_mail) {
            // Close notifications if open
            if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
                Log.d("TeacherScreen", "(MAIL) Closing notif popup");
                notifPopupWindow.dismiss();
            }

            // Toggle mail popup
            if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
                Log.d("TeacherScreen", "(MAIL) Closing mail popup");
                mailPopupWindow.dismiss();
                return true;
            }

            // Create mail popup ✅ FIXED SIZING
            View mailView = getLayoutInflater().inflate(R.layout.mail_drop_down, null);
            rv_MailDropDown = mailView.findViewById(R.id.rv_MDD_MailDropDown);
            mailSmallAdapter = new MailSmallAdapter(fbUser.getUid(), inbox, false);
            rv_MailDropDown.setLayoutManager(new LinearLayoutManager(this));
            rv_MailDropDown.setAdapter(mailSmallAdapter);



            LinearLayout ll_mailEmpty = mailView.findViewById(R.id.ll_MDD_EmptyState);
            Tool.handleEmpty(inbox.isEmpty(), rv_MailDropDown, ll_mailEmpty);

            mailPopupWindow = createPopupWindow(mailView, R.id.action_mail, "Mail");
            setupMailSeeAll(mailView);
            return true;

        } else if (itemId == R.id.action_notifications) {
            // Close mail if open
            if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
                Log.d("TeacherScreen", "(NOTIF) Closing mail popup");
                mailPopupWindow.dismiss();
            }

            // Toggle notif popup
            if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
                Log.d("TeacherScreen", "(NOTIF) Closing notif popup");
                notifPopupWindow.dismiss();
                return true;
            }

            // Create notif popup ✅ FIXED SIZING
            View notifView = getLayoutInflater().inflate(R.layout.notification_drop_down, null);
            rv_NotifDropdown = notifView.findViewById(R.id.rv_NotifDropDown);
            notifAdapter = new NotifAdapter(notifs);
            rv_NotifDropdown.setLayoutManager(new LinearLayoutManager(this));
            rv_NotifDropdown.setAdapter(notifAdapter);


            notifFullPage = notifView.findViewById(R.id.ib_NotifViewAll);
            notifFullPage.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherScreen.this, NotifPageFull.class);
                startActivity(intent);
                if (notifPopupWindow != null) {
                    notifPopupWindow.dismiss();
                }
            });
            ll_notifEmpty = notifView.findViewById(R.id.ll_NDD_EmptyState);
            Tool.handleEmpty(notifs.isEmpty(), rv_NotifDropdown, ll_notifEmpty);

            notifPopupWindow = createPopupWindow(notifView, R.id.action_notifications, "Notifications");
            return true;

        } else if (itemId == R.id.action_signOut) {
            Log.d("TeacherScreen", "(SIGNOUT) Sign out clicked");
            dismissPopups();
            return Tool.logOutUI(this, fbUser);
        }

        return super.onOptionsItemSelected(item);
    }

    private PopupWindow createPopupWindow(View contentView, int anchorId, String tag) {
        // ✅ FIXED DIMENSIONS - Works with RecyclerView
        PopupWindow popup = new PopupWindow(contentView,
                dpToPx(340), ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(12f);
        popup.setFocusable(true);
        // Prevent immediate "outside touch" dismissal caused by the same tap event on some devices/emulators.
        popup.setOutsideTouchable(false);
        popup.setOnDismissListener(() -> Log.d("TeacherScreen", tag + " popup dismissed"));

        // ✅ MEASURE content first (fixes WRAP_CONTENT bug)
        contentView.measure(
                View.MeasureSpec.makeMeasureSpec(dpToPx(340), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        popup.setHeight(contentView.getMeasuredHeight());

        // Try action item anchor first. If item is in overflow, anchor can be null.
        View anchor = topAppBar != null ? topAppBar.findViewById(anchorId) : null;
        if (anchor == null) {
            anchor = findViewById(anchorId);
        }

        if (anchor != null && anchor.isShown()) {
            View finalAnchor = anchor;
            anchor.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    popup.showAsDropDown(finalAnchor, -dpToPx(20), dpToPx(8));
                    Log.d("TeacherScreen", tag + " popup shown at action anchor");
                }
            });
        } else if (topAppBar != null) {
            topAppBar.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    popup.showAtLocation(topAppBar, Gravity.TOP | Gravity.END, dpToPx(8), dpToPx(56));
                    Log.w("TeacherScreen", tag + " action anchor missing; shown from toolbar fallback");
                }
            });
        } else {
            Log.e("PopupError", tag + " cannot be shown; toolbar anchor missing");
        }

        return popup;
    }

    private void setupMailSeeAll(View mailView) {
        Button seeAll = mailView.findViewById(R.id.btn_MDD_ViewAllMail);
        if (seeAll != null) {
            seeAll.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherScreen.this, MailPageFull.class);
                intent.putExtra(User.SERIALIZE_KEY_CODE, teacher);
                startActivity(intent);
                if (mailPopupWindow != null) mailPopupWindow.dismiss();
            });
        }
    }


    private void dismissPopups() {
        if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
            mailPopupWindow.dismiss();
        }
        if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
            notifPopupWindow.dismiss();
        }
    }


    public static int getContainerId() {
        return R.id.nhf_ts_FragmentContainer;
    }

    public void prepareObjects() {
        DatabaseReference teacherRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.TEACHER.getPath())
                .child(teacher.getID());

        teacherRef.child("inbox").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (rv_MailDropDown != null && ll_mailEmpty != null) {
                    Tool.handleEmpty(snapshot.exists(), rv_MailDropDown, ll_mailEmpty);
                }
                if (!snapshot.exists()) return;

                String mailID = snapshot.getValue(String.class);
                Mail m = new Mail();
                m.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(MailFirebase object) throws ParseException {
                        try {
                            object.convertToNormal(new ObjectCallBack<>() {
                                @Override
                                public void onObjectRetrieved(Mail mail) {
                                    addMailToInbox(mail);
                                }

                                @Override
                                public void onError(DatabaseError error) { }
                            });
                        } catch (InvocationTargetException | NoSuchMethodException |
                                 IllegalAccessException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(DatabaseError error) { }
                }, mailID);
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        attachNotificationListener(teacherRef, "notitficationIDs");
        attachNotificationListener(teacherRef, "notificationIDs");
        attachNotificationListener(teacherRef, "notifications");
    }

    private void attachNotificationListener(DatabaseReference teacherRef, String childPath) {
        teacherRef.child(childPath).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String notifID = snapshot.getValue(String.class);
                if (!Tool.boolOf(notifID)) return;
                Notification<?> n = new Notification<>();
                n.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(NotificationFirebase object) {
                        addNotification(object);
                        LocalNotificationBridge.notifyIfNew(TeacherScreen.this, object);
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        android.util.Log.e("TeacherScreen", "Failed loading notification id=" + notifID + ": " + error.getMessage());
                    }
                }, notifID);
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("TeacherScreen", "Notification listener cancelled for path=" + childPath + ": " + error.getMessage());
            }
        });
    }

    public Teacher getTeacher() {
        return teacher;
    }

    private void addMailToInbox(Mail mail) {
        if (mail == null) {
            return;
        }
        if (containsMail(mail.getMailID())) {
            return;
        }
        inbox.add(mail);
        if (mailSmallAdapter != null) {
            mailSmallAdapter.notifyItemInserted(inbox.size() - 1);
        }
    }

    private boolean containsMail(String mailId) {
        if (!Tool.boolOf(mailId)) {
            return false;
        }
        for (Mail existing : inbox) {
            if (existing != null && mailId.equals(existing.getMailID())) {
                return true;
            }
        }
        return false;
    }

    private void addNotification(NotificationFirebase notification) {
        if (notification == null) {
            return;
        }
        if (containsNotification(notification.getID())) {
            return;
        }
        notifs.add(notification);
        if (notifAdapter != null) {
            notifAdapter.notifyItemInserted(notifs.size() - 1);
        }
        if (rv_NotifDropdown != null && ll_notifEmpty != null) {
            Tool.handleEmpty(notifs.isEmpty(), rv_NotifDropdown, ll_notifEmpty);
        }
    }

    private boolean containsNotification(String notificationId) {
        if (!Tool.boolOf(notificationId)) {
            return false;
        }
        for (NotificationFirebase existing : notifs) {
            if (existing != null && notificationId.equals(existing.getID())) {
                return true;
            }
        }
        return false;
    }
}

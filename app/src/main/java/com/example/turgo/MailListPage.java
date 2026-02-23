package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MailListPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MailListPage extends Fragment {

    ArrayList<Mail> mails = new ArrayList<>();
    LinearLayout ll_emptyState;
    TextView tv_emptyText;
    RecyclerView rv_mailInbox;
    MailSmallAdapter mailSmallAdapter;

    DatabaseReference mailboxRef;
    ChildEventListener mailboxListener;
    final Map<String, ValueEventListener> mailDocListeners = new HashMap<>();
    User user;
    MailType mailType = MailType.INBOX;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MailListPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MailInbox.
     */
    // TODO: Rename and change types and number of parameters
    public static MailListPage newInstance(String param1, String param2) {
        MailListPage fragment = new MailListPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_list, container, false);
        MailPageFull mailPageFull = (MailPageFull) requireActivity();
        user = mailPageFull.getUser();
        if (user == null || !Tool.boolOf(user.getUid())) {
            return view;
        }

        Bundle bundle =  getArguments();
        if (bundle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mailType = bundle.getSerializable(MailType.MAIL_TYPE.getMailType(), MailType.class);
            } else {
                mailType = (MailType) bundle.getSerializable(MailType.MAIL_TYPE.getMailType());
            }
        }
        if (mailType == null) mailType = MailType.INBOX;

        tv_emptyText = view.findViewById(R.id.tv_ml_EmptyText);
        tv_emptyText.setText("Your " + mailType.getMailType().toLowerCase() + " is empty!");
        ll_emptyState = view.findViewById(R.id.ll_ml_emptyState);

        mailPageFull.tv_pageTitle.setText(mailType.getMailType().toUpperCase());

        boolean editable = mailType == MailType.DRAFT;
        mailSmallAdapter = new MailSmallAdapter(user.getUid(), mails, editable, mailPageFull, user, mailType);
        mailPageFull.setAdapter(mailSmallAdapter);
        rv_mailInbox = view.findViewById(R.id.rv_mailinbox_mails);

        rv_mailInbox.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv_mailInbox.setAdapter(mailSmallAdapter);

        updateEmptyState();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startRealtime(mailType, user);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRealtime();
    }

    private String mailboxFieldFor(MailType type) {
        switch (type) {
            case INBOX:
                return "inbox";
            case OUTBOX:
                return "outbox";
            case DRAFT:
                return "draftMails";
            default:
                return "inbox";
        }
    }

    private void startRealtime(MailType type, User user) {
        if (user == null || !Tool.boolOf(user.getUid())) return;
        stopRealtime();
        mails.clear();
        mailSmallAdapter.notifyDataSetChanged();
        updateEmptyState();

        String userPath = user.getFirebaseNode().getPath();
        String field = mailboxFieldFor(type);
        mailboxRef = FirebaseDatabase.getInstance()
                .getReference(userPath)
                .child(user.getUid())
                .child(field);

        mailboxListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String mailId = snapshot.getValue(String.class);
                if (Tool.boolOf(mailId)) {
                    attachMailDocListener(mailId);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                String mailId = snapshot.getValue(String.class);
                if (Tool.boolOf(mailId) && !mailDocListeners.containsKey(mailId)) {
                    attachMailDocListener(mailId);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String mailId = snapshot.getValue(String.class);
                if (Tool.boolOf(mailId)) {
                    detachAndRemoveMail(mailId);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MailListPage", "Mailbox listener cancelled: " + error.getMessage());
            }
        };
        mailboxRef.addChildEventListener(mailboxListener);
    }

    private void attachMailDocListener(String mailId) {
        if (mailDocListeners.containsKey(mailId)) return;

        DatabaseReference mailRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MAIL.getPath())
                .child(mailId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    detachAndRemoveMail(mailId);
                    return;
                }

                MailFirebase mf = snapshot.getValue(MailFirebase.class);
                if (mf == null) return;

                try {
                    mf.convertToNormal(new ObjectCallBack<>() {
                        @Override
                        public void onObjectRetrieved(Mail mail) {
                            if (!isAdded()) return;
                            upsertMail(mail);
                        }

                        @Override
                        public void onError(DatabaseError error) {
                            Log.e("MailListPage", "Mail convert error: " + error.getMessage());
                        }
                    });
                } catch (Exception e) {
                    Log.e("MailListPage", "Mail parse error", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MailListPage", "Mail doc listener cancelled: " + error.getMessage());
            }
        };

        mailRef.addValueEventListener(listener);
        mailDocListeners.put(mailId, listener);
    }

    private void upsertMail(Mail mail) {
        if (mail == null || !Tool.boolOf(mail.getMailID())) return;
        int index = findMailIndexById(mail.getMailID());
        if (index >= 0) {
            mails.set(index, mail);
            mailSmallAdapter.notifyItemChanged(index);
        } else {
            mails.add(0, mail);
            mailSmallAdapter.notifyItemInserted(0);
        }
        updateEmptyState();
    }

    private int findMailIndexById(String mailId) {
        for (int i = 0; i < mails.size(); i++) {
            Mail item = mails.get(i);
            if (item != null && mailId.equals(item.getMailID())) {
                return i;
            }
        }
        return -1;
    }

    private void detachAndRemoveMail(String mailId) {
        ValueEventListener listener = mailDocListeners.remove(mailId);
        if (listener != null) {
            FirebaseDatabase.getInstance()
                    .getReference(FirebaseNode.MAIL.getPath())
                    .child(mailId)
                    .removeEventListener(listener);
        }

        int index = findMailIndexById(mailId);
        if (index >= 0) {
            mails.remove(index);
            mailSmallAdapter.notifyItemRemoved(index);
        }
        updateEmptyState();
    }

    private void stopRealtime() {
        if (mailboxRef != null && mailboxListener != null) {
            mailboxRef.removeEventListener(mailboxListener);
        }
        mailboxListener = null;
        mailboxRef = null;

        for (Map.Entry<String, ValueEventListener> entry : mailDocListeners.entrySet()) {
            FirebaseDatabase.getInstance()
                    .getReference(FirebaseNode.MAIL.getPath())
                    .child(entry.getKey())
                    .removeEventListener(entry.getValue());
        }
        mailDocListeners.clear();
    }

    private void updateEmptyState() {
        if (rv_mailInbox == null || ll_emptyState == null) return;
        Tool.handleEmpty(mails.isEmpty(), rv_mailInbox, ll_emptyState);
    }
}

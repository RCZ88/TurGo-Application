package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MailListPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MailListPage extends Fragment {

    ArrayList<Mail> mails;
    LinearLayout ll_emptyState;
    TextView tv_emptyText;


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
        User user = mailPageFull.getUser();

        Bundle bundle =  getArguments();
        MailType mailType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mailType = bundle.getSerializable(MailType.MAIL_TYPE.getMailType(), MailType.class);
        }else{
            mailType = (MailType) bundle.getSerializable(MailType.MAIL_TYPE.getMailType());
        }
        tv_emptyText = view.findViewById(R.id.tv_ml_EmptyText);
        switch(mailType){
            case INBOX:
                this.mails = user.getInbox();
                break;
            case OUTBOX:
                this.mails = user.getOutbox();
                break;
            case DRAFT:
                this.mails = user.getDraftMails();
                break;
        }
        tv_emptyText.setText("Your " + mailType.getMailType().toLowerCase() + " is empty!");
        ll_emptyState = view.findViewById(R.id.ll_ml_emptyState);

        mailPageFull.tv_pageTitle.setText(mailType.getMailType().toUpperCase());

        MailSmallAdapter mailSmallAdapter = new MailSmallAdapter(user.getUid(), mails, false, mailPageFull);
        RecyclerView rv_mailInbox =  view.findViewById(R.id.rv_mailinbox_mails);

        rv_mailInbox.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv_mailInbox.setAdapter(mailSmallAdapter);

        Tool.handleEmpty(mailSmallAdapter.getMails().isEmpty(), rv_mailInbox, ll_emptyState);
        // Inflate the layout for this fragment
        return view;
    }
}
package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DraftMailViewAll#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DraftMailViewAll extends Fragment {
    RecyclerView rv_drafts;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DraftMailViewAll() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DraftMailViewAll.
     */
    // TODO: Rename and change types and number of parameters
    public static DraftMailViewAll newInstance(String param1, String param2) {
        DraftMailViewAll fragment = new DraftMailViewAll();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_draft_mail_view_all, container, false);
        MailPageFull mailPageFull = (MailPageFull) requireActivity();
        User user = mailPageFull.getUser();
        rv_drafts = view.findViewById(R.id.rv_DMVA_DraftMails);
        MailSmallAdapter adapter = new MailSmallAdapter(user.getUID(), user.getOutbox(), true, mailPageFull);
        rv_drafts.setAdapter(adapter);
        return view;
    }
}
package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {

    HashMap<String, ArrayList<String>> profileDetails;
    ArrayList<String>personalInfo;
    ArrayList<String>contact;
    ArrayList<String>preferences;
    ArrayList<String>languange;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
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

    private void initializeContent(){
        profileDetails = new HashMap<>();
        personalInfo = new ArrayList<>();
        contact = new ArrayList<>();
        languange = new ArrayList<>();

        personalInfo.add("Full Name");
        personalInfo.add("Nickname");
        personalInfo.add("Date of Birth");
        personalInfo.add("Gender");

        contact.add("Email");
        contact.add("Phone Number");

        preferences.add("Theme");
        preferences.add("Notifications");

        languange.add("Language");

        profileDetails.put("Personal Info", personalInfo);
        profileDetails.put("Contact", contact);
        profileDetails.put("Preferences", preferences);
        profileDetails.put("Language", languange);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initializeContent();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ExpandableListView expandableListView = view.findViewById(R.id.el_ProfileTraits);
        ArrayList<String> expandableListTitle = new ArrayList<>(profileDetails.keySet());
        CustomExpandableListAdapter adapter = new CustomExpandableListAdapter(getContext(), expandableListTitle, profileDetails);

// Set the adapter
        expandableListView.setAdapter(adapter);

// Optional: Handle child clicks
        /*expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedItem = profileDetails.get(expandableListTitle.get(groupPosition)).get(childPosition);
            Toast.makeText(getContext(), "Clicked: " + selectedItem, Toast.LENGTH_SHORT).show();
            return false;
        });*/
        // Inflate the layout for this fragment
        return view;
    }
}
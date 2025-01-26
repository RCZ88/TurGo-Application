package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup_UserDetails_Teacher#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup_UserDetails_Teacher extends Fragment implements checkFragmentCompletion{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Button btn_addSubject;
    Spinner sp_subjects;
    ListView lv_selectedSubjects;
    SignUpPage sup;
    ArrayList<String> selectedSubjects;
    ArrayAdapter<String>listAdapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public signup_UserDetails_Teacher() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signup_UserDetails_Teacher.
     */
    // TODO: Rename and change types and number of parameters
    public static signup_UserDetails_Teacher newInstance(String param1, String param2) {
        signup_UserDetails_Teacher fragment = new signup_UserDetails_Teacher();
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
        View view = inflater.inflate(R.layout.fragment_signup__user_details__teacher, container, false);
        btn_addSubject = view.findViewById(R.id.btn_add_subject);
        sp_subjects = view.findViewById(R.id.spinner_subjects);
        lv_selectedSubjects = view.findViewById(R.id.listview_selected_subjects);
        ArrayAdapter<CharSequence>adapter = ArrayAdapter.createFromResource(getContext(), R.array.course_options, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        sp_subjects.setAdapter(adapter);
        selectedSubjects = new ArrayList<>();
        btn_addSubject.setOnClickListener(view12 -> {
            String subject = sp_subjects.getSelectedItem().toString();
            if(!selectedSubjects.contains(subject)){
                selectedSubjects.add(subject);
                showOnAdapter();
            }
        });
        lv_selectedSubjects.setOnItemClickListener((adapterView, view1, i, l) -> {
            selectedSubjects.remove(adapterView.getItemAtPosition(i).toString());
            showOnAdapter();
        });
        showOnAdapter();
        return view;
    }
    private void showOnAdapter(){
        listAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, selectedSubjects);
        lv_selectedSubjects.setAdapter(listAdapter);
    }

    @Override
    public boolean checkIfCompleted() {
        return !selectedSubjects.isEmpty();
    }
}
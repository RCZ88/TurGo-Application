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
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup__student_selectcourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup__student_selectcourse extends Fragment implements checkFragmentCompletion{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    SignUpPage sup;
    Button btn_addCourse;
    ListView lv_selectedCourses;
    ArrayList<String>selectedCourses;
    ArrayAdapter<String>listAdapter;
    Spinner sp_chooseCourse;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public signup__student_selectcourse() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signup__student_selectcourse.
     */
    // TODO: Rename and change types and number of parameters
    public static signup__student_selectcourse newInstance(String param1, String param2) {
        signup__student_selectcourse fragment = new signup__student_selectcourse();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedCourses = new ArrayList<>();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup__student_selectcourse, container, false);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.course_options, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        sp_chooseCourse = view.findViewById(R.id.spinner_courses);
        //Assign string arrays to Spinner
        sp_chooseCourse.setAdapter(adapter);
        btn_addCourse = view.findViewById(R.id.btn_add_course);
        lv_selectedCourses = view.findViewById(R.id.listview_selected_courses);
        showOnAdapter();
        btn_addCourse.setOnClickListener(view1 -> {
            String subject = sp_chooseCourse.getSelectedItem().toString();
            if(!selectedCourses.contains(subject)){
                selectedCourses.add(subject);
                showOnAdapter();
            }
        });
        lv_selectedCourses.setOnItemClickListener((adapterView, view12, i, l) -> {
            selectedCourses.remove(adapterView.getItemAtPosition(i).toString());
            showOnAdapter();
        });
        // Inflate the layout for this fragment
        return view;
    }
    private void showOnAdapter(){
        listAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, selectedCourses);
        lv_selectedCourses.setAdapter(listAdapter);
    }

    @Override
    public boolean checkIfCompleted() {
        return !selectedCourses.isEmpty();
    }
}
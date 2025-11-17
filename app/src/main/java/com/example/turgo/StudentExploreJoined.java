package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentExploreJoined#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentExploreJoined extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FragmentContainerView fcv_ExploreJoined;
    Button btn_Explore, btn_MyCourse;

    public StudentExploreJoined() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudentExploreJoined.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentExploreJoined newInstance(String param1, String param2) {
        StudentExploreJoined fragment = new StudentExploreJoined();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_explore_joined, container, false);
        fcv_ExploreJoined = view.findViewById(R.id.fcv_SEJ_Container);
        getChildFragmentManager().beginTransaction().replace(R.id.fcv_SEJ_Container, new CourseJoinedFullPage()).commit();
        btn_Explore = view.findViewById(R.id.btn_SEJ_Explore);
        btn_MyCourse = view.findViewById(R.id.btn_SEJ_MyCourse);
        btn_Explore.setOnClickListener(view2 -> getChildFragmentManager().beginTransaction().replace(R.id.fcv_SEJ_Container, new CourseExploreFullPage()).commit());
        btn_MyCourse.setOnClickListener(view1 -> getChildFragmentManager().beginTransaction().replace(R.id.fcv_SEJ_Container, new CourseJoinedFullPage()).commit());
        return view;
    }
}
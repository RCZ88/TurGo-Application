package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

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
    MaterialButton btn_Explore, btn_MyCourse;
    MaterialButtonToggleGroup tg_tab;
    private static final String TAB_EXPLORE = "explore";
    private static final String TAB_MY_COURSES = "my_courses";
    private String currentTab = TAB_MY_COURSES;

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
        View view = inflater.inflate(R.layout.fragment_student_explore_joined, container, false);
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getString("currentTab", TAB_MY_COURSES);
        }

        fcv_ExploreJoined = view.findViewById(R.id.fcv_SEJ_Container);
        tg_tab = view.findViewById(R.id.tg_SEJ_Tab);
        btn_Explore = view.findViewById(R.id.btn_SEJ_Explore);
        btn_MyCourse = view.findViewById(R.id.btn_SEJ_MyCourse);

        tg_tab.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btn_SEJ_Explore) {
                showExploreTab();
            } else if (checkedId == R.id.btn_SEJ_MyCourse) {
                showMyCoursesTab();
            }
        });

        if (TAB_MY_COURSES.equals(currentTab)) {
            tg_tab.check(R.id.btn_SEJ_MyCourse);
        } else {
            tg_tab.check(R.id.btn_SEJ_Explore);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTab", currentTab);
    }

    public void showExploreTab() {
        currentTab = TAB_EXPLORE;
        updateTabUi();
        if (isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fcv_SEJ_Container, new Student_ExploreCourse())
                    .commit();
        }
    }

    public void showMyCoursesTab() {
        currentTab = TAB_MY_COURSES;
        updateTabUi();
        if (isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fcv_SEJ_Container, new Student_MyCourses())
                    .commit();
        }
    }

    private void updateTabUi() {
        if (!isAdded() || getContext() == null || btn_Explore == null || btn_MyCourse == null) {
            return;
        }
        int activeBg = ContextCompat.getColor(requireContext(), R.color.brand_emerald);
        int inactiveBg = ContextCompat.getColor(requireContext(), R.color.transparent);
        int activeText = ContextCompat.getColor(requireContext(), R.color.white_soft);
        int inactiveText = ContextCompat.getColor(requireContext(), R.color.brand_emerald_dark);

        boolean exploreActive = TAB_EXPLORE.equals(currentTab);
        btn_Explore.setBackgroundTintList(ColorStateList.valueOf(exploreActive ? activeBg : inactiveBg));
        btn_Explore.setTextColor(exploreActive ? activeText : inactiveText);

        boolean myCoursesActive = TAB_MY_COURSES.equals(currentTab);
        btn_MyCourse.setBackgroundTintList(ColorStateList.valueOf(myCoursesActive ? activeBg : inactiveBg));
        btn_MyCourse.setTextColor(myCoursesActive ? activeText : inactiveText);
    }
}

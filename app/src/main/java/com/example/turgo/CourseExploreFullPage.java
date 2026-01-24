package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseExploreFullPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseExploreFullPage extends Fragment implements RequiresDataLoading{

    Button btn_joinCourse;
    TextView tv_courseTitle, tv_courseDescription, tv_teacherName, tv_teacherDescription;
    ViewPager2 vp_courseImages;
    ImageView iv_courseWallpaper;
    LinearLayout ll_ImageIndicators;
    RecyclerView rv_courseDayTimes;
    Course course;
    Teacher teacher;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CourseExploreFullPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CourseExploreDetails.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseExploreFullPage newInstance(String param1, String param2) {
        CourseExploreFullPage fragment = new CourseExploreFullPage();
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
            onDataLoaded(getArguments());
        }

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_course_full_page, container, false);
        tv_courseTitle = view.findViewById(R.id.tv_ecfp_CourseTitle);
        btn_joinCourse = view.findViewById(R.id.btn_JoinCourse);
        tv_courseDescription = view.findViewById(R.id.tv_CourseDescription);
        tv_teacherDescription = view.findViewById(R.id.tv_TeacherDescription);
        tv_teacherName = view.findViewById(R.id.tv_TeacherName);
        vp_courseImages = view.findViewById(R.id.vp_ecfp_courseImages);
        ll_ImageIndicators = view.findViewById(R.id.ll_ecfp_imageIndicators);
        iv_courseWallpaper = view.findViewById(R.id.iv_ecfp_courseWallpaper);
        rv_courseDayTimes = view.findViewById(R.id.rv_ecfp_availTimes);
        rv_courseDayTimes.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));

        ArrayList<DayTimeArrangement>dtas = course.getDayTimeArrangement();

        ArrayList<String> daysOfDtas = DayTimeArrangement.getDaysOfDtas(dtas);
        ArrayList<String> timesOfDtas = DayTimeArrangement.getTimesOfDtas(dtas);
        Log.d("CEFP", daysOfDtas.toString());
        Log.d("CEFP", timesOfDtas.toString());

        DayTimeAdapter adapter = new DayTimeAdapter(daysOfDtas, timesOfDtas);
        rv_courseDayTimes.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_courseDayTimes.setAdapter(adapter);

        Tool.setImageCloudinary(getContext(), course.getBackgroundCloudinary(), iv_courseWallpaper);

        tv_courseTitle.setText(course.getCourseName());
        tv_courseDescription.setText(course.getCourseDescription());

        tv_teacherName.setText(teacher.getFullName());
        String teacherResume = "Teacher Description not Found";
        if(Tool.boolOf(teacher.getTeacherResume())){
            teacherResume = teacher.getTeacherResume();
        }
        tv_teacherDescription.setText(teacherResume);

        btn_joinCourse.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), RegisterCourse.class);
            intent.putExtra("Student", ((StudentScreen)getContext()).getStudent());
            intent.putExtra(Course.SERIALIZE_KEY_CODE, course);
            startActivity(intent);
        });
        CourseImageAdapter cia = new CourseImageAdapter(course.getImagesCloudinary());
        setupCarousel(cia);

        return view;
    }

    private void setupCarousel(CourseImageAdapter adapter){
        vp_courseImages.setAdapter(adapter);

        setupIndicators(adapter.getItemCount());
        vp_courseImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });

    }

    private void setupIndicators(int imageAmount) {
        ll_ImageIndicators.removeAllViews();
        for (int i = 0; i < imageAmount; i++) {
            ImageView dot = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.indicator_dot);
            ll_ImageIndicators.addView(dot);
        }
        updateIndicators(0);
    }

    private void updateIndicators(int currentPosition) {
        for (int i = 0; i < ll_ImageIndicators.getChildCount(); i++) {
            ImageView dot = (ImageView) ll_ImageIndicators.getChildAt(i);
            dot.setSelected(i == currentPosition);
        }
    }

    @Override
    public Bundle loadDataInBackground(Bundle input, DataLoading.ProgressCallback callback) {
        Bundle output = new Bundle();
        Course course =  (Course)input.getSerializable(Course.SERIALIZE_KEY_CODE);
        if(course != null){
            Teacher teacher = Await.get(course::getTeacher);
            output.putSerializable(Teacher.SERIALIZE_KEY_CODE, teacher);
            output.putSerializable(Course.SERIALIZE_KEY_CODE, course);
        }
        return output;
    }

    @Override
    public void onDataLoaded(Bundle preloadedData) {
        teacher = (Teacher)preloadedData.getSerializable(Teacher.SERIALIZE_KEY_CODE);
        course = (Course) preloadedData.getSerializable(Course.SERIALIZE_KEY_CODE);
    }

    @Override
    public void onLoadingError(Exception error) {

    }
}
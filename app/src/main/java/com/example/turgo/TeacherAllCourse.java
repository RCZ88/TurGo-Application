package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherAllCourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherAllCourse extends Fragment implements  RequiresDataLoading{
    RecyclerView rv_allCourse;
    ArrayList<Course> teacherCoursesTeach = new ArrayList<>();
    ArrayList<Integer> studentCountOfCourses = new ArrayList<>();
    ArrayList<Meeting>nextMeetingOfCourses = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeacherAllCourse() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherAllCourse.
     */
    // TODO: Rename and change types and number of parameters
    public static TeacherAllCourse newInstance(String param1, String param2) {
        TeacherAllCourse fragment = new TeacherAllCourse();
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
        View view = inflater.inflate(R.layout.fragment_teacher_all_course, container, false);
        rv_allCourse = view.findViewById(R.id.rv_TAC_AllCourse);
        TeacherScreen ts = (TeacherScreen) getActivity();
        assert ts != null;
        CourseTeachersAdapter adapter = new CourseTeachersAdapter(teacherCoursesTeach, nextMeetingOfCourses, studentCountOfCourses,  new OnItemClickListener<>() {
            @Override
            public void onItemClick(Course item) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Course.SERIALIZE_KEY_CODE, item);
                DataLoading.loadAndNavigate(requireContext(), TeacherCourseScreen.class, bundle, true, TeacherScreen.class, ts.getTeacher());
            }

            @Override
            public void onItemLongClick(Course item) {

            }
        });
        rv_allCourse.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv_allCourse.setAdapter(adapter);
        return view;
    }

    @Override
    public Bundle loadDataInBackground(Bundle input, DataLoading.ProgressCallback log) {
        Teacher teacher = (Teacher) input.getSerializable(Teacher.SERIALIZE_KEY_CODE);
        ArrayList<Course> teacherCoursesTeach = teacher.getCoursesTeach();
        ArrayList<Meeting>nextMeetingOfCourses = teacherCoursesTeach.stream().map(course-> course.getNextMeetingOfNextSchedule()).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> studentCountOfCourses = Tool.streamToArray(teacherCoursesTeach.stream().map(course -> Await.get(course::getStudents).size()));


        Bundle output = new Bundle();
        output.putSerializable("teacherCoursesTeach", teacherCoursesTeach);
        output.putSerializable("nextMeetingOfCourses", nextMeetingOfCourses);
        output.putSerializable("studentCountOfCourses", studentCountOfCourses);
        return output;
    }

    @Override
    public void onDataLoaded(Bundle preloadedData) {
        teacherCoursesTeach = (ArrayList<Course>) preloadedData.getSerializable("teacherCoursesTeacher");
        nextMeetingOfCourses = (ArrayList<Meeting>) preloadedData.getSerializable("nextMeetingOfCourses");
        studentCountOfCourses = (ArrayList<Integer>) preloadedData.getSerializable("studentCountOfCourses");
    }

    @Override
    public void onLoadingError(Exception error) {

    }
}
package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherAllCourse extends Fragment {

    RecyclerView rv_allCourse;
    ArrayList<Course> teacherCoursesTeach = new ArrayList<>();
    ArrayList<Integer> studentCountOfCourses = new ArrayList<>();
    ArrayList<Meeting> nextMeetingOfCourses = new ArrayList<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public TeacherAllCourse() {
    }

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

        View view = inflater.inflate(R.layout.fragment_teacher_all_course, container, false);
        rv_allCourse = view.findViewById(R.id.rv_TAC_AllCourse);

        TeacherScreen ts = (TeacherScreen) getActivity();
        assert ts != null;

        Teacher teacher = ts.getTeacher();

        // Load data synchronously using Await
        teacherCoursesTeach = teacher.getCoursesTeach();
//        nextMeetingOfCourses = teacherCoursesTeach.stream()
//                .map(course->{
//                    course.getNextMeetingOfNextSchedule().addOnSuccessListener()
//                })
//
        List<Task<Meeting>> tasks = new ArrayList<>();
        for(Course course : teacherCoursesTeach){
            tasks.add(course.getNextMeetingOfNextSchedule());
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            ArrayList<Meeting> meetings = new ArrayList<>();
            for (Object o : objects) {
                meetings.add((Meeting) o);
            }
            nextMeetingOfCourses = meetings;
            studentCountOfCourses = Tool.streamToArray(
                    teacherCoursesTeach.stream()
                            .map(course -> course.getStudentIds().size())
            );
            CourseTeachersAdapter adapter = new CourseTeachersAdapter(
                    teacherCoursesTeach,
                    nextMeetingOfCourses,
                    studentCountOfCourses,
                    new OnItemClickListener<>() {
                        @Override
                        public void onItemClick(Course item) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Course.SERIALIZE_KEY_CODE,item);
                            Tool.loadFragment(requireActivity(), TeacherScreen.getContainerId(), new TeacherCourseScreen(), bundle);
                        }

                        @Override
                        public void onItemLongClick(Course item) {
                        }
                    }
            );

            rv_allCourse.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv_allCourse.setAdapter(adapter);
        });





        return view;
    }
}

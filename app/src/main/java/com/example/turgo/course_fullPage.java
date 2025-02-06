package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link course_fullPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class course_fullPage extends Fragment {
    Course course;
    Student student;
    TextView tv_courseTeacher, tv_courseDays, tv_agendaDate, tv_courseAgenda, tv_noTaskMessage;
    ImageView iv_courseBackgroundImage;
    RecyclerView rv_listOfTasks, rv_latestAgenda;
    Button btn_viewAllAgenda;
    TaskAdapter taskAdapter;
    CollapsingToolbarLayout collapsingToolbar;
    ArrayList<Task> tasks;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public course_fullPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment course_fullPage.
     */
    // TODO: Rename and change types and number of parameters
    public static course_fullPage newInstance(String param1, String param2) {
        course_fullPage fragment = new course_fullPage();
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

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course_full_page, container, false);
        assert getActivity() != null;
        Intent intent = getActivity().getIntent();
        course = (Course) intent.getSerializableExtra("Selected Course");
        student = ((StudentScreen) getActivity()).getStudent();
        tasks = student.getAllTaskOfCourse(course);

        tv_courseTeacher = view.findViewById(R.id.courseTeacher);
        tv_courseDays = view.findViewById(R.id.courseDays);
        tv_noTaskMessage = view.findViewById(R.id.noTasksMessage);
        collapsingToolbar = view.findViewById(R.id.toolbar);
        iv_courseBackgroundImage = view.findViewById(R.id.courseImage);
        rv_listOfTasks = view.findViewById(R.id.taskRecyclerView);
        rv_latestAgenda = view.findViewById(R.id.rv_LatestTask);
        btn_viewAllAgenda = view.findViewById(R.id.btn_ViewAllAgendaOfCourse);
        collapsingToolbar.setTitle(course.getCourseName());
        tv_courseTeacher.setText(course.getTeacher().getFullName());
        tv_courseAgenda.setText(student.getLatestAgendaOfCourse(course).getContents());
        tv_courseDays.setText(course.getDaysOfSchedule());
        tv_agendaDate.setText(student.getLatestAgendaOfCourse(course).getDate().toString());
        tv_noTaskMessage.setText("Yay you're Free! No Task Found!");
        iv_courseBackgroundImage.setImageBitmap(course.getBackground());

        loadTasks();
        setLatestAgenda();

        btn_viewAllAgenda.setOnClickListener(view1 -> {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.nav_host_fragment, new AllAgendaPage(student.getAgendaOfCourse(course)));
            ft.addToBackStack(null);
            ft.commit();
        });
        return view;
    }
    private void setLatestAgenda(){
        ArrayList<Agenda>theAgenda = new ArrayList<>();
        theAgenda.add(student.getLatestAgendaOfCourse(course));
        AgendaAdapter agendaAdapter = new AgendaAdapter(theAgenda);
        rv_latestAgenda.setAdapter(agendaAdapter);
    }
    private void loadTasks() {
        tasks = student.getAllTaskOfCourse(course); // Assume this method fetches tasks
        if (tasks.isEmpty()) {
            tv_noTaskMessage.setVisibility(View.VISIBLE);
            rv_listOfTasks.setVisibility(View.GONE);
        } else {
            tv_noTaskMessage.setVisibility(View.GONE);
            rv_listOfTasks.setVisibility(View.VISIBLE);
            rv_listOfTasks.setLayoutManager(new LinearLayoutManager(getContext()));
            taskAdapter = new TaskAdapter(student.getAllTaskOfCourse(course), student);
            rv_listOfTasks.setAdapter(taskAdapter);
        }
    }
}
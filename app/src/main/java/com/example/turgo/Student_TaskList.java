package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_TaskList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_TaskList extends Fragment {
    RecyclerView rv_tasks;
    Button btn_viewPastTasks;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Student_TaskList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment student_TaskList.
     */
    // TODO: Rename and change types and number of parameters
    public static Student_TaskList newInstance(String param1, String param2) {
        Student_TaskList fragment = new Student_TaskList();
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
        View view = inflater.inflate(R.layout.fragment_student__task_list, container, false);
        Student student = ((StudentScreen)getActivity()).getStudent();
        TaskAdapter taskAdapter = new TaskAdapter(student.getAllTask(), student);
        rv_tasks = view.findViewById(R.id.rv_stl_AllTask);
        btn_viewPastTasks = view.findViewById(R.id.btn_ViewPastTask);
        rv_tasks.setAdapter(taskAdapter);
        btn_viewPastTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}
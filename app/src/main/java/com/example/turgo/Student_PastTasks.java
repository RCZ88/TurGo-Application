package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_PastTasks#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_PastTasks extends Fragment {
    RecyclerView rv_pastTasks;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Student_PastTasks() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Student_PastTasks.
     */
    // TODO: Rename and change types and number of parameters
    public static Student_PastTasks newInstance(String param1, String param2) {
        Student_PastTasks fragment = new Student_PastTasks();
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
        View view = inflater.inflate(R.layout.fragment_student__past_tasks, container, false);
        StudentScreen ss = (StudentScreen) requireActivity();
        Student student = ss.getStudent();
        rv_pastTasks = view.findViewById(R.id.rv_SPT_PastTasks);

        TaskAdapter taskAdapter = new TaskAdapter(student.getCompletedTask(), student, new OnItemClickListener<Task>() {
            @Override
            public void onItemClick(Task item) {
                TaskFullPage taskFullPage = new TaskFullPage();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Task.SERIALIZE_KEY_CODE, item);
                taskFullPage.setArguments(bundle);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nhf_ss_FragContainer, taskFullPage)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onItemLongClick(Task item) {

            }
        });
        rv_pastTasks.setAdapter(taskAdapter);

        return view;
    }
}
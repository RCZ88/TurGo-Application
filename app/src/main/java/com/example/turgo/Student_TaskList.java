package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_TaskList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_TaskList extends Fragment  {
    RecyclerView rv_tasks;
    Button btn_viewPastTasks;
    LinearLayout ll_emptyState;
    Spinner sp_filter;
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student__task_list, container, false);

        assert getActivity() != null;
        StudentScreen ss = (StudentScreen)requireActivity();
        rv_tasks = view.findViewById(R.id.rv_stl_AllTask);
        btn_viewPastTasks = view.findViewById(R.id.btn_ViewPastTask);
        ll_emptyState = view.findViewById(R.id.ll_stl_EmptyTask);
        sp_filter = view.findViewById(R.id.sp_stl_filter);
        loadLatestStudentTasks(ss);
        btn_viewPastTasks.setOnClickListener(view1 -> {
            Student_PastTasks spt = new Student_PastTasks();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nhf_ss_FragContainer, spt)
                    .addToBackStack(null)
                    .commit();
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void loadLatestStudentTasks(StudentScreen ss) {
        Student current = ss.getStudent();
        if (current == null || !Tool.boolOf(current.getID())) {
            Tool.handleEmpty(true, rv_tasks, ll_emptyState);
            return;
        }
        StudentRepository studentRepository = new StudentRepository(current.getID());
        TaskDeadlineService.processOverdueNonDropboxTasks().onSuccessTask(unused -> Tool.prepareUserObjectForScreen(studentRepository))
                .addOnSuccessListener(freshStudent -> {
                    ss.setStudent(freshStudent);
                    Log.d("StudentTaskList", "Current Task Pool: " + freshStudent.getCurrentTasks());
                    ArrayList<Task> currentTasks = freshStudent.getCurrentTasks();
                    TaskAdapter taskAdapter = new TaskAdapter(currentTasks, freshStudent, new OnItemClickListener<>() {
                        @Override
                        public void onItemClick(Task item) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Task.SERIALIZE_KEY_CODE, item);

                            TaskFullPage tfp = new TaskFullPage();
                            tfp.setArguments(bundle);
                            Tool.loadFragment(requireActivity(), R.id.nhf_ss_FragContainer, tfp);
                        }

                        @Override
                        public void onItemLongClick(Task item) {
                        }
                    }, TaskItemMode.RECYCLER);
                    rv_tasks.setLayoutManager(new LinearLayoutManager(requireContext()));
                    rv_tasks.setAdapter(taskAdapter);
                    setupFilterSpinner(freshStudent, currentTasks, taskAdapter);
                    Tool.handleEmpty(taskAdapter.getItemCount() == 0, rv_tasks, ll_emptyState);
                })
                .addOnFailureListener(e -> {
                    Tool.handleEmpty(true, rv_tasks, ll_emptyState);
                });
    }

    private void setupFilterSpinner(Student student, ArrayList<Task> source, TaskAdapter adapter) {
        ArrayList<String> filters = new ArrayList<>();
        filters.add("All");
        filters.add("Completed");
        filters.add("Uncompleted");
        filters.add("Unmarked");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filters
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_filter.setAdapter(spinnerAdapter);

        sp_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = filters.get(position);
                applyFilter(student, source, adapter, selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applyFilter(Student student, ArrayList<Task> source, TaskAdapter adapter, String selected) {
        ArrayList<Task> filtered = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Task task : source) {
            if (task == null) {
                continue;
            }
            TaskStatus status = student.resolveTaskStatus(task, now);
            if ("Completed".equals(selected) && status != TaskStatus.COMPLETED) {
                continue;
            }
            if ("Uncompleted".equals(selected) && status != TaskStatus.UNCOMPLETED) {
                continue;
            }
            if ("Unmarked".equals(selected) && status != TaskStatus.UNMARKED) {
                continue;
            }
            filtered.add(task);
        }
        adapter.replaceTasks(filtered);
        Tool.handleEmpty(adapter.getItemCount() == 0, rv_tasks, ll_emptyState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAdded() || getActivity() == null) {
            return;
        }
        StudentScreen ss = (StudentScreen) requireActivity();
        loadLatestStudentTasks(ss);
    }

}

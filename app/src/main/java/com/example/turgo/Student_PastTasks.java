package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_PastTasks#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_PastTasks extends Fragment {
    RecyclerView rv_pastTasks;
    ImageButton btn_back;
    LinearLayout ll_noTasks;
    Spinner sp_filter;

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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student__past_tasks, container, false);
        StudentScreen ss = (StudentScreen) requireActivity();
        rv_pastTasks = view.findViewById(R.id.rv_SPT_PastTasks);
        btn_back = view.findViewById(R.id.btn_spt_back);
        ll_noTasks = view.findViewById(R.id.ll_spt_emptyState);
        sp_filter = view.findViewById(R.id.sp_spt_filter);
        btn_back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        loadLatestCompletedTasks(ss);

        return view;
    }

    private void loadLatestCompletedTasks(StudentScreen ss) {
        Student current = ss.getStudent();
        if (current == null || !Tool.boolOf(current.getID())) {
            Tool.handleEmpty(true, rv_pastTasks, ll_noTasks);
            return;
        }

        StudentRepository studentRepository = new StudentRepository(current.getID());
        TaskDeadlineService.processOverdueNonDropboxTasks().onSuccessTask(unused -> Tool.prepareUserObjectForScreen(studentRepository))
                .addOnSuccessListener(freshStudent -> {
                    ss.setStudent(freshStudent);
                    ArrayList<Task> pastTasks = freshStudent.getPastTasks();
                    TaskAdapter taskAdapter = new TaskAdapter(pastTasks, freshStudent, new OnItemClickListener<Task>() {
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
                    }, TaskItemMode.RECYCLER);
                    rv_pastTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
                    rv_pastTasks.setAdapter(taskAdapter);
                    setupFilterSpinner(freshStudent, pastTasks, taskAdapter);
                    Tool.handleEmpty(taskAdapter.getItemCount() == 0, rv_pastTasks, ll_noTasks);
                })
                .addOnFailureListener(e -> Tool.handleEmpty(true, rv_pastTasks, ll_noTasks));
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
        Tool.handleEmpty(adapter.getItemCount() == 0, rv_pastTasks, ll_noTasks);
    }
}

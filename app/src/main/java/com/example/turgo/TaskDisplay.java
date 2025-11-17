package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskDisplay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskDisplay extends Fragment {
    Task task;
    TextView tv_taskTitle, tv_submissionTime, tv_date, tv_month;
    ImageView iv_submissionStatus;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TaskDisplay() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TaskDisplay.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskDisplay newInstance(String param1, String param2) {
        TaskDisplay fragment = new TaskDisplay();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static TaskDisplay newInstance(Task task){
        TaskDisplay fragment = new TaskDisplay();
        Bundle args = new Bundle();
        args.putSerializable(Task.SERIALIZE_KEY_CODE, task);
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
        View view = inflater.inflate(R.layout.fragment_task_display, container, false);
        StudentScreen activity = (StudentScreen) getActivity();

        tv_date = view.findViewById(R.id.tv_ftd_dateSubmission);
        tv_month = view.findViewById(R.id.tv_ftd_monthSubmission);
        tv_taskTitle = view.findViewById(R.id.tv_ftd_TaskTitle);
        tv_submissionTime = view.findViewById(R.id.tv_ftd_TimeSubmission);
        iv_submissionStatus =view.findViewById(R.id.iv_ftd_SubmissionStatus);

        if(getArguments()!= null) {
            task = (Task) getArguments().getSerializable(Task.SERIALIZE_KEY_CODE);
            LocalDateTime submissionDate = task.getDueDate();
            tv_date.setText(submissionDate.getDayOfMonth());
            tv_month.setText(submissionDate.getMonth().toString());
            tv_taskTitle.setText(task.getTitle());
            String submissionTime = submissionDate.getHour() + " : " + submissionDate.getMinute();
            tv_submissionTime.setText(submissionTime);
            StudentFirebase sf = activity.getStudent();
            Student s;
            try {
                s = (Student) sf.constructClass(Student.class, sf.getID());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
            iv_submissionStatus.setImageResource(task.isComplete(s) ? R.drawable.checkbox : R.drawable.pending);
        }
        return view;
    }
}
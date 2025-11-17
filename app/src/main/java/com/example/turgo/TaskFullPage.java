package com.example.turgo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFullPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFullPage extends Fragment {
    TextView tv_taskDate, tv_taskMonth, tv_taskTitle, tv_taskTime, tv_taskDescription, tv_noSubmission;
    RecyclerView rv_filesUploaded, rv_filesSubmitted;
    Button btn_uploadFile, btn_submitFile;
    Student student;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Task task;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TaskFullPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TaskFullPage.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskFullPage newInstance(String param1, String param2) {
        TaskFullPage fragment = new TaskFullPage();
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
            task = (Task)getArguments().getSerializable(Task.SERIALIZE_KEY_CODE);

        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_full_page, container, false);
        tv_taskDate = view.findViewById(R.id.tv_tfp_dateTask);
        tv_taskMonth = view.findViewById(R.id.tv_tfp_monthTask);
        tv_taskTime = view.findViewById(R.id.tv_tfp_timeTask);
        tv_taskDescription = view.findViewById(R.id.tv_tfp_taskDescription);
        tv_taskTitle = view.findViewById(R.id.tv_tfp_TaskTitle);
        tv_noSubmission = view.findViewById(R.id.tv_tfp_noSubmission);

        tv_taskTitle.setText(task.getTitle());
        tv_taskDate.setText(task.getDueDate().getDayOfMonth());
        tv_taskMonth.setText(task.getDueDate().getMonth().toString());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = task.getDueDate().format(dtf);
        tv_taskTime.setText(formattedTime);
        tv_taskDescription.setText(task.getDescription());

        rv_filesUploaded = view.findViewById(R.id.rv_FilesUploaded);
        rv_filesSubmitted = view.findViewById(R.id.rv_FilesSubmitted);
        StudentFirebase studentFirebase = ((StudentScreen)getActivity()).getStudent();
        try {
            student = studentFirebase.convertToNormal();
        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }
        ArrayList<file> studentFiles = task.getDropbox().getSubmissionSlot(student).getFilesOnly();
        if(!studentFiles.isEmpty()){
            FileAdapter fileAdapter = new FileAdapter(studentFiles);
            rv_filesUploaded.setAdapter(fileAdapter);
            tv_noSubmission.setVisibility(View.GONE);
            rv_filesUploaded.setVisibility(View.VISIBLE);
            rv_filesSubmitted.setVisibility(View.VISIBLE);
        }else{
            tv_noSubmission.setVisibility(View.VISIBLE);
            rv_filesUploaded.setVisibility(View.GONE);
            rv_filesSubmitted.setVisibility(View.GONE);
        }


        btn_uploadFile = view.findViewById(R.id.btn_tfp_SelectFile);
        ArrayList<file>filesUploaded = new ArrayList<>();
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result ->{
            if(result.getResultCode() == Activity.RESULT_OK & result.getData() != null){
                Intent data = result.getData();
                if(data.getClipData() != null){

                    int count = data.getClipData().getItemCount();
                    for(int i =0; i<count; i++){
                        Uri uriFile = data.getClipData().getItemAt(i).getUri();
                        String secure_url = "";
                        File tempFile;
                        try {
                            tempFile = Tool.uriToFile(uriFile, getContext());
                            secure_url = Tool.uploadToCloudinary(tempFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        filesUploaded.add(new file(tempFile.getName(), secure_url, student, LocalDateTime.now()));
                    }
                }else if(data.getData() != null){
                    Uri uriFile = data.getData();
                    File tempFile;
                    try {
                        tempFile = Tool.uriToFile(uriFile, getContext());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String secure_url = Tool.uploadToCloudinary(tempFile);
                    filesUploaded.add(new file(tempFile.getName(), secure_url, student, LocalDateTime.now()));
                }
                FileAdapter fileAdapter = new FileAdapter(filesUploaded);
                rv_filesUploaded.setAdapter(fileAdapter);
            }
        });
        btn_uploadFile.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select Files"));
        });

        ArrayList<file>filesSubmitted = new ArrayList<>();
        btn_submitFile = view.findViewById(R.id.btn_tfp_SubmitFile);
        btn_submitFile.setOnClickListener(view2 -> {
            try {
                student.submitTask(filesUploaded, task);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                     java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
            filesSubmitted.addAll(filesUploaded);
            FileAdapter fileAdapter = new FileAdapter(filesSubmitted);
            rv_filesSubmitted.setAdapter(fileAdapter);
        });




//        btn_uploadFile.setOnClickListener(view1 -> {
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("*/*");
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE);
//        });
        return view;
    }
}
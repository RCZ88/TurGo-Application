package com.example.turgo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DatabaseError;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.widget.SwitchCompat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFullPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFullPage extends Fragment {
    private static final String TAG = "TaskFullPageDebug";
    public static final String ARG_VIEWER_ROLE = "viewer_role";
    public static final String VIEWER_ROLE_TEACHER = "teacher";
    public static final String VIEWER_ROLE_STUDENT = "student";
    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_INPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    TextView tv_taskDate, tv_taskMonth, tv_taskTitle, tv_taskTime, tv_taskDescription, tv_noSubmission;
    RecyclerView rv_filesUploaded, rv_filesSubmitted;
    Button btn_uploadFile, btn_submitFile;
    Button btn_markDone, btn_markNotDone;
    MaterialCardView card_manualDecision, card_dropboxSection, card_dropboxNotOpen;
    TextView tv_manualStatus;

    MaterialCardView cardTeacherEdit, cardTeacherSubmissions;
    EditText etTeacherTitle, etTeacherDescription, etTeacherDueDate, etTeacherDueTime;
    SwitchCompat swTeacherDropbox;
    Button btnTeacherSave, btnTeacherCancel;
    RecyclerView rvTeacherSubmissions;
    TextView tvTeacherSubmissionEmpty;

    Student student;
    ArrayList<File>  fileDataList;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Task task;
    private final ArrayList<file> filesSelected = new ArrayList<>();
    private final ArrayList<SubmissionDisplay> selectedFilePreview = new ArrayList<>();
    private final ArrayList<SubmissionDisplay> submittedFilePreview = new ArrayList<>();
    private SubmissionAdapter filesSelectedAdapter;
    private SubmissionAdapter submittedFilesAdapter;
    private TeacherTaskSubmissionAdapter teacherTaskSubmissionAdapter;
    private final ArrayList<TeacherTaskSubmissionItem> teacherSubmissionItems = new ArrayList<>();
    private String submitButtonDefaultText;
    private String viewerRole = VIEWER_ROLE_STUDENT;
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
        card_manualDecision = view.findViewById(R.id.card_tfp_manualDecision);
        card_dropboxSection = view.findViewById(R.id.card_tfp_dropboxSection);
        card_dropboxNotOpen = view.findViewById(R.id.card_tfp_dropboxNotOpen);
        tv_manualStatus = view.findViewById(R.id.tv_tfp_manual_status);
        btn_markDone = view.findViewById(R.id.btn_tfp_markDone);
        btn_markNotDone = view.findViewById(R.id.btn_tfp_markNotDone);
        cardTeacherEdit = view.findViewById(R.id.card_tfp_teacher_edit);
        cardTeacherSubmissions = view.findViewById(R.id.card_tfp_teacher_submissions);
        etTeacherTitle = view.findViewById(R.id.et_tfp_teacher_title);
        etTeacherDescription = view.findViewById(R.id.et_tfp_teacher_description);
        etTeacherDueDate = view.findViewById(R.id.et_tfp_teacher_due_date);
        etTeacherDueTime = view.findViewById(R.id.et_tfp_teacher_due_time);
        swTeacherDropbox = view.findViewById(R.id.sw_tfp_teacher_dropbox);
        btnTeacherSave = view.findViewById(R.id.btn_tfp_teacher_save);
        btnTeacherCancel = view.findViewById(R.id.btn_tfp_teacher_cancel);
        rvTeacherSubmissions = view.findViewById(R.id.rv_tfp_teacher_submissions);
        tvTeacherSubmissionEmpty = view.findViewById(R.id.tv_tfp_teacher_submissions_empty);

        Bundle bundle = getArguments();
        if (bundle != null && Tool.boolOf(bundle.getString(ARG_VIEWER_ROLE))) {
            viewerRole = bundle.getString(ARG_VIEWER_ROLE, VIEWER_ROLE_STUDENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (bundle != null) {
                task = bundle.getSerializable(Task.SERIALIZE_KEY_CODE, Task.class);
            }
        } else {
            if (bundle != null) {
                task = (Task) bundle.getSerializable(Task.SERIALIZE_KEY_CODE);
            }
        }
        if (task == null) {
            Toast.makeText(requireContext(), "Task data unavailable.", Toast.LENGTH_SHORT).show();
            return view;
        }
        if (task.getDueDate() == null) {
            Toast.makeText(requireContext(), "Task due date unavailable.", Toast.LENGTH_SHORT).show();
            return view;
        }

        if (isTeacherViewer()) {
            configureTeacherUi();
        }

        final boolean hasDropbox = !task.isManualCompletionRequired();
        setDropboxCardState(hasDropbox);

        tv_taskTitle.setText(task.getTitle());
        tv_taskDate.setText(String.valueOf(task.getDueDate().getDayOfMonth()));
        tv_taskMonth.setText(task.getDueDate().getMonth().toString());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = task.getDueDate().format(dtf);
        tv_taskTime.setText(formattedTime);
        tv_taskDescription.setText(task.getDescription());

        rv_filesUploaded = view.findViewById(R.id.rv_FilesUploaded);
        rv_filesSubmitted = view.findViewById(R.id.rv_FilesSubmitted);
        btn_uploadFile = view.findViewById(R.id.btn_tfp_SelectFile);
        btn_submitFile = view.findViewById(R.id.btn_tfp_SubmitFile);
        rv_filesUploaded.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv_filesSubmitted.setLayoutManager(new LinearLayoutManager(requireContext()));
        fileDataList = new ArrayList<>();
        filesSelectedAdapter = new SubmissionAdapter(
                selectedFilePreview,
                SubmissionItemMode.FILE_PICKER,
                position -> {
                    if (position < 0 || position >= selectedFilePreview.size()) {
                        return;
                    }
                    selectedFilePreview.remove(position);
                    if (position < filesSelected.size()) {
                        filesSelected.remove(position);
                    }
                    filesSelectedAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Removed selected preview item at position=" + position
                            + ", remaining=" + selectedFilePreview.size());
                    if (selectedFilePreview.isEmpty()) {
                        tv_noSubmission.setVisibility(View.VISIBLE);
                    }
                }
        );
        rv_filesUploaded.setAdapter(filesSelectedAdapter);
        submittedFilesAdapter = new SubmissionAdapter(submittedFilePreview, SubmissionItemMode.FILE_PICKER);
        rv_filesSubmitted.setAdapter(submittedFilesAdapter);
        submitButtonDefaultText = btn_submitFile.getText().toString();
        rv_filesUploaded.setVisibility(View.VISIBLE);
        Log.d(TAG, "Recycler init complete. uploadedRV=" + (rv_filesUploaded != null)
                + ", submittedRV=" + (rv_filesSubmitted != null)
                + ", adapterCount=" + filesSelectedAdapter.getItemCount());

        if (isStudentViewer()) {
            StudentScreen ss = (StudentScreen)requireActivity();
            student = ss.getStudent();
            setupManualDecisionCard();
        } else {
            student = null;
            card_manualDecision.setVisibility(View.GONE);
            rv_filesUploaded.setVisibility(View.GONE);
            btn_uploadFile.setVisibility(View.GONE);
            btn_submitFile.setVisibility(View.GONE);
        }

        if (hasDropbox && isStudentViewer()) {
            task.getDropboxObject().addOnSuccessListener(dropbox -> {
                Log.d(TAG, "Dropbox loaded for preview? " + (dropbox != null));
                ArrayList<file> studentFiles = resolveStudentFilesFromDropbox(dropbox);
                Log.d(TAG, "Existing submitted file count=" + studentFiles.size());
                if(!studentFiles.isEmpty()){
                    submittedFilePreview.clear();
                    for (file f : studentFiles) {
                        ArrayList<String> files = new ArrayList<>();
                        files.add(f.getFileName());
                        String when = f.getFileCreateDate() != null ? f.getFileCreateDate().toString() : "-";
                        LocalDateTime submitTime = f.getSubmitTime() != null ? f.getSubmitTime() : f.getFileCreateDate();
                        boolean late = submitTime != null && submitTime.isAfter(task.getDueDate());
                        submittedFilePreview.add(new SubmissionDisplay(
                                student != null ? student.getFullName() : "",
                                task.getTitle(),
                                "",
                                when,
                                files,
                                late
                        ));
                    }
                    submittedFilesAdapter.notifyDataSetChanged();
                    tv_noSubmission.setVisibility(View.GONE);
                    rv_filesSubmitted.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Submitted preview adapter set. count=" + submittedFilePreview.size());
                }else{
                    tv_noSubmission.setVisibility(View.VISIBLE);
                    rv_filesSubmitted.setVisibility(View.GONE);
                    Log.d(TAG, "No submitted files; submitted preview hidden");
                }
            });
        } else {
            tv_noSubmission.setVisibility(View.VISIBLE);
            rv_filesSubmitted.setVisibility(View.GONE);
            Log.d(TAG, "Task has no dropbox; submitted preview hidden");
        }

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result ->{
            Log.d(TAG, "Picker resultCode=" + result.getResultCode() + ", hasData=" + (result.getData() != null));
            if(result.getResultCode() == Activity.RESULT_OK & result.getData() != null){
                filesSelected.clear();
                selectedFilePreview.clear();
                fileDataList.clear();
                Intent data = result.getData();
                if(data.getClipData() != null){

                    int count = data.getClipData().getItemCount();
                    Log.d(TAG, "Multi-select clipData count=" + count);
                    for(int i =0; i<count; i++){
                        Uri uriFile = data.getClipData().getItemAt(i).getUri();
                        Log.d(TAG, "Selected uri[" + i + "]=" + uriFile);
                        File tempFile;
                        try {
                            tempFile = Tool.uriToFile(uriFile, getContext());
                            fileDataList.add(tempFile);
                        } catch (IOException e) {
                            Log.e(TAG, "uriToFile failed for index " + i, e);
                            throw new RuntimeException(e);
                        }

                        LocalDateTime selectedAt = LocalDateTime.now();
                        String originalFileName = Tool.getFileName(requireContext(), uriFile);
                        if (!Tool.boolOf(originalFileName)) {
                            originalFileName = tempFile.getName();
                        }
                        filesSelected.add(new file(originalFileName, "", student, selectedAt, task));
                        ArrayList<String> files = new ArrayList<>();
                        files.add(originalFileName);
                        selectedFilePreview.add(new SubmissionDisplay(
                                student != null ? student.getFullName() : "Selected File",
                                task != null ? task.getTitle() : "",
                                "",
                                selectedAt.toString(),
                                files
                        ));
                        Log.d(TAG, "Added selected file: " + tempFile.getName());
                    }
                }else if(data.getData() != null){
                    Uri uriFile = data.getData();
                    Log.d(TAG, "Single-select uri=" + uriFile);
                    File tempFile;
                    try {
                        tempFile = Tool.uriToFile(uriFile, getContext());
                        fileDataList.add(tempFile);
                    } catch (IOException e) {
                        Log.e(TAG, "uriToFile failed for single select", e);
                        throw new RuntimeException(e);
                    }
                    LocalDateTime selectedAt = LocalDateTime.now();
                    String originalFileName = Tool.getFileName(requireContext(), uriFile);
                    if (!Tool.boolOf(originalFileName)) {
                        originalFileName = tempFile.getName();
                    }
                    filesSelected.add(new file(originalFileName, "", student, selectedAt, task));
                    ArrayList<String> files = new ArrayList<>();
                    files.add(originalFileName);
                    selectedFilePreview.add(new SubmissionDisplay(
                            student != null ? student.getFullName() : "Selected File",
                            task != null ? task.getTitle() : "",
                            "",
                            selectedAt.toString(),
                            files
                    ));
                    Log.d(TAG, "Added selected single file: " + tempFile.getName());
                }
                Log.d(TAG, "Selection lists ready. filesSelected=" + filesSelected.size()
                        + ", selectedPreview=" + selectedFilePreview.size());
                filesSelectedAdapter.notifyDataSetChanged();
                Log.d(TAG, "Adapter notified. adapterCountNow=" + filesSelectedAdapter.getItemCount());
                rv_filesUploaded.setVisibility(View.VISIBLE);
                tv_noSubmission.setVisibility(View.GONE);
                Log.d(TAG, "Uploaded preview visible=" + (rv_filesUploaded.getVisibility() == View.VISIBLE));
            }
        });
        btn_uploadFile.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select Files"));
        });

        Log.d(TAG, "Task  " + task.toString());
        Log.d(TAG, "Dropbox of Task: " + task.getDropbox());
        if (isStudentViewer()) {
            task.getDropboxObject().addOnSuccessListener(dropbox ->{
            Log.d(TAG, "Submissions: ");
            for (Submission submission : dropbox.getSubmissions()) {
                Log.d(TAG, submission.toString());
            }
            });
        }

        btn_submitFile.setOnClickListener(view2 -> {
            if (!isStudentViewer()) {
                return;
            }
            if (task.isManualCompletionRequired()) {
                Toast.makeText(requireContext(), "This task has no dropbox.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(filesSelected.isEmpty()){
                Toast.makeText(requireContext(), "You have yet to select a file..", Toast.LENGTH_SHORT).show();
                return;
            }
            if (fileDataList == null || fileDataList.isEmpty()) {
                Toast.makeText(requireContext(), "No file data to upload.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (fileDataList.size() != filesSelected.size()) {
                Log.e(TAG, "Submit aborted: selected files count mismatch. model="
                        + filesSelected.size() + ", fileData=" + fileDataList.size());
                Toast.makeText(requireContext(), "Selected file data is out of sync. Please reselect files.", Toast.LENGTH_SHORT).show();
                return;
            }

            task.getDropboxObject().addOnSuccessListener(dropbox ->{
                if (dropbox == null) {
                    Toast.makeText(requireContext(), "Failed to load dropbox for this task.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Submit aborted: dropbox is null for task=" + task.getID());
                    return;
                }

                task.setDropboxObject(dropbox);
                setUploadUiState(true, 0, fileDataList.size());
                AtomicInteger completedUploads = new AtomicInteger(0);
                AtomicInteger failedUploads = new AtomicInteger(0);
                ArrayList<file> uploadedModels = new ArrayList<>();
                AtomicBoolean finalized = new AtomicBoolean(false);
                AtomicBoolean connectivityFailure = new AtomicBoolean(false);

                for (int i = 0; i < fileDataList.size(); i++) {
                    file fileObject = filesSelected.get(i);
                    File fileData = fileDataList.get(i);
                    Tool.uploadToCloudinary(fileData, new ObjectCallBack<>() {
                        @Override
                        public void onObjectRetrieved(String path) {
                            fileObject.setSecureURL(path);
                            synchronized (uploadedModels) {
                                uploadedModels.add(fileObject);
                            }
                            Log.d(TAG, "URL Generated: " + path);
                            onSingleUploadFinished(completedUploads, failedUploads, uploadedModels, finalized, connectivityFailure);
                        }

                        @Override
                        public void onError(DatabaseError error) {
                            failedUploads.incrementAndGet();
                            if (Tool.isConnectivityIssue(error)) {
                                connectivityFailure.set(true);
                            }
                            Log.e(TAG, "Upload failed for " + fileObject.getFileName() + ": " + error.getMessage());
                            onSingleUploadFinished(completedUploads, failedUploads, uploadedModels, finalized, connectivityFailure);
                        }
                    });
                }

            }).addOnFailureListener(e -> {
                Log.e(TAG, "Submit failed: getDropboxObject failure", e);
                Toast.makeText(requireContext(), "Failed to submit task: unable to load dropbox.", Toast.LENGTH_SHORT).show();
            });
        });

        if (isTeacherViewer()) {
            bindTeacherEditInitialValues();
            setupTeacherEditActions();
            loadTeacherSubmissionItems();
        }

        return view;
    }

    private boolean isTeacherViewer() {
        return VIEWER_ROLE_TEACHER.equalsIgnoreCase(viewerRole);
    }

    private boolean isStudentViewer() {
        return !isTeacherViewer();
    }

    private void configureTeacherUi() {
        if (cardTeacherEdit != null) {
            cardTeacherEdit.setVisibility(View.VISIBLE);
        }
        if (cardTeacherSubmissions != null) {
            cardTeacherSubmissions.setVisibility(View.VISIBLE);
        }
        if (card_manualDecision != null) {
            card_manualDecision.setVisibility(View.GONE);
        }
        if (card_dropboxSection != null) {
            card_dropboxSection.setVisibility(View.GONE);
        }
        if (card_dropboxNotOpen != null) {
            card_dropboxNotOpen.setVisibility(View.GONE);
        }
        if (tv_noSubmission != null) {
            tv_noSubmission.setVisibility(View.GONE);
        }
        if (rvTeacherSubmissions != null) {
            rvTeacherSubmissions.setLayoutManager(new LinearLayoutManager(requireContext()));
            teacherTaskSubmissionAdapter = new TeacherTaskSubmissionAdapter();
            rvTeacherSubmissions.setAdapter(teacherTaskSubmissionAdapter);
        }
    }

    private void bindTeacherEditInitialValues() {
        if (task == null) {
            return;
        }
        etTeacherTitle.setText(task.getTitle());
        etTeacherDescription.setText(task.getDescription());
        if (task.getDueDate() != null) {
            etTeacherDueDate.setText(task.getDueDate().toLocalDate().format(DATE_INPUT_FORMATTER));
            etTeacherDueTime.setText(task.getDueDate().toLocalTime().format(TIME_INPUT_FORMATTER));
        }
        swTeacherDropbox.setChecked(!task.isManualCompletionRequired());
    }

    private void setupTeacherEditActions() {
        if (btnTeacherCancel != null) {
            btnTeacherCancel.setOnClickListener(v -> bindTeacherEditInitialValues());
        }
        if (btnTeacherSave != null) {
            btnTeacherSave.setOnClickListener(v -> saveTeacherEdits());
        }
    }

    private void saveTeacherEdits() {
        if (task == null) {
            return;
        }
        String newTitle = etTeacherTitle.getText().toString().trim();
        String newDescription = etTeacherDescription.getText().toString().trim();
        String dueDateInput = etTeacherDueDate.getText().toString().trim();
        String dueTimeInput = etTeacherDueTime.getText().toString().trim();
        boolean shouldEnableDropbox = swTeacherDropbox.isChecked();

        if (!Tool.boolOf(newTitle) || !Tool.boolOf(newDescription) || !Tool.boolOf(dueDateInput) || !Tool.boolOf(dueTimeInput)) {
            Toast.makeText(requireContext(), "Fill title, description, date, and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDateTime newDueDate;
        try {
            LocalDate date = LocalDate.parse(dueDateInput, DATE_INPUT_FORMATTER);
            LocalTime time = LocalTime.parse(dueTimeInput, TIME_INPUT_FORMATTER);
            newDueDate = LocalDateTime.of(date, time);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Use due date yyyy-MM-dd and time HH:mm.", Toast.LENGTH_SHORT).show();
            return;
        }

        task.setTitle(newTitle);
        task.setDescription(newDescription);
        task.setDueDate(newDueDate);

        TaskRepository taskRepository = new TaskRepository(task.getID());
        taskRepository.updateTitle(newTitle);
        taskRepository.updateDescription(newDescription);
        taskRepository.updateDueDate(newDueDate.toString());

        if (shouldEnableDropbox) {
            if (task.isManualCompletionRequired()) {
                task.enableDropbox()
                        .addOnSuccessListener(unused -> {
                            Dropbox dropbox = task.getDropboxCached();
                            if (dropbox != null) {
                                new DropboxRepository(dropbox.getID()).save(dropbox);
                                taskRepository.updateDropbox(dropbox);
                            }
                            task.setManualCompletionRequired(false);
                            setDropboxCardState(true);
                            loadTeacherSubmissionItems();
                            Toast.makeText(requireContext(), "Task updated.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(requireContext(), "Failed to enable dropbox.", Toast.LENGTH_SHORT).show());
            } else {
                taskRepository.updateManualCompletionRequired(false);
                setDropboxCardState(true);
                loadTeacherSubmissionItems();
                Toast.makeText(requireContext(), "Task updated.", Toast.LENGTH_SHORT).show();
            }
        } else {
            task.setManualCompletionRequired(true);
            task.setDropbox(null);
            taskRepository.updateManualCompletionRequired(true);
            taskRepository.getDbReference().child("dropbox").removeValue();
            setDropboxCardState(false);
            teacherSubmissionItems.clear();
            if (teacherTaskSubmissionAdapter != null) {
                teacherTaskSubmissionAdapter.submit(teacherSubmissionItems);
            }
            if (tvTeacherSubmissionEmpty != null) {
                tvTeacherSubmissionEmpty.setVisibility(View.VISIBLE);
                tvTeacherSubmissionEmpty.setText("Dropbox is disabled for this task.");
            }
            Toast.makeText(requireContext(), "Task updated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTeacherSubmissionItems() {
        if (!isTeacherViewer()) {
            return;
        }
        teacherSubmissionItems.clear();

        if (task == null || task.isManualCompletionRequired()) {
            if (teacherTaskSubmissionAdapter != null) {
                teacherTaskSubmissionAdapter.submit(teacherSubmissionItems);
            }
            if (tvTeacherSubmissionEmpty != null) {
                tvTeacherSubmissionEmpty.setVisibility(View.VISIBLE);
                tvTeacherSubmissionEmpty.setText("Dropbox is disabled for this task.");
            }
            return;
        }

        task.getDropboxObject().addOnSuccessListener(dropbox -> {
            HashMap<String, Submission> submissionMap = new HashMap<>();
            if (dropbox != null && dropbox.getSubmissions() != null) {
                for (Submission submission : dropbox.getSubmissions()) {
                    if (submission == null || submission.getOf() == null || !Tool.boolOf(submission.getOf().getID())) {
                        continue;
                    }
                    submissionMap.put(submission.getOf().getID(), submission);
                }
            }

            task.getStudentAssigned().addOnSuccessListener(studentsAssigned -> {
                buildTeacherSubmissionItems(studentsAssigned, submissionMap);
            }).addOnFailureListener(e -> {
                ArrayList<Student> fallback = new ArrayList<>();
                for (Map.Entry<String, Submission> entry : submissionMap.entrySet()) {
                    Submission submission = entry.getValue();
                    if (submission != null && submission.getOf() != null) {
                        fallback.add(submission.getOf());
                    }
                }
                buildTeacherSubmissionItems(fallback, submissionMap);
            });
        }).addOnFailureListener(e -> {
            if (tvTeacherSubmissionEmpty != null) {
                tvTeacherSubmissionEmpty.setVisibility(View.VISIBLE);
                tvTeacherSubmissionEmpty.setText("Failed to load submissions.");
            }
        });
    }

    private void buildTeacherSubmissionItems(List<Student> studentsAssigned, HashMap<String, Submission> submissionMap) {
        teacherSubmissionItems.clear();
        if (studentsAssigned != null) {
            for (Student assigned : studentsAssigned) {
                if (assigned == null || !Tool.boolOf(assigned.getID())) {
                    continue;
                }
                Submission submission = submissionMap.get(assigned.getID());
                teacherSubmissionItems.add(mapToTeacherSubmissionItem(assigned, submission));
            }
        }
        if (teacherTaskSubmissionAdapter != null) {
            teacherTaskSubmissionAdapter.submit(teacherSubmissionItems);
        }
        if (tvTeacherSubmissionEmpty != null) {
            tvTeacherSubmissionEmpty.setVisibility(teacherSubmissionItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private TeacherTaskSubmissionItem mapToTeacherSubmissionItem(Student studentObj, Submission submission) {
        String studentName = Tool.boolOf(studentObj.getFullName()) ? studentObj.getFullName() : studentObj.getID();
        String status = "Pending";
        String submittedAt = "Not submitted";
        ArrayList<String> fileNames = new ArrayList<>();
        String primaryUrl = null;

        if (submission != null && submission.getFiles() != null && !submission.getFiles().isEmpty()) {
            status = "Submitted";
            LocalDateTime latestTime = null;
            boolean hasLate = false;
            for (Map.Entry<file, Boolean> entry : submission.getFiles().entrySet()) {
                file submittedFile = entry.getKey();
                if (submittedFile == null) {
                    continue;
                }
                if (Tool.boolOf(submittedFile.getFileName())) {
                    fileNames.add(submittedFile.getFileName());
                }
                if (!Tool.boolOf(primaryUrl) && Tool.boolOf(submittedFile.getSecureURL())) {
                    primaryUrl = submittedFile.getSecureURL();
                }
                LocalDateTime time = submittedFile.getSubmitTime() != null ? submittedFile.getSubmitTime() : submittedFile.getFileCreateDate();
                if (time != null && (latestTime == null || time.isAfter(latestTime))) {
                    latestTime = time;
                }
                Boolean lateFlag = entry.getValue();
                if (Boolean.TRUE.equals(lateFlag)) {
                    hasLate = true;
                }
            }
            if (hasLate) {
                status = "Late";
            }
            if (latestTime != null) {
                submittedAt = latestTime.toString();
            }
        }

        return new TeacherTaskSubmissionItem(studentName, status, submittedAt, fileNames, primaryUrl);
    }

    @SuppressLint("SetTextI18n")
    private void setupManualDecisionCard() {
        if (student == null || task == null || task.getDueDate() == null || !task.isManualCompletionRequired()) {
            card_manualDecision.setVisibility(View.GONE);
            return;
        }

        card_manualDecision.setVisibility(View.VISIBLE);
        boolean isOverdue = task.getDueDate().isBefore(LocalDateTime.now());
        String taskId = task.getID();
        boolean manuallyCompleted = isTaskInList(student.getManualCompletedTask(), taskId);
        boolean manuallyMissed = isTaskInList(student.getManualMissedTask(), taskId);

        if (manuallyCompleted) {
            tv_manualStatus.setText("You marked this task as done.");
            btn_markDone.setEnabled(false);
            btn_markNotDone.setEnabled(true);
        } else if (manuallyMissed) {
            tv_manualStatus.setText("You marked this task as not done.");
            btn_markDone.setEnabled(true);
            btn_markNotDone.setEnabled(false);
        } else if (isOverdue) {
            tv_manualStatus.setText("This overdue task needs your status.");
            btn_markDone.setEnabled(true);
            btn_markNotDone.setEnabled(true);
        } else {
            tv_manualStatus.setText("No dropbox for this task. Mark it done when you finish.");
            btn_markDone.setEnabled(true);
            btn_markNotDone.setEnabled(false);
        }

        btn_markDone.setOnClickListener(v -> updateManualStatus(true));
        btn_markNotDone.setOnClickListener(v -> updateManualStatus(false));
    }

    private boolean isTaskInList(ArrayList<Task> taskList, String taskId) {
        if (!Tool.boolOf(taskId) || taskList == null || taskList.isEmpty()) {
            return false;
        }
        for (Task listTask : taskList) {
            if (listTask != null && Tool.boolOf(listTask.getID()) && taskId.equals(listTask.getID())) {
                return true;
            }
        }
        return false;
    }

    private void updateManualStatus(boolean completed) {
        if (student == null || task == null || !Tool.boolOf(student.getID()) || !Tool.boolOf(task.getID())) {
            return;
        }
        StudentRepository sr = new StudentRepository(student.getID());
        String taskId = task.getID();

        com.google.android.gms.tasks.Task<Void> writeTask;
        if (completed) {
            writeTask = Tasks.whenAll(
                    sr.removeUncompletedTaskAsync(taskId),
                    sr.removeManualMissedTaskAsync(taskId),
                    sr.addManualCompletedTaskAsync(taskId)
            );
        } else {
            writeTask = Tasks.whenAll(
                    sr.removeUncompletedTaskAsync(taskId),
                    sr.removeManualCompletedTaskAsync(taskId),
                    sr.addManualMissedTaskAsync(taskId)
            );
        }

        writeTask.addOnSuccessListener(unused -> {
            student.markManualTaskStatus(task, completed);
            if (student.getUncompletedTask() != null) {
                student.getUncompletedTask().removeIf(t -> t != null && Tool.boolOf(t.getID()) && taskId.equals(t.getID()));
            }
            setupManualDecisionCard();
            Toast.makeText(requireContext(), completed ? "Marked as done." : "Marked as not done.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update task status.", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onSingleUploadFinished(AtomicInteger completedUploads,
                                        AtomicInteger failedUploads,
                                        ArrayList<file> uploadedModels,
                                        AtomicBoolean finalized,
                                        AtomicBoolean connectivityFailure) {
        int done = completedUploads.incrementAndGet();
        int total = fileDataList != null ? fileDataList.size() : 0;
        if (!isAdded()) {
            return;
        }
        requireActivity().runOnUiThread(() -> {
            setUploadUiState(true, done, total);

            if (done != total || !finalized.compareAndSet(false, true)) {
                return;
            }

            if (uploadedModels.isEmpty()) {
                setUploadUiState(false, 0, 0);
                if (connectivityFailure != null && connectivityFailure.get()) {
                    Toast.makeText(requireContext(), "No internet connection. Upload failed.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Upload failed. No files submitted.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            student.submitTask(uploadedModels, task);
            for (file uploaded : uploadedModels) {
                ArrayList<String> files = new ArrayList<>();
                files.add(uploaded.getFileName());
                String when = uploaded.getSubmitTime() != null
                        ? uploaded.getSubmitTime().toString()
                        : (uploaded.getFileCreateDate() != null ? uploaded.getFileCreateDate().toString() : "-");
                LocalDateTime submitTime = uploaded.getSubmitTime() != null ? uploaded.getSubmitTime() : uploaded.getFileCreateDate();
                boolean late = submitTime != null && submitTime.isAfter(task.getDueDate());
                submittedFilePreview.add(new SubmissionDisplay(
                        student != null ? student.getFullName() : "",
                        task.getTitle(),
                        "",
                        when,
                        files,
                        late
                ));
            }
            submittedFilesAdapter.notifyDataSetChanged();
            rv_filesSubmitted.setVisibility(View.VISIBLE);
            tv_noSubmission.setVisibility(View.GONE);
            filesSelected.clear();
            selectedFilePreview.clear();
            fileDataList.clear();
            filesSelectedAdapter.notifyDataSetChanged();
            setUploadUiState(false, 0, 0);

            int failures = failedUploads.get();
            if (failures == 0) {
                Toast.makeText(requireContext(), "Task submitted successfully.", Toast.LENGTH_SHORT).show();
            } else {
                if (connectivityFailure != null && connectivityFailure.get()) {
                    Toast.makeText(requireContext(), "Submitted with " + failures + " failed upload(s) due to no internet connection.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), "Submitted with " + failures + " failed upload(s).", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setUploadUiState(boolean uploading, int done, int total) {
        if (btn_submitFile == null || btn_uploadFile == null) {
            return;
        }
        if (uploading) {
            btn_submitFile.setEnabled(false);
            btn_uploadFile.setEnabled(false);
            btn_submitFile.setText("Uploading " + done + "/" + total + "...");
        } else {
            btn_submitFile.setEnabled(true);
            btn_uploadFile.setEnabled(true);
            btn_submitFile.setText(submitButtonDefaultText != null ? submitButtonDefaultText : "Submit");
        }
    }

    private void setDropboxCardState(boolean hasDropbox) {
        if (isTeacherViewer()) {
            return;
        }
        if (card_dropboxSection == null || card_dropboxNotOpen == null) {
            return;
        }
        card_dropboxSection.setVisibility(hasDropbox ? View.VISIBLE : View.GONE);
        card_dropboxNotOpen.setVisibility(hasDropbox ? View.GONE : View.VISIBLE);
    }

    private ArrayList<file> resolveStudentFilesFromDropbox(Dropbox dropbox) {
        ArrayList<file> result = new ArrayList<>();
        if (dropbox == null) {
            return result;
        }

        Submission slot = null;
        if (student != null) {
            slot = dropbox.getSubmissionSlot(student);
        }

        if (slot == null) {
            String currentUid = FirebaseAuth.getInstance().getUid();
            if (Tool.boolOf(currentUid) && dropbox.getSubmissions() != null) {
                for (Submission submission : dropbox.getSubmissions()) {
                    if (submission == null || submission.getOf() == null || !Tool.boolOf(submission.getOf().getID())) {
                        continue;
                    }
                    if (currentUid.equals(submission.getOf().getID())) {
                        slot = submission;
                        break;
                    }
                }
            }
        }

        if (slot != null && slot.getFilesOnly() != null) {
            result = slot.getFilesOnly();
        }
        return result;
    }
}

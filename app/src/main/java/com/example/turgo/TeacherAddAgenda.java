package com.example.turgo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CheckBox;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DatabaseError;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TeacherAddAgenda extends Fragment {

    Spinner sp_AgendaOfCourse, sp_SelectMeeting;
    ChipGroup cg_SelectStudent;
    EditText etml_AgendaContents;
    Button btn_UploadImageAgenda, btn_SendAgenda;
    ToggleButton tb_AgendaFormat;
    CheckBox cb_notifyStudents;
    LinearLayout ll_noImageSelected;
    RecyclerView rv_agendaImagePreviews;
    ImageAdapter imagePreviewAdapter;
    TextView tv_presetStudents, tv_presetCourses, tv_noStudents;
    Teacher teacher;

    Course courseFromArgs;
    Course selectedCourse;
    Meeting selectedMeeting;
    private final Set<String> presetStudentIds = new HashSet<>();
    private boolean shouldApplyPresetStudentSelection = false;
    private final ArrayList<Uri> selectedImageUris = new ArrayList<>();

    private ActivityResultLauncher<Intent> filePickerLauncher;

    public TeacherAddAgenda() {}

    public static TeacherAddAgenda newInstance(String param1, String param2) {
        TeacherAddAgenda fragment = new TeacherAddAgenda();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_add_agenda, container, false);

        sp_AgendaOfCourse = view.findViewById(R.id.sp_TAA_AgendaOfCourse);
        cg_SelectStudent = view.findViewById(R.id.cg_TAA_SelectStudent);
        sp_SelectMeeting = view.findViewById(R.id.sp_TAA_SelectMeeting);
        etml_AgendaContents = view.findViewById(R.id.etml_TAA_AgendaContent);
        btn_UploadImageAgenda = view.findViewById(R.id.btn_TAA_UploadAgendaImage);
        btn_SendAgenda = view.findViewById(R.id.btn_TAA_SendAgenda);
        tb_AgendaFormat = view.findViewById(R.id.tb_TAA_AgendaFormat);
        cb_notifyStudents = view.findViewById(R.id.cb_TAA_NotifyStudents);
        ll_noImageSelected = view.findViewById(R.id.ll_TAA_NoImageSelected);
        rv_agendaImagePreviews = view.findViewById(R.id.rv_TAA_AgendaImagePreviews);
        tv_presetStudents = view.findViewById(R.id.tv_TAA_PresetStudents);
        tv_presetCourses = view.findViewById(R.id.tv_TAA_PresetCourse);
        tv_noStudents = view.findViewById(R.id.tv_TAA_StudentsEmpty);

        teacher = ((TeacherScreen) requireActivity()).getTeacher();
        imagePreviewAdapter = new ImageAdapter(selectedImageUris);
        rv_agendaImagePreviews.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_agendaImagePreviews.setAdapter(imagePreviewAdapter);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("presetCourse")) {
            courseFromArgs = (Course) bundle.getSerializable("presetCourse");
        }
        if (bundle != null) {
            ArrayList<Student> presetStudents = null;
            if (bundle.getSerializable("studentSelectedMenu") instanceof ArrayList) {
                presetStudents = (ArrayList<Student>) bundle.getSerializable("studentSelectedMenu");
            } else if (bundle.getSerializable("presetStudent") instanceof ArrayList) {
                presetStudents = (ArrayList<Student>) bundle.getSerializable("presetStudent");
            } else if (bundle.getSerializable("presetStudent") instanceof Student) {
                Student s = (Student) bundle.getSerializable("presetStudent");
                presetStudents = new ArrayList<>();
                if (s != null) {
                    presetStudents.add(s);
                }
            }
            if (presetStudents != null) {
                for (Student student : presetStudents) {
                    String studentId = resolveStudentId(student);
                    if (Tool.boolOf(studentId)) {
                        presetStudentIds.add(studentId);
                    }
                }
                shouldApplyPresetStudentSelection = !presetStudentIds.isEmpty();
            }
        }
        tv_presetStudents.setVisibility(View.GONE);
        tv_presetCourses.setVisibility(View.GONE);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                Uri uri = data.getClipData().getItemAt(i).getUri();
                                addSelectedImageUri(uri);
                            }
                        } else {
                            addSelectedImageUri(data.getData());
                        }
                        imagePreviewAdapter.notifyDataSetChanged();
                        updateImagePreviewState();
                    }
                });

        tb_AgendaFormat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyAgendaFormatState(isChecked);
        });
        applyAgendaFormatState(tb_AgendaFormat.isChecked());

        ArrayList<Course> teacherCourses = loadCourseSpinner();
        sp_AgendaOfCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = (Course) parent.getSelectedItem();
                selectedMeeting = null;
                loadStudentsForCourse(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (courseFromArgs != null && Tool.boolOf(courseFromArgs.getID())) {
            for (int i = 0; i < teacherCourses.size(); i++) {
                Course c = teacherCourses.get(i);
                if (c != null && Tool.boolOf(c.getID()) && c.getID().equals(courseFromArgs.getID())) {
                    sp_AgendaOfCourse.setSelection(i);
                    selectedCourse = c;
                    break;
                }
            }
        } else if (!teacherCourses.isEmpty()) {
            selectedCourse = teacherCourses.get(0);
            loadStudentsForCourse(selectedCourse);
        } else {
            selectedCourse = null;
        }

        sp_SelectMeeting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMeeting = (Meeting) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMeeting = null;
            }
        });

        btn_UploadImageAgenda.setOnClickListener(v -> openFilePicker());

        btn_SendAgenda.setOnClickListener(v -> {
            ArrayList<Student> selectedStudents = getSelectedStudentsFromChips();
            if (selectedStudents.isEmpty()) {
                Toast.makeText(requireContext(), "Select at least one student.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCourse == null) {
                Toast.makeText(requireContext(), "Select a course first.", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean shouldNotifyStudents = cb_notifyStudents != null && cb_notifyStudents.isChecked();

            if (tb_AgendaFormat.isChecked()) {
                handleTextAgenda(selectedMeeting, selectedStudents, selectedCourse, shouldNotifyStudents);
            } else {
                handleFileAgenda(selectedImageUris, selectedMeeting, selectedStudents, selectedCourse, shouldNotifyStudents);
            }
        });

        return view;
    }

    private ArrayList<Course> loadCourseSpinner(){
        ArrayList<Course> teacherCourses = teacher != null && teacher.getCoursesTeach() != null
                ? teacher.getCoursesTeach()
                : new ArrayList<>();
        SimpleSpinnerAdapter<Course> courseAdapter = new SimpleSpinnerAdapter<>(
                requireContext(),
                teacherCourses,
                course -> {
                    if (course == null || !Tool.boolOf(course.getCourseName())) {
                        return "Unnamed Course";
                    }
                    return course.getCourseName();
                }
        );
        sp_AgendaOfCourse.setAdapter(courseAdapter);
        return teacherCourses;
    }
    private void loadStudentsForCourse(Course course) {
        if (course == null) {
            cg_SelectStudent.removeAllViews();
            Tool.handleEmpty(true, cg_SelectStudent, tv_noStudents);
            setMeetingSpinner(new ArrayList<>());
            return;
        }
        course.getStudents().addOnSuccessListener(students -> {
            cg_SelectStudent.removeAllViews();
            ArrayList<Student> courseStudents = students != null ? (ArrayList<Student>) students : new ArrayList<>();
            if (courseStudents.isEmpty()) {
                Tool.handleEmpty(true, cg_SelectStudent, tv_noStudents);
                setMeetingSpinner(new ArrayList<>());
                shouldApplyPresetStudentSelection = false;
                return;
            }
            Tool.handleEmpty(false, cg_SelectStudent, tv_noStudents);
            for (Student student : courseStudents) {
                if (student == null) {
                    continue;
                }
                Chip chip = new Chip(requireContext());
                chip.setText(Tool.boolOf(student.getFullName()) ? student.getFullName() : student.getNickname());
                chip.setCheckable(true);
                chip.setCheckedIconVisible(true);
                chip.setTag(student);

                String studentId = resolveStudentId(student);
                if (shouldApplyPresetStudentSelection && Tool.boolOf(studentId) && presetStudentIds.contains(studentId)) {
                    chip.setChecked(true);
                }

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    Student firstSelected = getFirstSelectedStudent();
                    if (firstSelected != null) {
                        loadMeetings(selectedCourse, firstSelected);
                    } else {
                        setMeetingSpinner(new ArrayList<>());
                    }
                });
                cg_SelectStudent.addView(chip);
            }
            shouldApplyPresetStudentSelection = false;
            Student firstSelected = getFirstSelectedStudent();
            if (firstSelected != null) {
                loadMeetings(course, firstSelected);
            } else {
                setMeetingSpinner(new ArrayList<>());
            }
        }).addOnFailureListener(e -> {
            cg_SelectStudent.removeAllViews();
            Tool.handleEmpty(true, cg_SelectStudent, tv_noStudents);
            setMeetingSpinner(new ArrayList<>());
            shouldApplyPresetStudentSelection = false;
        });
    }

    private void loadMeetings(Course course, Student student) {
        if (course == null || student == null) {
            setMeetingSpinner(new ArrayList<>());
            return;
        }
        student.getAllMeetingOfCourse(course, 5).addOnSuccessListener(meetings -> {
            if(meetings != null){
                Log.d("TeacherAddAgenda", "Meetings Size" + meetings.size());
            }
            setMeetingSpinner(meetings != null ? meetings : new ArrayList<>());
        });
    }

    private void setMeetingSpinner(ArrayList<Meeting> meetings) {
        selectedMeeting = null;
        SimpleSpinnerAdapter<Meeting> meetingAdapter = new SimpleSpinnerAdapter<>(
                requireContext(),
                meetings,
                meeting -> {
                    if (meeting == null) {
                        return "Unknown Meeting";
                    }
                    String date = meeting.getDateOfMeeting() != null
                            ? meeting.getDateOfMeeting().toString()
                            : "No Date";
                    String time = meeting.getStartTimeChange() != null
                            ? meeting.getStartTimeChange().toString()
                            : "No Time";
                    return date + " " + time;
                }
        );
        sp_SelectMeeting.setAdapter(meetingAdapter);
    }

    private Student getFirstSelectedStudent() {
        for (int i = 0; i < cg_SelectStudent.getChildCount(); i++) {
            if (!(cg_SelectStudent.getChildAt(i) instanceof Chip)) {
                continue;
            }
            Chip chip = (Chip) cg_SelectStudent.getChildAt(i);
            if (chip.isChecked() && chip.getTag() instanceof Student) {
                return (Student) chip.getTag();
            }
        }
        return null;
    }

    private ArrayList<Student> getSelectedStudentsFromChips() {
        ArrayList<Student> selectedStudents = new ArrayList<>();
        for (int i = 0; i < cg_SelectStudent.getChildCount(); i++) {
            if (!(cg_SelectStudent.getChildAt(i) instanceof Chip)) {
                continue;
            }
            Chip chip = (Chip) cg_SelectStudent.getChildAt(i);
            if (chip.isChecked() && chip.getTag() instanceof Student) {
                selectedStudents.add((Student) chip.getTag());
            }
        }
        return selectedStudents;
    }

    private String resolveStudentId(Student student) {
        if (student == null) {
            return "";
        }
        if (Tool.boolOf(student.getUid())) {
            return student.getUid();
        }
        return Tool.boolOf(student.getID()) ? student.getID() : "";
    }

    private void handleFileAgenda(ArrayList<Uri> uris,
                                  Meeting meeting,
                                  ArrayList<Student> selectedStudents,
                                  Course course,
                                  boolean shouldNotifyStudents) {
        if (uris == null || uris.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one image first.", Toast.LENGTH_SHORT).show();
            return;
        }

        AtomicInteger pendingUploads = new AtomicInteger(uris.size());
        AtomicInteger successUploads = new AtomicInteger(0);
        AtomicBoolean hasFailure = new AtomicBoolean(false);

        for (Uri uri : uris) {
            if (uri == null) {
                hasFailure.set(true);
                if (pendingUploads.decrementAndGet() == 0) {
                    finishImageAgendaUpload(successUploads.get(), hasFailure.get(), shouldNotifyStudents, selectedStudents, course);
                }
                continue;
            }

            File uploadFile;
            try {
                uploadFile = Tool.uriToFile(uri, requireActivity());
            } catch (IOException e) {
                Log.e("TeacherAddAgenda", "Failed preparing selected image: " + uri, e);
                hasFailure.set(true);
                if (pendingUploads.decrementAndGet() == 0) {
                    finishImageAgendaUpload(successUploads.get(), hasFailure.get(), shouldNotifyStudents, selectedStudents, course);
                }
                continue;
            }

            Tool.uploadToCloudinary(uploadFile, new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(String secureUrl) {
                    file fileObj = new file(
                            Tool.getFileName(requireActivity(), uri),
                            secureUrl,
                            teacher,
                            LocalDateTime.now()
                    );

                    for (Student s : selectedStudents) {
                        Agenda agenda = new Agenda(fileObj, LocalDate.now(), meeting, teacher, s, course.getCourseID());
                        s.assignAgenda(agenda);
                        agenda.getRepositoryInstance().save(agenda);
                    }
                    successUploads.incrementAndGet();
                    if (pendingUploads.decrementAndGet() == 0) {
                        finishImageAgendaUpload(successUploads.get(), hasFailure.get(), shouldNotifyStudents, selectedStudents, course);
                    }
                }

                @Override
                public void onError(DatabaseError error) {
                    hasFailure.set(true);
                    if (Tool.isConnectivityIssue(error)) {
                        Log.w("TeacherAddAgenda", "Image upload failed due to connectivity issue: " + uri);
                    } else {
                        Log.e("TeacherAddAgenda", "Image upload failed: " + uri + " - " + error.getMessage());
                    }
                    if (pendingUploads.decrementAndGet() == 0) {
                        finishImageAgendaUpload(successUploads.get(), hasFailure.get(), shouldNotifyStudents, selectedStudents, course);
                    }
                }
            });
        }
    }

    private void finishImageAgendaUpload(int successUploads,
                                         boolean hasFailure,
                                         boolean shouldNotifyStudents,
                                         ArrayList<Student> selectedStudents,
                                         Course course) {
        if (!isAdded()) {
            return;
        }
        if (successUploads > 0 && shouldNotifyStudents) {
            notifyStudentsForAgenda(selectedStudents, course);
        }

        if (successUploads <= 0) {
            Toast.makeText(requireContext(), "Failed to upload selected images.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasFailure) {
            Toast.makeText(requireContext(), "Some images uploaded successfully, but some failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedImageUris.clear();
        imagePreviewAdapter.notifyDataSetChanged();
        updateImagePreviewState();
        Toast.makeText(requireContext(), "Agenda sent with selected images.", Toast.LENGTH_SHORT).show();
    }

    private void handleTextAgenda(Meeting meeting,
                                  ArrayList<Student> selectedStudents,
                                  Course course,
                                  boolean shouldNotifyStudents) {

        String content = etml_AgendaContents.getText().toString();
        for (Student s : selectedStudents) {
            Agenda agenda = new Agenda(content, LocalDate.now(), meeting, teacher, s, course.getCourseID());
            s.assignAgenda(agenda);
            agenda.getRepositoryInstance().save(agenda);
        }
        if (shouldNotifyStudents) {
            notifyStudentsForAgenda(selectedStudents, course);
        }
    }

    private void notifyStudentsForAgenda(ArrayList<Student> students, Course course) {
        if (teacher == null || students == null || students.isEmpty() || course == null) {
            return;
        }
        String teacherName = Tool.boolOf(teacher.getFullName()) ? teacher.getFullName() : "Your teacher";
        String title = "New Agenda Posted";
        String content = teacherName + " posted a new agenda in " + course.getCourseName() + ".";
        NotificationService.notifyUsers(students, teacher, title, content)
                .addOnFailureListener(e -> android.util.Log.w("TeacherAddAgenda", "Failed sending agenda notifications", e));
    }


    private void addSelectedImageUri(Uri uri) {
        if (uri == null) {
            return;
        }
        String candidate = uri.toString();
        for (Uri selectedUri : selectedImageUris) {
            if (selectedUri != null && candidate.equals(selectedUri.toString())) {
                return;
            }
        }
        selectedImageUris.add(uri);
    }

    private void updateImagePreviewState() {
        if (tb_AgendaFormat == null || ll_noImageSelected == null || rv_agendaImagePreviews == null) {
            return;
        }
        boolean isTextMode = tb_AgendaFormat.isChecked();
        if (isTextMode) {
            ll_noImageSelected.setVisibility(View.GONE);
            rv_agendaImagePreviews.setVisibility(View.GONE);
            return;
        }
        if (selectedImageUris.isEmpty()) {
            ll_noImageSelected.setVisibility(View.VISIBLE);
            rv_agendaImagePreviews.setVisibility(View.GONE);
        } else {
            ll_noImageSelected.setVisibility(View.GONE);
            rv_agendaImagePreviews.setVisibility(View.VISIBLE);
        }
    }

    private void applyAgendaFormatState(boolean isTextMode) {
        if (etml_AgendaContents == null || btn_UploadImageAgenda == null) {
            return;
        }
        if (isTextMode) {
            etml_AgendaContents.setVisibility(View.VISIBLE);
            btn_UploadImageAgenda.setVisibility(View.GONE);
        } else {
            etml_AgendaContents.setVisibility(View.GONE);
            btn_UploadImageAgenda.setVisibility(View.VISIBLE);
        }
        updateImagePreviewState();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

}

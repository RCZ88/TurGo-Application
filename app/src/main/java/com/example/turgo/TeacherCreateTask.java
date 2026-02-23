package com.example.turgo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class TeacherCreateTask extends Fragment {

    private EditText et_taskTitle, et_taskDescription;
    private Spinner sp_taskOfCourse;
    private ChipGroup cg_selectStudent; // Replaced Spinner with ChipGroup
    private Button btn_createTask;
    private CheckBox cb_openDropbox;
    private CheckBox cb_notifyStudents;
    private TextView tv_selectDate, tv_selectTime,  tv_presetStudentsSelected, tv_presetCourseSelected, tv_noStudentFound;
    private MaterialCardView cardSelectDate, cardSelectTime;

    private static final String DEFAULT_DATE_LABEL = "Select Date";
    private static final String DEFAULT_TIME_LABEL = "Select Time";
    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Teacher teacher;
    private Course coursePreset = null;
    private final ArrayList<Student> studentSelectedList = new ArrayList<>();
    private final Course[] courseSelected = {null};
    private final Set<String> presetStudentUids = new HashSet<>();
    private boolean shouldApplyPresetSelection = false;

    public TeacherCreateTask() {
        // Required empty public constructor
    }

    public static TeacherCreateTask newInstance() {
        return new TeacherCreateTask();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_create_task, container, false);

        // 1. Initialize Views
        et_taskTitle = view.findViewById(R.id.et_TCT_TaskTitle);
        et_taskDescription = view.findViewById(R.id.etml_TCT_TaskDescription);
        sp_taskOfCourse = view.findViewById(R.id.sp_TCT_TaskOfCourse);
        cg_selectStudent = view.findViewById(R.id.cg_TCT_SelectStudent);

        cb_openDropbox = view.findViewById(R.id.cb_TCT_OpenDropbox);
        cb_notifyStudents = view.findViewById(R.id.cb_TCT_NotifyStudents);
        btn_createTask = view.findViewById(R.id.btn_TCT_CreateTask);
        tv_presetStudentsSelected = view.findViewById(R.id.tv_TCK_PresetStudent);
        tv_presetCourseSelected = view.findViewById(R.id.tv_TCK_PresetCourse);
        tv_selectDate = view.findViewById(R.id.tv_TCT_SelectedDate); // Added missing mappings
        tv_selectTime = view.findViewById(R.id.tv_TCT_SelectedTime);
        tv_noStudentFound = view.findViewById(R.id.tv_TCT_StudentsEmpty);
        cardSelectDate = view.findViewById(R.id.card_TCT_SelectDate);
        cardSelectTime = view.findViewById(R.id.card_TCT_SelectTime);

        teacher = ((TeacherScreen) requireActivity()).getTeacher();

        ArrayList<Course> coursesTeach = new ArrayList<>();
        if (teacher.getCoursesTeach() != null) {
            coursesTeach = teacher.getCoursesTeach();
        }

        // 2. Read preset data from arguments (if opened from course page)
        Bundle args = getArguments();
        if (args != null) {
            coursePreset = (Course) args.getSerializable("presetCourse");
            ArrayList<Student> presetStudents = null;
            if (args.getSerializable("studentSelectedMenu") instanceof ArrayList) {
                presetStudents = (ArrayList<Student>) args.getSerializable("studentSelectedMenu");
            } else if (args.getSerializable("presetStudent") instanceof ArrayList) {
                presetStudents = (ArrayList<Student>) args.getSerializable("presetStudent");
            } else if (args.getSerializable("presetStudent") instanceof Student) {
                Student s = (Student) args.getSerializable("presetStudent");
                presetStudents = new ArrayList<>();
                if (s != null) {
                    presetStudents.add(s);
                }
            }

            if (presetStudents != null) {
                for (Student s : presetStudents) {
                    if (s != null && Tool.boolOf(s.getUid())) {
                        presetStudentUids.add(s.getUid());
                    }
                }
                shouldApplyPresetSelection = !presetStudentUids.isEmpty();
            }
        }

        // Always use spinner + chipgroup (same behavior as bottom-nav entry)
        tv_presetStudentsSelected.setVisibility(View.GONE);
        tv_presetCourseSelected.setVisibility(View.GONE);
        cg_selectStudent.setVisibility(View.VISIBLE);
        sp_taskOfCourse.setVisibility(View.VISIBLE);

        // 3. Setup Course Spinner
        SimpleSpinnerAdapter<Course> cAdapter = new SimpleSpinnerAdapter<>(
                requireContext(),
                coursesTeach,
                Course::getCourseName
        );
        sp_taskOfCourse.setAdapter(cAdapter);

        ArrayList<Course> finalCoursesTeach = coursesTeach;
        sp_taskOfCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= finalCoursesTeach.size()) {
                    return;
                }
                Course selected = finalCoursesTeach.get(position);
                courseSelected[0] = selected;
                loadStudentChipsForCourse(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (coursePreset != null && Tool.boolOf(coursePreset.getID()) && !coursesTeach.isEmpty()) {
            for (int i = 0; i < coursesTeach.size(); i++) {
                Course c = coursesTeach.get(i);
                if (c != null && Tool.boolOf(c.getID()) && c.getID().equals(coursePreset.getID())) {
                    sp_taskOfCourse.setSelection(i);
                    courseSelected[0] = c;
                    break;
                }
            }
        }


        // 4. Handle Date Picking
        tv_selectDate.setOnClickListener(v -> openDatePicker());
        tv_selectTime.setOnClickListener(v -> openTimePicker());
        cardSelectDate.setOnClickListener(v -> openDatePicker());
        cardSelectTime.setOnClickListener(v -> openTimePicker());

        // 5. Handle Form Submission
        btn_createTask.setOnClickListener(v -> {

            studentSelectedList.clear();
            for (int i = 0; i < cg_selectStudent.getChildCount(); i++) {
                Chip chip = (Chip) cg_selectStudent.getChildAt(i);
                if (chip.isChecked()) {
                    studentSelectedList.add((Student) chip.getTag());
                }
            }

            // Check if form is complete
            boolean hasTitle = !et_taskTitle.getText().toString().trim().isEmpty();
            boolean hasDesc = !et_taskDescription.getText().toString().trim().isEmpty();
            Course finalCourse = courseSelected[0] != null ? courseSelected[0] : (Course) sp_taskOfCourse.getSelectedItem();
            boolean hasCourse = finalCourse != null;
            boolean hasStudents = !studentSelectedList.isEmpty();
            boolean hasDate = !tv_selectDate.getText().toString().trim().equals(DEFAULT_DATE_LABEL);
            boolean hasTime = !tv_selectTime.getText().toString().trim().equals(DEFAULT_TIME_LABEL);

            if (hasTitle && hasDesc && hasCourse && hasStudents && hasDate && hasTime) {

                String taskTitle = et_taskTitle.getText().toString();
                String taskDescription = et_taskDescription.getText().toString();
                boolean shouldNotifyStudents = cb_notifyStudents != null && cb_notifyStudents.isChecked();

                LocalDateTime submissionDate;
                try {
                    String dateText = tv_selectDate.getText().toString().trim();
                    String timeText = tv_selectTime.getText().toString().trim();
                    submissionDate = LocalDateTime.parse(dateText + " " + timeText, DEADLINE_FORMATTER);
                } catch (DateTimeParseException e) {
                    Toast.makeText(getActivity(), "Invalid date/time format. Please reselect deadline.", Toast.LENGTH_LONG).show();
                    return;
                }

                AtomicReference<Task> task = new AtomicReference<>(
                        new Task(
                                taskTitle,
                                taskDescription,
                                submissionDate,
                                finalCourse != null ? finalCourse.getID() : null,
                                null,
                                teacher != null ? teacher.getID() : null,
                                Tool.streamToArray(studentSelectedList.stream().map(Student::getUid))
                        )
                );

                task.get().setManualCompletionRequired(!cb_openDropbox.isChecked());

                Runnable assignTaskToStudents = () -> {
                    for (Student s : studentSelectedList) {
                        try {
                            s.assignTask(task.get());
                        } catch (InvocationTargetException | NoSuchMethodException |
                                 IllegalAccessException | java.lang.InstantiationException e) {
                            e.printStackTrace();
                        }
                    }
                };

                Runnable finalizeTaskCreation = () -> {
                    TaskRepository tr = new TaskRepository(task.get().getTaskID());
                    tr.save(task.get());
                    assignTaskToStudents.run();

                    if (shouldNotifyStudents && teacher != null && finalCourse != null) {
                        String teacherName = Tool.boolOf(teacher.getFullName()) ? teacher.getFullName() : "Your teacher";
                        String title = "New Task Assigned";
                        String content = teacherName + " assigned '" + taskTitle + "' in " + finalCourse.getCourseName() + ".";
                        NotificationService.notifyUsers(studentSelectedList, teacher, title, content)
                                .addOnFailureListener(e -> android.util.Log.w("TeacherCreateTask", "Failed sending task notifications", e));
                    }

                    Toast.makeText(getActivity(), "Task Created Successfully!", Toast.LENGTH_SHORT).show();
                };

                if (cb_openDropbox.isChecked()) {
                    task.get().enableDropbox().addOnSuccessListener(nothing -> {
                        Dropbox dropbox = task.get().getDropboxCached();
                        if (dropbox != null) {
                            DropboxRepository dr = new DropboxRepository(dropbox.getID());
                            dr.save(dropbox);
                            TaskRepository tr = new TaskRepository(task.get().getID());
                            tr.save(task.get());
                        }
                        finalizeTaskCreation.run();
                    }).addOnFailureListener(e ->
                            Toast.makeText(getActivity(), "Failed to enable dropbox.", Toast.LENGTH_LONG).show());
                } else {
                    finalizeTaskCreation.run();
                }

            } else {
                Toast.makeText(getActivity(), "Please fill all fields, pick date and time, and select at least one student.", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private void loadStudentChipsForCourse(Course selected) {
        if (selected == null) {
            cg_selectStudent.removeAllViews();
            Tool.handleEmpty(true, cg_selectStudent, tv_noStudentFound);
            return;
        }

        selected.getStudents().addOnSuccessListener(students -> {
            cg_selectStudent.removeAllViews();
            if (students == null || students.isEmpty()) {
                tv_noStudentFound.setText(selected.getCourseName() + " has no student.");
                Tool.handleEmpty(true, cg_selectStudent, tv_noStudentFound);
                shouldApplyPresetSelection = false;
                return;
            }

            Tool.handleEmpty(false, cg_selectStudent, tv_noStudentFound);
            for (Student student : students) {
                if (student == null) {
                    continue;
                }
                Chip chip = new Chip(requireContext());
                chip.setText(student.getFullName());
                chip.setCheckable(true);
                chip.setCheckedIconVisible(true);
                chip.setTag(student);
                if (shouldApplyPresetSelection && Tool.boolOf(student.getUid()) && presetStudentUids.contains(student.getUid())) {
                    chip.setChecked(true);
                }
                cg_selectStudent.addView(chip);
            }
            shouldApplyPresetSelection = false;
        }).addOnFailureListener(e -> {
            cg_selectStudent.removeAllViews();
            tv_noStudentFound.setText("Failed to load students.");
            Tool.handleEmpty(true, cg_selectStudent, tv_noStudentFound);
            shouldApplyPresetSelection = false;
        });
    }

    private void openTimePicker() {
        // Get the current time to set as the default in the dialog
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create and show the TimePickerDialog
        TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    // Format the time to always be 2 digits (e.g., "09:05" instead of "9:5")
                    // This is CRUCIAL so LocalDateTime.parse() doesn't crash later
                    @SuppressLint("DefaultLocale")
                    String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);

                    // Update the TextView on your new card
                    tv_selectTime.setText(formattedTime);
                },
                hour,
                minute,
                true // true = 24-hour format (14:30), false = AM/PM format (2:30 PM)
        );

        timePickerDialog.show();
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Formatting to ensure MM and DD have leading zeros if needed for LocalDateTime.parse
                    @SuppressLint("DefaultLocale")
                    String selectedDate = String.format("%04d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay);
                    tv_selectDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
}

package com.example.turgo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminCourseFullPage extends AppCompatActivity {
    public static final String EXTRA_DTA = "dtaObj";
    public static final String ACTION_ADMIN_DTA_FULL_PAGE = "com.example.turgo.ADMIN_DTA_FULL_PAGE";

    private Course course;
    private CourseRepository courseRepository;

    private TextView tvCourseId;
    private EditText etCourseName;
    private EditText etDescription;
    private AutoCompleteTextView actvCourseType;
    private AutoCompleteTextView actvTeacher;
    private EditText etBaseCost;
    private EditText etHourlyCost;
    private EditText etMonthlyDiscount;
    private EditText etMaxStudents;
    private SwitchMaterial swAutoAccept;
    private SwitchMaterial swPayPerMonth;
    private SwitchMaterial swPayPerMeeting;
    private SwitchMaterial swPrivateMode;
    private SwitchMaterial swGroupMode;
    private TextView tvLogoPath;
    private TextView tvBannerPath;
    private ImageView ivLogoPreview;
    private ImageView ivBannerPreview;
    private RecyclerView rvImages;
    private RecyclerView rvDtas;
    private TextView tvCountStudents;
    private TextView tvCountSchedules;
    private TextView tvCountAgendas;
    private TextView tvCountStudentCourses;
    private MaterialCardView cvBack;
    private MaterialButton btnSaveTop;
    private MaterialButton btnSaveBottom;
    private MaterialButton btnDiscard;
    private MaterialButton btnDeleteCourse;
    private MaterialButton btnChangeLogo;
    private MaterialButton btnChangeBanner;
    private MaterialButton btnAddImage;
    private MaterialButton btnAddDta;

    private final ArrayList<CourseType> allCourseTypes = new ArrayList<>();
    private final ArrayList<Teacher> allTeachers = new ArrayList<>();
    private CourseType selectedCourseType;
    private Teacher selectedTeacher;
    private DTAAdapter dtaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_course_full_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        course = (Course) getIntent().getSerializableExtra(Course.SERIALIZE_KEY_CODE);
        if (course == null) {
            Toast.makeText(this, "No course selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        courseRepository = new CourseRepository(course.getID());

        bindViews();
        setupStaticClicks();
        setupRecycler();
        loadDropdownData();
        bindCourseToUi();
    }

    private void bindViews() {
        tvCourseId = findViewById(R.id.tv_ACFP_courseId);
        etCourseName = findViewById(R.id.et_ACFP_courseName);
        etDescription = findViewById(R.id.et_ACFP_description);
        actvCourseType = findViewById(R.id.actv_ACFP_courseType);
        actvTeacher = findViewById(R.id.actv_ACFP_teacher);
        etBaseCost = findViewById(R.id.et_ACFP_baseCost);
        etHourlyCost = findViewById(R.id.et_ACFP_hourlyCost);
        etMonthlyDiscount = findViewById(R.id.et_ACFP_monthlyDiscount);
        etMaxStudents = findViewById(R.id.et_ACFP_maxStudents);
        swAutoAccept = findViewById(R.id.sw_ACFP_autoAccept);
        swPayPerMonth = findViewById(R.id.sw_ACFP_payPerMonth);
        swPayPerMeeting = findViewById(R.id.sw_ACFP_payPerMeeting);
        swPrivateMode = findViewById(R.id.sw_ACFP_privateMode);
        swGroupMode = findViewById(R.id.sw_ACFP_groupMode);
        tvLogoPath = findViewById(R.id.tv_ACFP_logoPath);
        tvBannerPath = findViewById(R.id.tv_ACFP_bannerPath);
        ivLogoPreview = findViewById(R.id.iv_ACFP_logoPreview);
        ivBannerPreview = findViewById(R.id.iv_ACFP_bannerPreview);
        rvImages = findViewById(R.id.rv_ACFP_images);
        rvDtas = findViewById(R.id.rv_ACFP_dtas);
        tvCountStudents = findViewById(R.id.tv_ACFP_countStudents);
        tvCountSchedules = findViewById(R.id.tv_ACFP_countSchedules);
        tvCountAgendas = findViewById(R.id.tv_ACFP_countAgendas);
        tvCountStudentCourses = findViewById(R.id.tv_ACFP_countStudentCourses);
        cvBack = findViewById(R.id.cv_ACFP_back);
        btnSaveTop = findViewById(R.id.btn_ACFP_saveTop);
        btnSaveBottom = findViewById(R.id.btn_ACFP_save);
        btnDiscard = findViewById(R.id.btn_ACFP_discard);
        btnDeleteCourse = findViewById(R.id.btn_ACFP_deleteCourse);
        btnChangeLogo = findViewById(R.id.btn_ACFP_changeLogo);
        btnChangeBanner = findViewById(R.id.btn_ACFP_changeBanner);
        btnAddImage = findViewById(R.id.btn_ACFP_addImage);
        btnAddDta = findViewById(R.id.btn_ACFP_addDTA);
    }

    private void setupStaticClicks() {
        cvBack.setOnClickListener(v -> finish());
        btnSaveTop.setOnClickListener(v -> saveCourseChanges());
        btnSaveBottom.setOnClickListener(v -> saveCourseChanges());
        btnDiscard.setOnClickListener(v -> bindCourseToUi());
        btnDeleteCourse.setOnClickListener(v -> deleteCourse());

        btnChangeLogo.setOnClickListener(v ->
                Toast.makeText(this, "Logo editor not implemented yet", Toast.LENGTH_SHORT).show());
        btnChangeBanner.setOnClickListener(v ->
                Toast.makeText(this, "Banner editor not implemented yet", Toast.LENGTH_SHORT).show());
        btnAddImage.setOnClickListener(v ->
                Toast.makeText(this, "Add image flow not implemented yet", Toast.LENGTH_SHORT).show());
        btnAddDta.setOnClickListener(v ->
                Toast.makeText(this, "Add DTA flow not implemented yet", Toast.LENGTH_SHORT).show());
    }

    private void setupRecycler() {
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ArrayList<String> imageUrls = course.getImagesCloudinary() == null ? new ArrayList<>() : course.getImagesCloudinary();
        rvImages.setAdapter(new CourseImageAdapter(imageUrls));

        ArrayList<DayTimeArrangement> dtaList = course.getDayTimeArrangement() == null
                ? new ArrayList<>()
                : course.getDayTimeArrangement();
        dtaAdapter = new DTAAdapter(dtaList);
        dtaAdapter.setListener(new OnItemClickListener<>() {
            @Override
            public void onItemClick(Integer item) {
                Toast.makeText(AdminCourseFullPage.this, "Remove DTA flow not implemented yet", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(Integer item) {
                if (item == null || item < 0 || item >= dtaList.size()) {
                    return;
                }
                openDtaFullPage(dtaList.get(item));
            }
        });
        rvDtas.setLayoutManager(new LinearLayoutManager(this));
        rvDtas.setAdapter(dtaAdapter);
    }

    private void loadDropdownData() {
        RequireUpdate.getAllObjects(CourseType.class).addOnSuccessListener(courseTypes -> {
            allCourseTypes.clear();
            ArrayList<String> labels = new ArrayList<>();
            for (CourseType type : courseTypes) {
                if (type == null) {
                    continue;
                }
                allCourseTypes.add(type);
                labels.add(type.getCourseType());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels);
            actvCourseType.setAdapter(adapter);

            if (course.getCourseType() != null) {
                actvCourseType.setText(course.getCourseType().getCourseType(), false);
                selectedCourseType = course.getCourseType();
            }
        });

        RequireUpdate.getAllObjects(Teacher.class).addOnSuccessListener(teachers -> {
            allTeachers.clear();
            ArrayList<String> labels = new ArrayList<>();
            int selectedIndex = -1;

            for (int i = 0; i < teachers.size(); i++) {
                Teacher teacher = teachers.get(i);
                if (teacher == null) {
                    continue;
                }
                allTeachers.add(teacher);
                labels.add(teacher.getFullName());
                if (course.getTeacherId() != null && course.getTeacherId().equals(teacher.getID())) {
                    selectedIndex = labels.size() - 1;
                    selectedTeacher = teacher;
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels);
            actvTeacher.setAdapter(adapter);
            if (selectedIndex >= 0 && selectedIndex < labels.size()) {
                actvTeacher.setText(labels.get(selectedIndex), false);
            }
        });

        actvCourseType.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < allCourseTypes.size()) {
                selectedCourseType = allCourseTypes.get(position);
            }
        });

        actvTeacher.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < allTeachers.size()) {
                selectedTeacher = allTeachers.get(position);
            }
        });
    }

    private void bindCourseToUi() {
        tvCourseId.setText(course.getID());
        etCourseName.setText(valueOrEmpty(course.getCourseName()));
        etDescription.setText(valueOrEmpty(course.getCourseDescription()));
        etBaseCost.setText(String.valueOf(course.getBaseCost()));
        etHourlyCost.setText(String.valueOf(course.getHourlyCost()));
        etMonthlyDiscount.setText(String.valueOf(course.getMonthlyDiscountPercentage()));
        etMaxStudents.setText(String.valueOf(course.getMaxStudentPerMeeting()));
        swAutoAccept.setChecked(course.isAutoAcceptStudent());

        ArrayList<Boolean> paymentPer = safeBoolList(course.getPaymentPer(), 2);
        swPayPerMonth.setChecked(paymentPer.get(Course.PER_MONTH_INDEX));
        swPayPerMeeting.setChecked(paymentPer.get(Course.PER_MEETING_INDEX));

        ArrayList<Boolean> privateGroup = safeBoolList(course.getPrivateGroup(), 2);
        swPrivateMode.setChecked(privateGroup.get(Course.PRIVATE_INDEX));
        swGroupMode.setChecked(privateGroup.get(Course.GROUP_INDEX));

        tvLogoPath.setText(valueOrEmpty(course.getLogoCloudinary()));
        tvBannerPath.setText(valueOrEmpty(course.getBackgroundCloudinary()));
        if (Tool.boolOf(course.getLogoCloudinary())) {
            Tool.setImageCloudinary(this, course.getLogoCloudinary(), ivLogoPreview);
        }
        if (Tool.boolOf(course.getBackgroundCloudinary())) {
            Tool.setImageCloudinary(this, course.getBackgroundCloudinary(), ivBannerPreview);
        }

        tvCountStudents.setText(String.valueOf(course.getStudentsId() == null ? 0 : course.getStudentsId().size()));
        tvCountSchedules.setText(String.valueOf(course.getSchedules() == null ? 0 : course.getSchedules().size()));
        tvCountAgendas.setText(String.valueOf(course.getAgendas() == null ? 0 : course.getAgendas().size()));
        tvCountStudentCourses.setText(String.valueOf(course.getStudentsCourse() == null ? 0 : course.getStudentsCourse().size()));
    }

    private void saveCourseChanges() {
        String courseName = etCourseName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        if (courseName.isEmpty()) {
            Toast.makeText(this, "Course name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCourseType == null) {
            Toast.makeText(this, "Select a course type", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTeacher == null) {
            Toast.makeText(this, "Select a teacher", Toast.LENGTH_SHORT).show();
            return;
        }

        double baseCost = parseDoubleOrDefault(etBaseCost, course.getBaseCost());
        double hourlyCost = parseDoubleOrDefault(etHourlyCost, course.getHourlyCost());
        double monthlyDiscount = parseDoubleOrDefault(etMonthlyDiscount, course.getMonthlyDiscountPercentage());
        int maxStudents = parseIntOrDefault(etMaxStudents, course.getMaxStudentPerMeeting());

        ArrayList<Boolean> paymentPer = new ArrayList<>();
        paymentPer.add(swPayPerMonth.isChecked());
        paymentPer.add(swPayPerMeeting.isChecked());

        ArrayList<Boolean> privateGroup = new ArrayList<>();
        privateGroup.add(swPrivateMode.isChecked());
        privateGroup.add(swGroupMode.isChecked());

        Map<String, Object> updates = new HashMap<>();
        updates.put("courseName", courseName);
        updates.put("courseDescription", description);
        updates.put("courseType", selectedCourseType.getID());
        updates.put("teacher", selectedTeacher.getID());
        updates.put("baseCost", baseCost);
        updates.put("hourlyCost", hourlyCost);
        updates.put("monthlyDiscountPercentage", monthlyDiscount);
        updates.put("maxStudentPerMeeting", maxStudents);
        updates.put("autoAcceptStudent", swAutoAccept.isChecked());
        updates.put("paymentPer", paymentPer);
        updates.put("privateGroup", privateGroup);

        String oldTeacherId = course.getTeacherId();
        String newTeacherId = selectedTeacher.getID();

        courseRepository.getDbReference().updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    if (oldTeacherId != null && !oldTeacherId.equals(newTeacherId)) {
                        TeacherRepository oldRepo = new TeacherRepository(oldTeacherId);
                        oldRepo.removeCourseTeach(course.getID());
                        TeacherRepository newRepo = new TeacherRepository(newTeacherId);
                        newRepo.addCourseTeach(course);
                    }

                    course.setCourseName(courseName);
                    course.setCourseDescription(description);
                    course.setCourseType(selectedCourseType);
                    course.setTeacher(newTeacherId);
                    course.setBaseCost(baseCost);
                    course.setHourlyCost(hourlyCost);
                    course.setMonthlyDiscountPercentage(monthlyDiscount);
                    course.setMaxStudentPerMeeting(maxStudents);
                    course.setAutoAcceptStudent(swAutoAccept.isChecked());
                    course.setPaymentPer(paymentPer);
                    course.setPrivateGroup(privateGroup);

                    Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update course", Toast.LENGTH_SHORT).show());
    }

    private void deleteCourse() {
        courseRepository.getDbReference().removeValue()
                .addOnSuccessListener(unused -> {
                    if (course.getTeacherId() != null) {
                        new TeacherRepository(course.getTeacherId()).removeCourseTeach(course.getID());
                    }
                    Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete course", Toast.LENGTH_SHORT).show());
    }

    private void openDtaFullPage(DayTimeArrangement dta) {
        Intent intent = new Intent(ACTION_ADMIN_DTA_FULL_PAGE);
        intent.setPackage(getPackageName());
        intent.putExtra(Course.SERIALIZE_KEY_CODE, course);
        intent.putExtra(EXTRA_DTA, dta);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "DTA full page not created yet", Toast.LENGTH_SHORT).show();
        }
    }

    private static ArrayList<Boolean> safeBoolList(ArrayList<Boolean> source, int minSize) {
        ArrayList<Boolean> result = new ArrayList<>();
        if (source != null) {
            result.addAll(source);
        }
        while (result.size() < minSize) {
            result.add(false);
        }
        return result;
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static double parseDoubleOrDefault(EditText editText, double defaultValue) {
        try {
            String raw = editText.getText().toString().trim();
            if (raw.isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int parseIntOrDefault(EditText editText, int defaultValue) {
        try {
            String raw = editText.getText().toString().trim();
            if (raw.isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

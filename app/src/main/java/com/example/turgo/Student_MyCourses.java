package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Student_MyCourses extends Fragment {

    RecyclerView rv_myCourses;
    TextView tv_noCourse;
    Button btn_goToExplore;
    LinearLayout ll_empty;
    Student user;
    ArrayList<Teacher> teachersOfCourse = new ArrayList<>();
    private CourseAdapter adapter;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Student_MyCourses() {
        // Required empty public constructor
    }

    public static Student_MyCourses newInstance(String param1, String param2) {
        Student_MyCourses fragment = new Student_MyCourses();
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
            // No data loading needed here anymore
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student_my_courses, container, false);

        tv_noCourse = view.findViewById(R.id.tv_smc_NoCoursesJoined);
        btn_goToExplore = view.findViewById(R.id.btn_smc_NavigateExploreCourse);
        rv_myCourses = view.findViewById(R.id.rv_ListOfMyCourses);
        ll_empty = view.findViewById(R.id.ll_smc_EmptyState);

        rv_myCourses.setLayoutManager(new LinearLayoutManager(view.getContext()));
        loadActiveStudentThenCourses();

        btn_goToExplore.setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof StudentExploreJoined) {
                ((StudentExploreJoined) parent).showExploreTab();
                return;
            }
            Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), new Student_ExploreCourse());
        });

        return view;
    }

    private void loadActiveStudentThenCourses() {
        if (!isUiReady()) {
            return;
        }
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null || !Tool.boolOf(fbUser.getUid())) {
            rv_myCourses.setAdapter(null);
            Tool.handleEmpty(true, rv_myCourses, ll_empty);
            return;
        }
        String activeUid = fbUser.getUid();

        StudentScreen activity = (StudentScreen) getActivity();
        if (activity == null) {
            rv_myCourses.setAdapter(null);
            Tool.handleEmpty(true, rv_myCourses, ll_empty);
            return;
        }

        Student current = activity.getStudent();
        if (current != null && Tool.boolOf(current.getID()) && current.getID().equals(activeUid)) {
            user = current;
            Log.d("Student_MyCourse", "Using active student from screen uid=" + activeUid);
            loadCoursesDirectly();
            return;
        }

        new StudentRepository(activeUid).loadAsNormal()
                .addOnSuccessListener(freshStudent -> {
                    if (!isUiReady() || freshStudent == null) {
                        return;
                    }
                    user = freshStudent;
                    activity.setStudent(freshStudent);
                    Log.d("Student_MyCourse", "Refreshed active student uid=" + activeUid);
                    loadCoursesDirectly();
                })
                .addOnFailureListener(e -> {
                    if (!isUiReady()) {
                        return;
                    }
                    Log.e("Student_MyCourse", "Failed to refresh active student", e);
                    rv_myCourses.setAdapter(null);
                    Tool.handleEmpty(true, rv_myCourses, ll_empty);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        rv_myCourses = null;
        tv_noCourse = null;
        btn_goToExplore = null;
        ll_empty = null;
    }

    private boolean isUiReady() {
        return isAdded() && getContext() != null && rv_myCourses != null && ll_empty != null;
    }

    private void loadCoursesDirectly() {
        Log.d("Student_MyCourses", "!isUiReady(): " + !isUiReady() + "\n user == null: " + (user == null) + "\n!Tool.boolOf(user.getID())" + !Tool.boolOf(user.getID()));
        if (!isUiReady() || user == null || !Tool.boolOf(user.getID())) {

            if (rv_myCourses == null || ll_empty == null) {
                return;
            }
            rv_myCourses.setAdapter(null);
            Tool.handleEmpty(true, rv_myCourses, ll_empty);
            return;
        }
        Log.d("Student_MyCourses", "Safe");

        FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.STUDENT.getPath())
                .child(user.getID())
                .child("courseTaken")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isUiReady()) {
                            return;
                        }
                        ArrayList<String> courseIds = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String id = child.getValue(String.class);
                            Log.d("Student_MyCourses", " course Taken id: " +id);
                            if (Tool.boolOf(id)) {
                                courseIds.add(id);
                            }
                        }
                        // Keep insertion order but remove accidental duplicates.
                        courseIds = new ArrayList<>(new LinkedHashSet<>(courseIds));

                        if (courseIds.isEmpty()) {
                            user.setCourseTaken(new ArrayList<>());
                            rv_myCourses.setAdapter(null);
                            Tool.handleEmpty(true, rv_myCourses, ll_empty);
                            return;
                        }

                        ArrayList<Task<Course>> courseTasks = new ArrayList<>();
                        for (String courseId : courseIds) {
                            courseTasks.add(new CourseRepository(courseId).loadLite());
                        }

                        Tasks.whenAllComplete(courseTasks).addOnCompleteListener(task -> {
                            if (!isUiReady()) {
                                return;
                            }
                            ArrayList<Course> courses = new ArrayList<>();
                            for (Task<Course> courseTask : courseTasks) {
                                if (courseTask.isSuccessful() && courseTask.getResult() != null) {
                                    courses.add(courseTask.getResult());
                                } else if (courseTask.getException() != null) {
                                    Log.w("Student_MyCourses", "Failed loading a course", courseTask.getException());
                                }
                            }
                            user.setCourseTaken(courses);
                            Log.d("Student_MyCourses", "Loaded courseTaken directly. Count=" + courses.size());
                            Tool.handleEmpty(courses.isEmpty(), rv_myCourses, ll_empty);
                            if (!courses.isEmpty()) {
                                loadTeachersForCourses(courses).addOnCompleteListener(teacherTask -> {
                                    if (!isUiReady()) {
                                        return;
                                    }
                                    ArrayList<Teacher> teachers = new ArrayList<>();
                                    if (teacherTask.isSuccessful() && teacherTask.getResult() != null) {
                                        teachers = teacherTask.getResult();
                                    } else if (teacherTask.getException() != null) {
                                        Log.w("Student_MyCourses", "Failed loading teachers for courses", teacherTask.getException());
                                    }
                                    setupAdapter(courses, teachers);
                                });
                            } else {
                                rv_myCourses.setAdapter(null);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (!isUiReady()) {
                            return;
                        }
                        Log.e("Student_MyCourses", "Failed loading courseTaken", error.toException());
                        rv_myCourses.setAdapter(null);
                        Tool.handleEmpty(true, rv_myCourses, ll_empty);
                    }
                });
    }


    private Task<ArrayList<Teacher>> loadTeachersForCourses(ArrayList<Course> courses) {
        TaskCompletionSource<ArrayList<Teacher>> tcs = new TaskCompletionSource<>();
        ArrayList<Task<Teacher>> teacherTasks = new ArrayList<>();

        for (Course course : courses) {
            teacherTasks.add(course.getTeacher());
        }

        Tasks.whenAllComplete(teacherTasks).addOnCompleteListener(done -> {
            ArrayList<Teacher> teachers = new ArrayList<>();
            for (int i = 0; i < teacherTasks.size(); i++) {
                Task<Teacher> teacherTask = teacherTasks.get(i);
                if (teacherTask.isSuccessful()) {
                    teachers.add(teacherTask.getResult());
                } else {
                    teachers.add(null);
                    if (teacherTask.getException() != null) {
                        Log.w("Student_MyCourses", "Failed loading teacher for course index=" + i, teacherTask.getException());
                    }
                }
            }
            tcs.setResult(teachers);
        });

        return tcs.getTask();
    }

    private void setupAdapter(ArrayList<Course> courses, ArrayList<Teacher> teachers) {
        Context context = getContext();
        if (!isUiReady() || context == null) {
            return;
        }
        teachersOfCourse = new ArrayList<>();
        if (teachers != null) {
            teachersOfCourse.addAll(teachers);
        }
        while (teachersOfCourse.size() < courses.size()) {
            teachersOfCourse.add(null);
        }
        if (teachersOfCourse.size() > courses.size()) {
            teachersOfCourse = new ArrayList<>(teachersOfCourse.subList(0, courses.size()));
        }

        adapter = new CourseAdapter(courses, user, new OnItemClickListener<>() {
            @Override
            public void onItemClick(Course item) {
                selectCourse(item);
            }

            @Override
            public void onItemLongClick(Course item) {
                // No-op
            }
        }, teachersOfCourse, context, true);

        rv_myCourses.setAdapter(adapter);
    }

    public void selectCourse(Course course) {
        if (course == null || !Tool.boolOf(course.getID())) {
            return;
        }
        Bundle bundle = new Bundle();
        new CourseRepository(course.getID()).loadAsNormal().addOnSuccessListener(fullCourse -> {
            if (!isAdded()) {
                return;
            }
            if (fullCourse == null) {
                return;
            }
            bundle.putSerializable(Course.SERIALIZE_KEY_CODE, fullCourse);
            fullCourse.getTeacher().addOnSuccessListener(t ->{
                if (!isAdded()) {
                    return;
                }
                bundle.putSerializable(Teacher.SERIALIZE_KEY_CODE, t);
                Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), new CourseJoinedFullPage(), bundle);
            }).addOnFailureListener(e -> {
                if (!isAdded()) {
                    return;
                }
                Log.w("Student_MyCourses", "Failed to load teacher for selected course. Opening course anyway.", e);
                Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), new CourseJoinedFullPage(), bundle);
            });
        }).addOnFailureListener(e -> {
            Log.e("Student_MyCourses", "Failed to load full course before opening", e);
        });
    }
}

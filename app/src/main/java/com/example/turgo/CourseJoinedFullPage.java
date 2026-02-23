package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseJoinedFullPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseJoinedFullPage extends Fragment {
    Course course;
    Student student;
    TextView tv_courseTeacher, tv_courseDays, tv_nextMeetingDate, tv_noTaskMessage;
    CollapsingToolbarLayout ct_courseTitle;
    TextView tv_noAgendaMessage;
    ImageView iv_courseBackgroundImage;
    RecyclerView rv_listOfTasks, rv_latestAgenda;
    Button btn_viewAllAgenda, btn_viewScheduleDetailed;
    Toolbar tb_topBar;
    TaskAdapter taskAdapter;
    ArrayList<Task> tasks = new ArrayList<>();
    Teacher teacher;
    private ViewGroup contentContainer;
    private LinearLayout llEmptyState;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CourseJoinedFullPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment course_fullPage.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseJoinedFullPage newInstance(String param1, String param2) {
        CourseJoinedFullPage fragment = new CourseJoinedFullPage();
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
            loadItems(getArguments());
        }
    }

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course_full_page, container, false);
        assert getActivity() != null;
//        Intent intent = getActivity().getIntent();
//        course = (Course) intent.getSerializableExtra("Selected Course");

        StudentScreen ss = (StudentScreen)getActivity();
        student = ss.getStudent();

        tv_courseTeacher = view.findViewById(R.id.tv_cfp_TeacherName);
        tv_courseDays = view.findViewById(R.id.tv_cfp_ScheduleDays);
        tv_nextMeetingDate = view.findViewById(R.id.tv_cfp_NextMeeting);
        tv_noTaskMessage = view.findViewById(R.id.tv_cfp_noTasksMessage);
        ct_courseTitle = view.findViewById(R.id.ct_cfp_CourseTitle);
        iv_courseBackgroundImage = view.findViewById(R.id.iv_cfp_courseWallpaper);
        tv_noAgendaMessage = view.findViewById(R.id.tv_cfp_NoAgenda);
        btn_viewScheduleDetailed = view.findViewById(R.id.btn_cfp_fullScheduleView);
        llEmptyState = view.findViewById(R.id.ll_cfp_empty_state);
        contentContainer = view.findViewById(R.id.nsv_cfp_content);

        rv_listOfTasks = view.findViewById(R.id.rv_cfp_ListOfTasks);
        rv_latestAgenda = view.findViewById(R.id.rv_cfp_ListOFAgenda);
        btn_viewAllAgenda = view.findViewById(R.id.btn_ViewAllAgendaOfCourse);
        tb_topBar = view.findViewById(R.id.tb_cfp_backButton);

        tb_topBar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        boolean empty = course == null || student == null;
        Tool.handleEmpty(empty, contentContainer, llEmptyState);
        if (empty) {
            return view;
        }


        tasks = student.getAllTaskOfCourse(course);
        if (course != null) {
            ct_courseTitle.setTitle(course.getCourseName());
            student.getClosestMeetingOfCourse(course).addOnSuccessListener(meetingDate ->{
               if (!isUiReady()) {
                   return;
               }
               if(meetingDate != null){
                   tv_nextMeetingDate.setText(meetingDate.toString());
               }else{
                   Log.d("CJFP", "meeting is null!");
               }
            });
        }
        //async - completed
        if (teacher != null) {
            tv_courseTeacher.setText(teacher.getFullName());
        }
        if(course == null){
            Log.d("CJFP", "course is null");

        }
        if(student == null){
            Log.d("CJFP", "student is null");
        }
        if (course != null && student != null) {
            tv_courseDays.setText(course.getDaysOfSchedule(student));
        }

        tv_noTaskMessage.setText("Yay you're Free! No Task Found!");
        if (iv_courseBackgroundImage != null) {
            Tool.setImageCloudinary(getContext(), course.getBackgroundCloudinary(), iv_courseBackgroundImage);

            Log.d("Course", "getBackgroundCloudinary LINK: " + course.getBackgroundCloudinary());
        }else{
            Log.d("CourseJoinedFullPage", "iv_courseBackgroundImage is null");
        }

        btn_viewAllAgenda.setOnClickListener(v -> openAllAgendaPage());
        btn_viewScheduleDetailed.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
            Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), new StudentMeetings(), bundle);
        });

        loadTasks();
        setLatestAgenda();

        return view;
    }
    private void setLatestAgenda(){
        ArrayList<Agenda>theAgenda = new ArrayList<>();
        student.getLatestAgendaOfCourse(course, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Agenda object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, java.lang.InstantiationException {
                if (!isUiReady()) {
                    return;
                }
                if (object != null) {
                    theAgenda.add(object);
                }
                AgendaAdapter agendaAdapter = new AgendaAdapter(theAgenda);
                rv_latestAgenda.setLayoutManager(new LinearLayoutManager(getContext()));
                rv_latestAgenda.setAdapter(agendaAdapter);
                Tool.handleEmpty(theAgenda.isEmpty(), rv_latestAgenda, tv_noAgendaMessage);
                Log.d("CourseJoinedFullPage", "Agenda Empty?: "  + theAgenda.isEmpty());
            }

            @Override
            public void onError(DatabaseError error) {
                if (!isUiReady()) {
                    return;
                }
                rv_latestAgenda.setLayoutManager(new LinearLayoutManager(getContext()));
                rv_latestAgenda.setAdapter(new AgendaAdapter(new ArrayList<>()));
                Tool.handleEmpty(true, rv_latestAgenda, tv_noAgendaMessage);
                Log.e("CourseJoinedFullPage", "Failed loading latest agenda", error.toException());
            }
        });

    }

    private void openAllAgendaPage() {
        if (student == null || course == null || !isAdded()) {
            return;
        }
        student.getAgendaOfCourse(course, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(ArrayList<Agenda> object) {
                if (!isAdded()) {
                    return;
                }
                ArrayList<Agenda> agendas = object != null ? object : new ArrayList<>();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Agenda.SERIALIZE_KEY_CODE, agendas);
                Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), new AllAgendaPage(), bundle);
            }

            @Override
            public void onError(DatabaseError error) {
                if (!isAdded()) {
                    return;
                }
                Log.e("CourseJoinedFullPage", "Failed loading agendas for full page", error.toException());
                Bundle bundle = new Bundle();
                bundle.putSerializable(Agenda.SERIALIZE_KEY_CODE, new ArrayList<Agenda>());
                Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), new AllAgendaPage(), bundle);
            }
        });
    }
    private void loadTasks() {
        if (!isUiReady()) {
            return;
        }
        tasks = student.getAllTaskOfCourse(course); // Assume this method fetches tasks
        Tool.handleEmpty(tasks.isEmpty(), rv_listOfTasks, tv_noTaskMessage);
        if (!tasks.isEmpty()) {
            rv_listOfTasks.setLayoutManager(new LinearLayoutManager(getContext()));
            taskAdapter = new TaskAdapter(student.getAllTaskOfCourse(course), student, new OnItemClickListener<Task>() {
                @Override
                public void onItemClick(Task item) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Task.SERIALIZE_KEY_CODE, item);

                    TaskFullPage tfp = new TaskFullPage();
                    tfp.setArguments(bundle);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(StudentScreen.getContainer(), tfp)
                            .addToBackStack(null)
                            .commit();
                }

                @Override
                public void onItemLongClick(Task item) {

                }
            }, TaskItemMode.RECYCLER);
            rv_listOfTasks.setAdapter(taskAdapter);
        }
    }
    private void loadItems(Bundle input){
        course = (Course)input.getSerializable(Course.SERIALIZE_KEY_CODE);
        teacher = (Teacher)input.getSerializable(Teacher.SERIALIZE_KEY_CODE);
    }

    private boolean isUiReady() {
        return isAdded()
                && getContext() != null
                && tv_nextMeetingDate != null
                && rv_listOfTasks != null
                && rv_latestAgenda != null
                && contentContainer != null
                && llEmptyState != null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tv_courseTeacher = null;
        tv_courseDays = null;
        tv_nextMeetingDate = null;
        tv_noTaskMessage = null;
        ct_courseTitle = null;
        tv_noAgendaMessage = null;
        iv_courseBackgroundImage = null;
        rv_listOfTasks = null;
        rv_latestAgenda = null;
        btn_viewAllAgenda = null;
        btn_viewScheduleDetailed = null;
        tb_topBar = null;
        taskAdapter = null;
        contentContainer = null;
        llEmptyState = null;
    }
}


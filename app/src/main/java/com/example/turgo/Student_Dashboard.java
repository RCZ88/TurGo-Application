package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_Dashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_Dashboard extends Fragment implements RequiresDataLoading{
    RecyclerView rv_coursesCompletedThisWeek;
    FragmentContainerView fcv_upcomingSchedule;
    Student user;
    ArrayList<Task> studentTasks = new ArrayList<>();
    ViewPager2 vp2_listOfTasks;

    ProgressBar pb_progressThisWeek;
    ImageButton btn_details, ib_prevTask, ib_nextTask, btn_scanQR;
    TaskPagerAdapter adapter;
    TextView tv_noScheduleFound, tv_noTaskFound, tv_noMeetingCompleted;
    LinearLayout ll_Task;
    boolean expanded;

    Schedule scheduleOfNextMeeting;
    Course course;
    String roomId;
    ArrayList<Course>coursesForScheduleAdapter = new ArrayList<>();
    ArrayList<Schedule>schedulesCompleted = new ArrayList<>();
    Meeting nextMeeting;
    Teacher teacher;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Meeting currentMeeting;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Student_Dashboard() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudentDashboard.
     */
    // TODO: Rename and change types and number of parameters
    public static Student_Dashboard newInstance(String param1, String param2) {
        Student_Dashboard fragment = new Student_Dashboard();
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
            onDataLoaded(getArguments());
        }

    }

    @SuppressLint({"MissingInflatedId", "CommitTransaction", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_dashboard, container, false);
        rv_coursesCompletedThisWeek = view.findViewById(R.id.rv_CourseCompleted);
        tv_noScheduleFound = view.findViewById(R.id.tv_SD_ScheduleEmpty);
        tv_noMeetingCompleted = view.findViewById(R.id.tv_SD_NoMeetingsCompleted);
        tv_noTaskFound = view.findViewById(R.id.tv_SD_NoTask);
        ib_nextTask = view.findViewById(R.id.ib_NextTask);
        ib_prevTask = view.findViewById(R.id.ib_PrevTask);
        btn_scanQR = view.findViewById(R.id.btn_ScanQRAttendance);
        btn_details = view.findViewById(R.id.ib_SD_ShowDetails);
        ll_Task = view.findViewById(R.id.ll_SD_TaskToDo);
        fcv_upcomingSchedule = view.findViewById(R.id.fcv_UpcomingClass);
        pb_progressThisWeek = view.findViewById(R.id.pb_WeeksProgress);
        vp2_listOfTasks = view.findViewById(R.id.vp2_TasksContainer);


        StudentScreen activity = (StudentScreen) getActivity();
        assert activity != null;
        user = activity.getStudent();
        Log.d("StudentDashboard(OnCreate)", "Student Retrieved:"+user);

        studentTasks = user.getUncompletedTask();

        rv_coursesCompletedThisWeek.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //Course course = Await.get(schedule::getScheduleOfCourse);
        ScheduleAdapter sa = new ScheduleAdapter(schedulesCompleted, coursesForScheduleAdapter);
        rv_coursesCompletedThisWeek.setAdapter(sa);
        boolean empty = !Tool.boolOf(schedulesCompleted) || schedulesCompleted.isEmpty();
        Tool.handleEmpty(empty , rv_coursesCompletedThisWeek, tv_noMeetingCompleted);


        double progress = user.getPercentageCompleted();
        pb_progressThisWeek.setProgress((int)Math.ceil(progress));

        if(studentTasks != null){
            Tool.handleEmpty(studentTasks.isEmpty(), ll_Task, tv_noTaskFound);
            adapter = new TaskPagerAdapter(getActivity(), studentTasks);
            vp2_listOfTasks.setAdapter(adapter);
        }


        ib_nextTask.setOnClickListener(view12 -> {
            int currentItem = vp2_listOfTasks.getCurrentItem();
            if(currentItem < adapter.getItemCount()){
                vp2_listOfTasks.setCurrentItem(currentItem + 1, true);
            }
        });
        ib_prevTask.setOnClickListener(view13 -> {
            int currentItem = vp2_listOfTasks.getCurrentItem();
            if(currentItem > 0){
                vp2_listOfTasks.setCurrentItem(currentItem - 1, true);
            }
        });



        Tool.handleEmpty(nextMeeting == null, fcv_upcomingSchedule, tv_noScheduleFound);
        if(nextMeeting != null){
            Log.d("studentDashboard", "NextMeeting != null");

            MeetingDisplay meetingDisplay = new MeetingDisplay();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Student.SERIALIZE_KEY_CODE, user);
            meetingDisplay.setArguments(bundle);

            FragmentManager fragmentManager = getChildFragmentManager();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fcv_UpcomingClass, meetingDisplay);
            fragmentTransaction.commit();
            CourseDetails courseDetails = new CourseDetails();
            courseDetails.tv_room.setText(roomId);
            courseDetails.tv_duration.setText(scheduleOfNextMeeting.getDuration());
            courseDetails.tv_teacherName.setText(teacher.getFullName());
            courseDetails.tv_dayOfWeek.setText(course.getDaysOfSchedule(user));
            LocalDate closestMeeting = user.getClosestMeetingOfCourse(course);
            if(closestMeeting!= null){
                courseDetails.tv_nextMeetingDate.setText(closestMeeting.toString());
            }else{
                courseDetails.tv_nextMeetingDate.setText("No Meeting Found!");
            }
            Glide.with(requireContext()).load(course.getLogo()).into(courseDetails.iv_courseLogo);

            if(savedInstanceState != null){
                Tool.loadFragment(requireActivity(),fcv_upcomingSchedule.getId(), meetingDisplay);
            }

            expanded = false;

            btn_details.setOnClickListener(view1 -> {
                if(!expanded){
                    expanded = true;
                    if(savedInstanceState != null){
                        Tool.loadFragment(requireActivity(),fcv_upcomingSchedule.getId(), courseDetails);
                    }
                    btn_details.setImageResource(R.drawable.caret);
                }else{
                    expanded = false;
                    if(savedInstanceState != null){
                        Tool.loadFragment(requireActivity(),fcv_upcomingSchedule.getId(), meetingDisplay);
                    }
                    btn_details.setImageResource(R.drawable.caret_down);
                }
            });
            btn_scanQR.setOnClickListener(this::attendMeetingScan);

//        courseDetails.iv_courseLogo.setImageBitmap(scheduleOfCourse.getLogo());

        }

        return view;
    }
    public void attendMeetingScan(View view){
        QRCmanager.scanCode(barLauncher);
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
            AtomicReference<Schedule> schedule = new AtomicReference<>();
            Tool.run(requireActivity(), "Attending Meeting...",
                    ()->{
                        currentMeeting = Await.get(cb -> Meeting.getMeetingFromDB(result.getContents(),cb));
                        schedule.set(Await.get(currentMeeting::getMeetingOfSchedule));
                    },
                    ()->{
                        user.attendMeeting(currentMeeting);
                        int hour = schedule.get().getMeetingEnd().getHour();
                        int minute = schedule.get().getMeetingEnd().getMinute();
                        TimeChecker.addTimer(hour, minute, currentMeeting);
                        Toast.makeText(getContext(), "Successful!", Toast.LENGTH_SHORT).show();
                    },
                    e->{

                    });

        }
    });

    @Override
    public Bundle loadDataInBackground(Bundle input, DataLoading.ProgressCallback loadingLog) {
        Bundle bundle = new Bundle();
        Meeting nextMeeting;
        Student user;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            nextMeeting = input.getSerializable("nextMeeting", Meeting.class);
            user = input.getSerializable("student", Student.class);
        }else{
            nextMeeting = (Meeting) input.getSerializable("nextMeeting");
            user = (Student) input.getSerializable("student");
        }

        Schedule schedule = null;
        Course course = null;
        Teacher teacher = null;
        String roomId = "";
        if(Tool.boolOf(nextMeeting)){
            loadingLog.onProgress("Loading Meeting of Schedule...");
            schedule = Await.get(nextMeeting::getMeetingOfSchedule);
            course = Await.get(schedule::getScheduleOfCourse);
            teacher = Await.get(course::getTeacher);
            roomId = Await.get(schedule::getRoom).getID();
        }


        ArrayList<Schedule> completedSchedules = user.getScheduleCompletedThisWeek();
        ArrayList<Course> coursesOfSchedule = completedSchedules.stream().map(s -> Await.get(s::getScheduleOfCourse)).collect(Collectors.toCollection(ArrayList::new));

        bundle.putSerializable(Schedule.SERIALIZE_KEY_CODE, schedule);
        bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
        bundle.putSerializable(Teacher.SERIALIZE_KEY_CODE, teacher);
        bundle.putSerializable("completedSchedules", completedSchedules);
        bundle.putSerializable("coursesOfSchedule", coursesOfSchedule);
        bundle.putSerializable("nextMeeting", nextMeeting);
        bundle.putString("roomId", roomId);
        return bundle;
    }

    @Override
    public void onDataLoaded(Bundle preloadedData) {
        scheduleOfNextMeeting = (Schedule)preloadedData.getSerializable(Schedule.SERIALIZE_KEY_CODE);
        course = (Course)preloadedData.getSerializable(Course.SERIALIZE_KEY_CODE);
        teacher = (Teacher)preloadedData.getSerializable(Teacher.SERIALIZE_KEY_CODE);
        coursesForScheduleAdapter = (ArrayList<Course>) preloadedData.getSerializable("coursesOfSchedule");
        schedulesCompleted = (ArrayList<Schedule>)preloadedData.getSerializable("completedSchedules");
        nextMeeting = (Meeting)preloadedData.getSerializable("nextMeeting");
        roomId = preloadedData.getString("roomId");
    }

    @Override
    public void onLoadingError(Exception error) {

    }
}
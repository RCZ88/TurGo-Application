package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_Dashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_Dashboard extends Fragment{
    RecyclerView rv_coursesCompletedThisWeek;
    FragmentContainerView fcv_upcomingSchedule;
    StudentFirebase fbStudent;
    Student user;
    ArrayList<Task> studentTasks;
    ViewPager2 vp2_listOfTasks;
    ProgressBar pb_progressThisWeek;
    ImageButton btn_details, ib_prevTask, ib_nextTask;
    TaskPagerAdapter adapter;
    boolean expanded;

    Handler handler = new Handler();

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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint({"MissingInflatedId", "CommitTransaction"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_dashboard, container, false);
        rv_coursesCompletedThisWeek = view.findViewById(R.id.rv_CourseCompleted);

        StudentScreen activity = (StudentScreen) getActivity();
        assert activity != null;
        fbStudent = activity.getStudent();
        try {
            user = fbStudent.convertToNormal();
        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }
        Task t = new Task();
        studentTasks = user.getUncompletedTask();

        rv_coursesCompletedThisWeek.setLayoutManager(new LinearLayoutManager(this.getContext()));
        ScheduleAdapter sa = new ScheduleAdapter(user.getScheduleCompletedThisWeek());
        rv_coursesCompletedThisWeek.setAdapter(sa);

        pb_progressThisWeek = view.findViewById(R.id.pb_WeeksProgress);
        double progress = user.getPercentageCompleted();
        pb_progressThisWeek.setProgress((int)Math.ceil(progress));

        vp2_listOfTasks = view.findViewById(R.id.vp2_TasksContainer);
        adapter = new TaskPagerAdapter(getActivity(), studentTasks);
        vp2_listOfTasks.setAdapter(adapter);

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


        Meeting nextMeeting = user.getNextMeeting();
        Schedule meetingOfSchedule = nextMeeting.getMeetingOfSchedule();
        fcv_upcomingSchedule = view.findViewById(R.id.fcv_UpcomingClass);
        MeetingDisplay meetingDisplay = new MeetingDisplay();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fcv_UpcomingClass, meetingDisplay);
        fragmentTransaction.commit();

        CourseDetails courseDetails = new CourseDetails();
        Course scheduleOfCourse = meetingOfSchedule.getScheduleOfCourse();
        courseDetails.tv_teacherName.setText(scheduleOfCourse.getTeacher().getFullName());
        courseDetails.tv_room.setText((meetingOfSchedule.getRoom().getID()));
        courseDetails.tv_dayOfWeek.setText(scheduleOfCourse.getDaysOfSchedule(user));
        courseDetails.tv_nextMeetingDate.setText(user.getClosestMeetingOfCourse(scheduleOfCourse).toString());
        courseDetails.tv_duration.setText(meetingOfSchedule.getDuration());
//        courseDetails.iv_courseLogo.setImageBitmap(scheduleOfCourse.getLogo());
        Glide.with(requireContext()).load(scheduleOfCourse.getLogo()).into(courseDetails.iv_courseLogo);
        if(savedInstanceState != null){
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(fcv_upcomingSchedule.getId(), meetingDisplay)
                    .commit();
        }

        expanded = false;
        btn_details.setImageResource(R.drawable.caret_down);

        btn_details.setOnClickListener(view1 -> {
            if(!expanded){
                expanded = true;
                if(savedInstanceState != null){
                    getChildFragmentManager()
                            .beginTransaction()
                            .replace(fcv_upcomingSchedule.getId(), courseDetails);
                }
                btn_details.setImageResource(R.drawable.caret);
            }else{
                expanded = false;
                if(savedInstanceState != null){
                    getChildFragmentManager()
                            .beginTransaction()
                            .replace(fcv_upcomingSchedule.getId(), meetingDisplay)
                            .commit();
                }
                btn_details.setImageResource(R.drawable.caret_down);
            }
        });
        return view;
    }
    public void attendMeetingScan(View view){
        QRCmanager.scanCode(barLauncher);
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){


            Meeting.getMeetingFromDB(result.getContents(), new ObjectCallBack<Meeting>() {
                @Override
                public void onObjectRetrieved(Meeting object) {
                    currentMeeting = object;
                    user.attendMeeting(currentMeeting);
                    int hour = currentMeeting.getMeetingOfSchedule().getMeetingEnd().getHour();
                    int minute = currentMeeting.getMeetingOfSchedule().getMeetingEnd().getMinute();
                    TimeChecker.addTimer(hour, minute, currentMeeting);
                    Toast.makeText(getContext(), "Successful!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(DatabaseError error) {
                    Log.e("DB ERROR", "Problem: " + error);
                }
            });

        }
    });
//
//    @Override
//    public void prepareObjects() throws ParseException {
//        //Cannot resolve method 'getUncompletedTask' in 'StudentFirebase'
//        //Cannot resolve method 'getScheduleCompletedThisWeek' in 'StudentFirebase'
//        //Cannot resolve method 'getNextMeeting' in 'StudentFirebase'
//        //'getDaysOfSchedule(com.example.turgo.Student)' in 'com.example.turgo.Course' cannot be applied to '(com.example.turgo.StudentFirebase)'
//        //Cannot resolve method 'getClosestMeetingOfCourse' in 'StudentFirebase'
//        //Cannot resolve method 'attendMeeting' in 'StudentFirebase'
//
//        Task t = new Task();
//        Schedule s = new Schedule();
//        Meeting m = new Meeting();
//
//        final ArrayList<TaskFirebase>[] uncompletedTask = new ArrayList[]{new ArrayList<>()};
//        t.retrieveListFromUser(user.getID(), "uncompletedTaskIds", new ObjectCallBack<ArrayList<TaskFirebase>>() {
//            @Override
//            public void onObjectRetrieved(ArrayList<TaskFirebase> object) {
//                uncompletedTask[0] = object;
//            }
//
//            @Override
//            public void onError(DatabaseError error) {
//
//            }
//        });
//        this.uncompletedTask = uncompletedTask[0];
//
//        final ArrayList<ScheduleFirebase>[] scheduleCompletedThisWeek = new ArrayList[]{new ArrayList<>()};
//        s.retrieveListFromUser(user.getID(), "scheduleCompletedThisWeek", new ObjectCallBack<ArrayList<ScheduleFirebase>>() {
//            @Override
//            public void onObjectRetrieved(ArrayList<ScheduleFirebase> object) {
//                scheduleCompletedThisWeek[0] = object;
//            }
//
//            @Override
//            public void onError(DatabaseError error) {
//
//            }
//        });
//
//        final MeetingFirebase[] nextMeeting = {null};
//        m.retrieveOnce(new ObjectCallBack<MeetingFirebase>() {
//            @Override
//            public void onObjectRetrieved(MeetingFirebase object) {
//                nextMeeting[0] = object;
//            }
//
//            @Override
//            public void onError(DatabaseError error) {
//
//            }
//        }, user.getID());
//        Student student = this.user.convertToNormal();
//
//    }
}
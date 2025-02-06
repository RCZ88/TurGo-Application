package com.example.turgo;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_Dashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_Dashboard extends Fragment {
    RecyclerView rv_coursesCompletedThisWeek;
    FragmentContainerView fcv_upcomingSchedule;
    Student user;
    ProgressBar pb_progressThisWeek;
    ImageButton btn_details;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_dashboard, container, false);
        rv_coursesCompletedThisWeek = view.findViewById(R.id.rv_CourseCompleted);

        StudentScreen activity = (StudentScreen) getActivity();
        this.user = activity.getStudent();
        rv_coursesCompletedThisWeek.setLayoutManager(new LinearLayoutManager(this.getContext()));
        ScheduleAdapter sa = new ScheduleAdapter(user.getScheduleCompletedThisWeek());
        rv_coursesCompletedThisWeek.setAdapter(sa);

        pb_progressThisWeek = view.findViewById(R.id.pb_WeeksProgress);
        new Thread(()->{
            double progress = user.getPercentageCompleted();
            handler.post(() -> pb_progressThisWeek.setProgress((int)Math.ceil(progress)));
            try {
                Thread.sleep(500); // Simulate a task
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        fcv_upcomingSchedule = view.findViewById(R.id.fcv_UpcomingClass);
        CourseName courseName = new CourseName();

        Meeting nextMeeting = user.getNextMeeting();
        Schedule meetingOfSchedule = nextMeeting.getMeetingOfSchedule();

        courseName.tv_startTime.setText(nextMeeting.getStartTimeChange().toString());
        courseName.tv_courseName.setText(meetingOfSchedule.getScheduleOfCourse().getCourseName());
        courseName.iv_courseIcon.setImageBitmap(meetingOfSchedule.getScheduleOfCourse().getLogo());


        CourseDetails courseDetails = new CourseDetails();
        courseDetails.tv_teacherName.setText(meetingOfSchedule.getScheduleOfCourse().getTeacher().getFullName());
        courseDetails.tv_room.setText((meetingOfSchedule.getRoom().getRoomId()));
        courseDetails.tv_dayOfWeek.setText(meetingOfSchedule.getScheduleOfCourse().getDaysOfSchedule());
        courseDetails.tv_nextMeetingDate.setText(user.getClosestMeetingOfCourse(meetingOfSchedule.getScheduleOfCourse()).toString());
        courseDetails.tv_duration.setText(meetingOfSchedule.getDuration());
        courseDetails.iv_courseLogo.setImageBitmap(meetingOfSchedule.getScheduleOfCourse().getLogo());
        if(savedInstanceState != null){
            getFragmentManager()
                    .beginTransaction()
                    .replace(fcv_upcomingSchedule.getId(), courseName)
                    .commit();
        }
        expanded = false;
        btn_details.setImageResource(R.drawable.caret_down);

        btn_details.setOnClickListener(view1 -> {
            if(!expanded){
                expanded = true;
                if(savedInstanceState != null){
                    getFragmentManager()
                            .beginTransaction()
                            .replace(fcv_upcomingSchedule.getId(), courseDetails);
                }
                btn_details.setImageResource(R.drawable.caret);
            }else{
                expanded = false;
                if(savedInstanceState != null){
                    getFragmentManager()
                            .beginTransaction()
                            .replace(fcv_upcomingSchedule.getId(), courseName)
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
                public void onObjectRetrieved(Meeting Object) {
                    currentMeeting = Object;
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
}
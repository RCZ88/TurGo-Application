package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseJoinedFullPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseJoinedFullPage extends Fragment implements RequiresDataLoading{
    Course course;
    Student student;
    TextView tv_courseTeacher, tv_courseDays, tv_nextMeetingDate, tv_noTaskMessage, tv_courseTitle;
    ImageView iv_courseBackgroundImage;
    RecyclerView rv_listOfTasks, rv_latestAgenda;
    Button btn_viewAllAgenda, btn_viewAllMeeting;
    TaskAdapter taskAdapter;
    ArrayList<Task> tasks = new ArrayList<>();
    Teacher teacher;

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
            onDataLoaded(getArguments());
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

        tasks = student.getAllTaskOfCourse(course);

        tv_courseTeacher = view.findViewById(R.id.tv_cfp_TeacherName);
        tv_courseDays = view.findViewById(R.id.tv_cfp_ScheduleDays);
        tv_nextMeetingDate = view.findViewById(R.id.tv_cfp_NextMeeting);
        tv_noTaskMessage = view.findViewById(R.id.noTasksMessage);
        tv_courseTitle = view.findViewById(R.id.tv_cfp_CourseTitle);
        iv_courseBackgroundImage = view.findViewById(R.id.iv_ecfp_courseWallpaper);

        rv_listOfTasks = view.findViewById(R.id.rv_cfp_ListOfTasks);
        rv_latestAgenda = view.findViewById(R.id.rv_cfp_ListOFAgenda);
        btn_viewAllAgenda = view.findViewById(R.id.btn_ViewAllAgendaOfCourse);
        tv_courseTitle.setText(course.getCourseName());
        //async - completed
        tv_courseTeacher.setText(teacher.getFullName());
        tv_courseDays.setText(course.getDaysOfSchedule(student));
        tv_noTaskMessage.setText("Yay you're Free! No Task Found!");

//        iv_courseBackgroundImage.setImageBitmap(course.getBackground());
        Glide.with(requireContext()).load(course.getBackground()).into(iv_courseBackgroundImage);

        loadTasks();
        //async - completed
        Tool.run(requireActivity(), "Retrieving the Latest Agenda..",
                ()->{
                    Await.get(this::setLatestAgenda);
                },
                ()->{
                    btn_viewAllAgenda.setOnClickListener(view1 -> {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        AtomicReference<ArrayList<Agenda>> agendas = new AtomicReference<>();
                        //async - completed
                        Tool.run(requireActivity(), "Loading Agendas...",
                                ()->{
                                    agendas.set(Await.get(cb -> student.getAgendaOfCourse(course, cb)));
                                },
                                ()->{
                                    ft.replace(R.id.nhf_ss_FragContainer, new AllAgendaPage(agendas.get()));
                                    ft.addToBackStack(null);
                                    ft.commit();
                                },
                                e->{

                                });
                    });
                    btn_viewAllMeeting.setOnClickListener(view12 -> {
                        StudentMeetings studentMeetings = new StudentMeetings();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
                        bundle.putSerializable(Student.SERIALIZE_KEY_CODE, student);
                        studentMeetings.setArguments(bundle);

                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.nhf_ss_FragContainer, studentMeetings)
                                .addToBackStack(null)
                                .commit();
                    });
                },
                e->{

                });

        return view;
    }
    private void setLatestAgenda(ObjectCallBack<Void>cb){
        ArrayList<Agenda>theAgenda = new ArrayList<>();
        student.getLatestAgendaOfCourse(course, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Agenda object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, java.lang.InstantiationException {
                theAgenda.add(object);
                AgendaAdapter agendaAdapter = new AgendaAdapter(theAgenda);
                rv_latestAgenda.setAdapter(agendaAdapter);
                cb.onObjectRetrieved(null);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });

    }
    private void loadTasks() {
        tasks = student.getAllTaskOfCourse(course); // Assume this method fetches tasks
        if (tasks.isEmpty()) {
            tv_noTaskMessage.setVisibility(View.VISIBLE);
            rv_listOfTasks.setVisibility(View.GONE);
        } else {
            tv_noTaskMessage.setVisibility(View.GONE);
            rv_listOfTasks.setVisibility(View.VISIBLE);
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
                            .replace(R.id.nhf_ss_FragContainer, tfp)
                            .addToBackStack(null)
                            .commit();
                }

                @Override
                public void onItemLongClick(Task item) {

                }
            });
            rv_listOfTasks.setAdapter(taskAdapter);
        }
    }

    @Override
    public Bundle loadDataInBackground(Bundle input, DataLoading.ProgressCallback callback) {
        Bundle output = new Bundle();
        Course course = (Course)input.getSerializable(Course.SERIALIZE_KEY_CODE);
        Teacher teacher = Await.get(course::getTeacher);

        output.putSerializable(Teacher.SERIALIZE_KEY_CODE, teacher);
        output.putSerializable(Course.SERIALIZE_KEY_CODE, course);

        return output;
    }

    @Override
    public void onDataLoaded(Bundle preloadedData) {
        course = (Course)preloadedData.getSerializable(Course.SERIALIZE_KEY_CODE);
        teacher = (Teacher)preloadedData.getSerializable(Teacher.SERIALIZE_KEY_CODE);
    }

    @Override
    public void onLoadingError(Exception error) {

    }
}
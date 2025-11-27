package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentMeetings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentMeetings extends Fragment {
    RecyclerView rv_futureMeeting, rv_pastMeetings;
    Course course;
    Student student;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StudentMeetings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudentMeetings.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentMeetings newInstance(String param1, String param2) {
        StudentMeetings fragment = new StudentMeetings();
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
        View view = inflater.inflate(R.layout.fragment_student_meetings, container, false);
        assert getArguments() != null;
        this.course = (Course) getArguments().getSerializable(Course.SERIALIZE_KEY_CODE);
        StudentScreen studentScreen = (StudentScreen) getActivity();
        assert studentScreen != null;
        try {
            studentScreen.getStudent().convertToNormal(new ObjectCallBack<Student>() {
                @Override
                public void onObjectRetrieved(Student object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, java.lang.InstantiationException {
                    student = object;
                }

                @Override
                public void onError(DatabaseError error) {

                }
            });
        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }
        MeetingAdapter pastMeeting = new MeetingAdapter(student.getAllMeetingOfCourse(course));
        rv_pastMeetings.setAdapter(pastMeeting);
        MeetingAdapter futureMeeting = new MeetingAdapter(student.getAllMeetingOfCourse(course));
        rv_futureMeeting.setAdapter(futureMeeting);

        return view;
    }
}
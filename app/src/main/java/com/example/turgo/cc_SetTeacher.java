package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_SetTeacher#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_SetTeacher extends Fragment {
    SearchView sv_searchTeacher;
    RecyclerView rv_teachersFound, rv_teacherSelected;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public cc_SetTeacher() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment cc_teacher_schedule.
     */
    // TODO: Rename and change types and number of parameters
    public static cc_SetTeacher newInstance(String param1, String param2) {
        cc_SetTeacher fragment = new cc_SetTeacher();
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
        View view = inflater.inflate(R.layout.fragment_cc_teacher, container, false);
        sv_searchTeacher = view.findViewById(R.id.sv_CC_SearchTeacher);
        rv_teachersFound = view.findViewById(R.id.rv_CC_SelectTeacher);
        rv_teacherSelected = view.findViewById(R.id.rv_CC_TeacherSelected);

        TeacherFirebase teacherFirebase = new TeacherFirebase();
        ArrayList<TeacherFirebase> tf = (ArrayList<TeacherFirebase>) teacherFirebase.getAllObject(FirebaseNode.TEACHER);
        ArrayList<TeacherMini>tm = new ArrayList<>();
        for(TeacherFirebase tff: tf){
            tm.add(tff.toTM());
        }
        TeacherAdapter taa = new TeacherAdapter(new ArrayList<>(), getContext(), null);
        @SuppressLint("NotifyDataSetChanged") TeacherAdapter ta = new TeacherAdapter(tm, getContext(), teacherMini -> {
            taa.teacherSearchResult.clear();
            taa.teacherSearchResult.add(teacherMini);
            taa.notifyDataSetChanged();
            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference(FirebaseNode.TEACHER.getPath()).child(teacherMini.getRealTeacherID());
            CreateCourse cc = (CreateCourse) requireActivity();
            dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    TeacherFirebase tfb = (TeacherFirebase) (snapshot.getValue());
                    try {
                        assert tfb != null;
                        cc.teacher = (Teacher) tfb.constructClass(Teacher.class, teacherMini.getRealTeacherID());
                    } catch (NoSuchMethodException | InvocationTargetException |
                             IllegalAccessException | java.lang.InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        rv_teachersFound.setAdapter(ta);

        sv_searchTeacher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                ta.filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ta.filter(s);
                return false;
            }
        });


        return view;
    }
}
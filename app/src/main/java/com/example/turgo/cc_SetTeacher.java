package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_SetTeacher#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_SetTeacher extends Fragment implements checkFragmentCompletion{
    SearchView sv_searchTeacher;
    RecyclerView rv_teachersFound, rv_teacherSelected;
    TextView tv_teachersNotAvailable, tv_teacherNotSelected;
    TeacherAdapter teacherSearchAdapter, teacherSelectedAdapter;
    private static final String tag = "cc_SetTeacher";
    ArrayList<TeacherFirebase>teachers = new ArrayList<>();
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
        tv_teachersNotAvailable = view.findViewById(R.id.tv_CC_NoTeacherAvailable);
        tv_teacherNotSelected = view.findViewById(R.id.tv_CC_NoTeacherSelected);



        TeacherFirebase teacherFirebase = new TeacherFirebase();
        teacherFirebase.getAllObject(FirebaseNode.TEACHER, TeacherFirebase.class, new ObjectCallBack<ArrayList<TeacherFirebase>>() {
            @Override
            public void onObjectRetrieved(ArrayList<TeacherFirebase> object) {
                ArrayList<TeacherFirebase> tf  = object;
                teachers = tf;
                Log.d(tag, tf.size() + " Teacher Collected!");
                for(TeacherFirebase teacher : tf){
                    Log.d(tag, teacher.toString());
                }
                initializeUI(tf);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });

        return view;
    }
    private void initializeUI(ArrayList<TeacherFirebase>tf){
        Log.d(tag, "Total teachers fetched: " + tf.size());
        ArrayList<TeacherMini>tm = new ArrayList<>();
        for(TeacherFirebase tff: tf){
            TeacherMini teacherMini = tff.toTM();
            tm.add(teacherMini);
            Log.d(tag, "Teacher Added -> " + teacherMini);
        }
        teacherSelectedAdapter = new TeacherAdapter(new ArrayList<>(), getContext(), null, false);
        rv_teacherSelected.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_teacherSelected.setAdapter(teacherSelectedAdapter);

        teacherSearchAdapter = new TeacherAdapter(tm, getContext(), new OnItemClickListener<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(Pair<Integer, TeacherMini> pair) {
                Log.d(tag, "Pair: " + pair);
                TeacherMini teacherMini = pair.two;
                int arrIndex = pair.one;
                Log.d(tag, "Teacher Mini OBJ Selected: " + teacherMini);
                teacherSelectedAdapter.teacherSearchResult.clear();
                teacherSelectedAdapter.teacherSearchResult.add(teacherMini);
                teacherSelectedAdapter.notifyDataSetChanged();
                showRV();
                int recyclerSize = rv_teachersFound.getChildCount();
                Log.d("cc_setTeacher", "Recycler Size:" + recyclerSize);
                Log.d(tag, "Teacher Search Result Size: " +teacherSelectedAdapter.teacherSearchResult.size());
                CreateCourse cc = (CreateCourse) requireActivity();
                TeacherFirebase selectedTeacher = teachers.get(arrIndex);

                Log.d(tag, "Converting TeacherFirebase to Normal");
                try {
                    selectedTeacher.convertToNormal(new ObjectCallBack<>() {
                        @Override
                        public void onObjectRetrieved(Teacher object) {
                            cc.teacher = object;
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    });
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | java.lang.InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onItemLongClick(Pair<Integer, TeacherMini> pair) {

            }
        }, true);
        rv_teachersFound.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_teachersFound.setAdapter(teacherSearchAdapter);

        Tool.handleEmpty(teacherSearchAdapter.teacherSearchResult.isEmpty(), rv_teachersFound, tv_teachersNotAvailable);
        Tool.handleEmpty(teacherSelectedAdapter.teacherSearchResult.isEmpty(), rv_teacherSelected, tv_teacherNotSelected);

        sv_searchTeacher.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                teacherSearchAdapter.filter(s);
                Tool.handleEmpty(teacherSearchAdapter.teacherSearchResult.isEmpty(), rv_teachersFound, tv_teachersNotAvailable);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                teacherSearchAdapter.filter(s);
                Tool.handleEmpty(teacherSearchAdapter.teacherSearchResult.isEmpty(), rv_teachersFound, tv_teachersNotAvailable);
                return false;
            }
        });

    }

    private void showRV() {
        Tool.handleEmpty(teacherSelectedAdapter.teacherSearchResult.isEmpty(), rv_teacherSelected, tv_teacherNotSelected);
    }

    @Override
    public boolean checkIfCompleted() {
        if(rv_teacherSelected.getChildCount() == 0){
            Toast.makeText(requireContext(), "Please Select a Teacher!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
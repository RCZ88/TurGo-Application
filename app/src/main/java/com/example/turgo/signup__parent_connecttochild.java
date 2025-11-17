package com.example.turgo;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup__parent_connecttochild#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup__parent_connecttochild extends Fragment implements checkFragmentCompletion{

    SignUpPage signUpPage;
    EditText et_childIdManual;
    Button btn_searchChild, btn_ScanQR, btn_addChild;
    TextView tv_childFound;
    Student childFound;
    ArrayList<String> childSelected;
    ArrayList<Student> childSelectedStudent;
    ArrayAdapter<String>listAdapter;
    ListView lv_childSelected;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public signup__parent_connecttochild() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signup__parent_connecttochild.
     */
    // TODO: Rename and change types and number of parameters
    public static signup__parent_connecttochild newInstance(String param1, String param2) {
        signup__parent_connecttochild fragment = new signup__parent_connecttochild();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        childSelected = new ArrayList<>();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup__parent_connecttochild, container, false);
        childSelectedStudent = new ArrayList<>();
        et_childIdManual = view.findViewById(R.id.tie_searchChildId);
        btn_searchChild = view.findViewById(R.id.btn_SearchIDManual);
        btn_ScanQR = view.findViewById(R.id.btn_ScanQRCODE);
        btn_addChild = view.findViewById(R.id.btn_AddChild);
        lv_childSelected = view.findViewById(R.id.lv_ChildAccountSelected);
        tv_childFound = view.findViewById(R.id.tv_SearchIdResult);
        btn_searchChild.setOnClickListener(view1 -> searchChildByID());
        btn_addChild.setOnClickListener(view12 -> {
            boolean condition1 = childFound != null;
            boolean condition2 = !(childSelectedStudent.contains(childFound));
            if(condition1 && condition2){
                childSelected.add(childFound.getFullName());
                childSelectedStudent.add(childFound);
                Log.d("Child Add", "Child Added");
                showOnAdapter();
            }else{
                Log.e("Add Child Error", "Child Found: " + condition1 + "\nChild Already Exist: " + condition2);
                Log.d("Child selected", Arrays.toString(childSelectedStudent.toArray()));
            }

        });
        btn_ScanQR.setOnClickListener(view13 -> QRCmanager.scanCode(barLauncher));
        return view;
    }
    public void scanQR(View view){

    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
            et_childIdManual.setText(result.getContents());
            Toast.makeText(getContext(), "Scan Successfully Done!", Toast.LENGTH_SHORT).show();
            /*AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();*/
        }
    });
    public void searchChildByID(){
        String uid = et_childIdManual.getText().toString();
        Log.d("UID INPUTTED: ", uid);
        if(!uid.isBlank()){
            User.getUserDataFromDB(uid, new ObjectCallBack<User>() {
                @Override
                public void onObjectRetrieved(User object) {
                    childFound = (Student) object;
                    tv_childFound.setText(object.getFullName());
                }

                @Override
                public void onError(DatabaseError error) {
                    Log.e("User Retrieve Error", "Failed to retrieve user with UID: " + uid + " Error: " +error);
                }
            });

        }
    }
    private void showOnAdapter(){
        listAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, childSelected);
        lv_childSelected.setAdapter(listAdapter);
    }

    @Override
    public boolean checkIfCompleted() {
        return !tv_childFound.getText().toString().isEmpty();
    }
}
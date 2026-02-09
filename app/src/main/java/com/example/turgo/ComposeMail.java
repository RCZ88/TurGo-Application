package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ComposeMail extends AppCompatActivity {
    EditText et_subject, et_body;
    AutoCompleteTextView actv_to;
    Button btn_addAttachment, btn_send, btn_draft;
    User user;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actv_to = findViewById(R.id.actv_CM_To);
        et_subject = findViewById(R.id.et_CM_Subject);
        et_body = findViewById(R.id.etml_CM_Body);
        btn_addAttachment = findViewById(R.id.btn_CM_AddAttachment);
        btn_send = findViewById(R.id.btn_CM_Send);
        btn_draft = findViewById(R.id.btn_CM_Draft);

        Intent intent = getIntent();
        if(intent.getSerializableExtra(User.SERIALIZE_KEY_CODE) != null){
            user = (User) intent.getSerializableExtra(User.SERIALIZE_KEY_CODE);
        }

        ArrayList<User>users = getAllUsers();


        UserAdapter adapter = new UserAdapter(this, users);
        actv_to.setAdapter(adapter);

        actv_to.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() >= 1){
                    queryUserByUsername(s.toString(), users ->{
                        adapter.clear();
                        adapter.addAll(users);
                    });
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final User[] userTo = new User[1];
        actv_to.setOnItemClickListener((parent, view, position, id) -> userTo[0] = (User) parent.getItemAtPosition(position));
        
        ActivityResultLauncher<Intent> filePickerLauncher;
        ArrayList<Uri> filesUploaded = new ArrayList<>();
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
            if(result.getResultCode() == RESULT_OK && result.getData()!= null){
                Intent data = result.getData();
                if(data.getClipData() != null){
                    int count = data.getClipData().getItemCount();
                    for(int i =0; i<count; i++){
                        Uri uriFile = data.getClipData().getItemAt(i).getUri();
                        filesUploaded.add(uriFile);
                    }
                }else if(data.getData() != null){
                    Uri uriFile = data.getData();
                    filesUploaded.add(uriFile);
                }
            }
        });


        btn_send.setOnClickListener(v-> {
            String subject = et_subject.getText().toString();
            String body = et_body.getText().toString();
            ArrayList<file> files = new ArrayList<>();
            for(Uri uri : filesUploaded){
                final String[] path = new String[1];
                try {
                    Tool.uploadToCloudinary(Tool.uriToFile(uri, this), new ObjectCallBack<String>() {
                        @Override
                        public void onObjectRetrieved(String object) {
                            path[0] = object;
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                files.add(new file(Tool.getFileName(this, uri), path[0], user, LocalDateTime.now()));
            }
            for(file file : files){
                try {
                    file.updateDB();
                } catch (NoSuchMethodException | InvocationTargetException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
            Mail mail = new Mail(user, userTo[0], subject, body);

            try {
                mail.setDraft(false);
                User.sendMail(mail);
                mail.updateDB();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                     InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
        btn_draft.setOnClickListener(v-> {
            String subject = et_subject.getText().toString();
            String body = et_body.getText().toString();
            ArrayList<file>attachments = new ArrayList<>();
            for(Uri file : filesUploaded){
                try {
                    final String[] cloudinaryUrl = {""};
                    Tool.uploadToCloudinary(Tool.uriToFile(file, this), new ObjectCallBack<String>() {
                        @Override
                        public void onObjectRetrieved(String object) {
                            cloudinaryUrl[0] = object;
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    });
                    file theFile = new file(Tool.getFileName(this, file), cloudinaryUrl[0], user, LocalDateTime.now());
                    attachments.add(theFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Mail mail = new Mail(user, userTo[0], subject, body);
            try{
                if(!mail.isDraft()){
                    mail.setDraft(true);
                    user.addDraftMail(mail);
                }
                mail.updateDB();
            }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                    InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compose_mail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void queryUserByUsername(String prefix, OnUsersResultListener listener){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(FirebaseNode.USER.getPath());
        String[]userTypes = {"student", "teacher", "parent", "admin"};

        AtomicInteger completedQueries = new AtomicInteger(0);
        int totalQueries = userTypes.length;
        for(String userType : userTypes){
            ref = ref.child(userType);
            Query query = ref.orderByChild("fullName")
                    .startAt(prefix.toLowerCase())
                    .endAt(prefix.toLowerCase() + "\uf8ff")
                    .limitToFirst(10);
            query.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<User>matchedUsers = new ArrayList<>();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(dataSnapshot.getValue() instanceof TeacherFirebase || dataSnapshot.getValue() instanceof StudentFirebase){
                            User user = (User) dataSnapshot.getValue();
                            if(user!= null){
                                matchedUsers.add(user);
                            }
                        }

                    }
                    listener.onUsersResult(matchedUsers);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
    }
    private void handleExistingDrafts(){
        Intent intent = getIntent();
        if(intent.hasExtra(Mail.SERIALIZE_KEY_CODE)){
            Mail mail = (Mail) intent.getSerializableExtra(Mail.SERIALIZE_KEY_CODE);
            et_subject.setText(mail.getHeader());
            et_body.setText(mail.getBody());
            actv_to.setText(mail.getTo().getFullName());

        }
    }
    private ArrayList<User> getAllUsers(){
        String[]userTypes = {"student", "teacher", "parent", "admin"};
        ArrayList<User>users = new ArrayList<>();
        for(String userType : userTypes){
            DatabaseReference dbf = FirebaseDatabase.getInstance().getReference(FirebaseNode.USER.getPath()).child(userType);
            dbf.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if(dataSnapshot.getValue() instanceof TeacherFirebase || dataSnapshot.getValue() instanceof StudentFirebase){
                            User user = (User) dataSnapshot.getValue();
                            if(user!= null){
                                users.add(user);
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return users;
    }

}
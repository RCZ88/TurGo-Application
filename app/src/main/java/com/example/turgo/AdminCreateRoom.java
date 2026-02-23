package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityAdminCreateRoomBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminCreateRoom extends AppCompatActivity {
    private ActivityAdminCreateRoomBinding binding;
    EditText et_roomTag;
    SeekBar sb_roomCapacity;
    Button btn_createRoom;
    ImageButton ib_goBack;
    RecyclerView rv_roomsCreated;
    TextView tv_emptyRoom, tv_capacitySelected;
    ChipGroup cg_courseTypes;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_create_room);


        et_roomTag = findViewById(R.id.et_ACR_roomTag);
        sb_roomCapacity = findViewById(R.id.sb_ACR_roomCapacity);
        btn_createRoom = findViewById(R.id.btn_ACR_create);
        rv_roomsCreated = findViewById(R.id.rv_ACR_roomsCreated);
        cg_courseTypes = findViewById(R.id.cg_ACR_courseTypes);
        tv_emptyRoom = findViewById(R.id.tv_ACR_emptyRoom);
        tv_capacitySelected = findViewById(R.id.tv_ACR_studentCapacityCount);
        ib_goBack = findViewById(R.id.ib_ACR_goBack);


        rv_roomsCreated.setLayoutManager(new LinearLayoutManager(this));

        getCourseTypes().addOnSuccessListener(this::setupCourseTypes);

        RoomAdapter roomAdapter = new RoomAdapter(this);
        getExistingRooms().addOnSuccessListener(rooms ->{
            for(Room room : rooms){
                Log.d("AdminCreateRoom", room.toString());
            }
            roomAdapter.setRooms(rooms);
            rv_roomsCreated.setAdapter(roomAdapter);
            Tool.handleEmpty(roomAdapter.getItemCount() ==0, rv_roomsCreated, tv_emptyRoom);
        });

        sb_roomCapacity.setMax(8);
        sb_roomCapacity.setMin(0);
        ib_goBack.setOnClickListener(v-> Tool.doBackPress(this));

        sb_roomCapacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_capacitySelected.setText(progress + " Students");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btn_createRoom.setOnClickListener(v -> {
            if(checkComplete()){
                roomAdapter.addRoom(createRoom());
                Log.d("AdminCreateRoom", "Room Created Successfully!");
                Toast.makeText(this, "Room created Successfully!", Toast.LENGTH_SHORT).show();
            }else{
                Log.d("AdminCreateRoom", "Please Complete all the fields First!");
                Toast.makeText(this, "Please Complete all the fields First!", Toast.LENGTH_SHORT).show();
            }
        });
        binding = ActivityAdminCreateRoomBinding.inflate(getLayoutInflater());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private Task<List<CourseType>> getCourseTypes(){
        TaskCompletionSource<List<CourseType>> taskSource = new TaskCompletionSource<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.COURSE_TYPE.getPath());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Task<CourseType>> taskCourseType = new ArrayList<>();
                for (DataSnapshot ds :  snapshot.getChildren()){
                    String courseTypeId = ds.getKey();
                    CourseTypeRepository ctr = new CourseTypeRepository(courseTypeId);
                    taskCourseType.add(ctr.loadAsNormal());
                }
                Tasks.whenAllSuccess(taskCourseType)
                        .addOnSuccessListener(results -> {
                            ArrayList<CourseType> courseTypes = new ArrayList<>();
                            for (Object result : results) {
                                if (result instanceof CourseType) {
                                    courseTypes.add((CourseType) result);
                                }
                            }
                            taskSource.setResult(courseTypes);
                        })
                        .addOnFailureListener(taskSource::setException);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                taskSource.setException(error.toException());
            }
        });
        return taskSource.getTask();
    }
    private Task<List<Room>> getExistingRooms(){
        TaskCompletionSource<List<Room>> taskSource = new TaskCompletionSource<>();
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference(FirebaseNode.ROOM.getPath());
        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Task<Room>>taskList = new ArrayList<>();
                for(DataSnapshot ds : snapshot.getChildren()){
                    RoomRepository roomRepository = new RoomRepository(ds.getKey());
                    taskList.add(roomRepository.loadAsNormal());
                }
                Tasks.whenAllSuccess(taskList)
                        .addOnSuccessListener(results -> {
                            ArrayList<Room> rooms = new ArrayList<>();
                            for (Object result : results) {
                                if (result instanceof Room) {
                                    rooms.add((Room) result);
                                }
                            }
                            taskSource.setResult(rooms);
                        })
                        .addOnFailureListener(taskSource::setException);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                taskSource.setException(error.toException());
            }
        });
        return taskSource.getTask();
    }
    private Room createRoom(){
        ArrayList<CourseType>selectedCourseType = getSelectedCourseObjects();
        Room room = new Room(et_roomTag.getText().toString(), sb_roomCapacity.getProgress(), selectedCourseType);
        RoomRepository roomRepository = new RoomRepository(room.getRoomId());
        roomRepository.save(room);
        return room;
    }
    private void setupCourseTypes(List<CourseType>types){
        cg_courseTypes.removeAllViews();

        for (CourseType type : types) {
            // 2. Create the Chip programmatically
            Chip chip = new Chip(this);

            chip.setText(type.getCourseType()); // Use the name for the label
            chip.setCheckable(true);

            // --- THE KEY PART ---
            chip.setTag(type); // Store the entire object here
            // --------------------

            // Apply your Emerald styling
            chip.setChipBackgroundColorResource(R.color.brand_emerald_pale);
            chip.setChipStrokeColorResource(R.color.brand_emerald_light);

            cg_courseTypes.addView(chip);
        }
    }
    private boolean checkComplete(){
        return Tool.boolOf(et_roomTag.getText().toString()) &&
                sb_roomCapacity.getProgress() != 0 &&
                !cg_courseTypes.getCheckedChipIds().isEmpty();
    }
    private ArrayList<CourseType> getSelectedCourseObjects() {
        ArrayList<CourseType> selectedObjects = new java.util.ArrayList<>();
        ArrayList<Integer> ids = (ArrayList<Integer>) cg_courseTypes.getCheckedChipIds();

        for (Integer id : ids) {
            Chip chip = findViewById(id);

            CourseType type = (CourseType) chip.getTag();
            if (type != null) {
                selectedObjects.add(type);
            }
        }

        return selectedObjects;
    }
}

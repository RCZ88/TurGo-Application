package com.example.turgo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomViewHolder> {
    private final ArrayList<Room> rooms = new ArrayList<>();
    private Activity activity ;

    public RoomAdapter(Activity activity){
        this.activity = activity;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setRooms(List<Room> roomList) {
        rooms.clear();
        if (roomList != null) {
            rooms.addAll(roomList);
        }
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void addRoom(Room room){
        rooms.add(room);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.room_item_view, parent, false);

        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.tv_roomCapacity.setText(room.getCapacity() + " Person");
        holder.tv_roomTag.setText(room.getRoomTag());
        holder.btn_removeRoom.setOnClickListener(v -> {
            Tool.warn(activity, "Delete Room", "Sure deleting this room?", "Confirm", "Cancel",
                    () -> {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    return;
                }

                Room roomToRemove = rooms.get(adapterPosition);
                rooms.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                notifyItemRangeChanged(adapterPosition, rooms.size() - adapterPosition);

                new RoomRepository(roomToRemove.getRoomId()).delete();
            });
        });

        holder.tv_courseCompat.setText(String.join(", ", CourseType.getCourseTypeString(room.getSuitableCourseType())));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }
}

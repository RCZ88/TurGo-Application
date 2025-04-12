package com.example.turgo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class TaskPagerAdapter extends FragmentStateAdapter {
    private ArrayList<Task>tasks;
    public TaskPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Task>tasks) {
        super(fragmentActivity);
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return TaskDisplay.newInstance(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}

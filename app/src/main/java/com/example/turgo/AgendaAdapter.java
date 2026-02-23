package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaViewHolder>{
    private final ArrayList<Agenda>agendas;

    public AgendaAdapter(ArrayList<Agenda> agendas) {
        this.agendas = agendas;
    }

    @NonNull
    @Override
    public AgendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.agenda_display, parent, false);
        return new AgendaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgendaViewHolder holder, int position) {
        Agenda agenda = agendas.get(position);
        String teacherName = "Teacher";
        if (agenda != null && agenda.getTeacher() != null && Tool.boolOf(agenda.getTeacher().getFullName())) {
            teacherName = agenda.getTeacher().getFullName();
        }
        holder.tv_teacherName.setText(teacherName);

        String content = agenda != null && Tool.boolOf(agenda.getContents())
                ? agenda.getContents()
                : "No agenda details.";
        holder.tv_contents.setText(content);

        String date = agenda != null && agenda.getDate() != null
                ? agenda.getDate().toString()
                : "No date";
        holder.tv_dateDisplay.setText(date);
    }

    @Override
    public int getItemCount() {
        return agendas.size();
    }
}

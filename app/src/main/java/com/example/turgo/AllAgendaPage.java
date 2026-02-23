package com.example.turgo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;

public class AllAgendaPage extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView rvThisWeek;
    private RecyclerView rvPastAgendas;
    private LinearLayout llThisWeekEmpty;
    private LinearLayout llPastEmpty;
    private TextView tvThisWeekCount;
    private TextView tvPastCount;

    private final ArrayList<Agenda> thisWeekAgendas = new ArrayList<>();
    private final ArrayList<Agenda> pastAgendas = new ArrayList<>();

    public AllAgendaPage() {}

    public static AllAgendaPage newInstance(String param1, String param2) {
        AllAgendaPage fragment = new AllAgendaPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_agenda_page, container, false);

        rvThisWeek = view.findViewById(R.id.rv_AAP_ThisWeek);
        rvPastAgendas = view.findViewById(R.id.rv_AAP_PastAgendas);
        llThisWeekEmpty = view.findViewById(R.id.ll_AAP_ThisWeekEmpty);
        llPastEmpty = view.findViewById(R.id.ll_AAP_PastEmpty);
        tvThisWeekCount = view.findViewById(R.id.tv_AAP_ThisWeekCount);
        tvPastCount = view.findViewById(R.id.tv_AAP_PastCount);

        rvThisWeek.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPastAgendas.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<Agenda> agendas = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getSerializable(Agenda.SERIALIZE_KEY_CODE) instanceof ArrayList) {
            agendas = (ArrayList<Agenda>) bundle.getSerializable(Agenda.SERIALIZE_KEY_CODE);
        }

        splitAgendasByWeek(agendas != null ? agendas : new ArrayList<>());
        bindSections();
        return view;
    }

    private void splitAgendasByWeek(ArrayList<Agenda> source) {
        thisWeekAgendas.clear();
        pastAgendas.clear();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        for (Agenda agenda : source) {
            if (agenda == null || agenda.getDate() == null) {
                continue;
            }
            LocalDate agendaDate = agenda.getDate();
            if (!agendaDate.isBefore(weekStart) && !agendaDate.isAfter(weekEnd)) {
                thisWeekAgendas.add(agenda);
            } else if (agendaDate.isBefore(weekStart)) {
                pastAgendas.add(agenda);
            } else {
                thisWeekAgendas.add(agenda);
            }
        }

        thisWeekAgendas.sort(Comparator.comparing(Agenda::getDate, Comparator.nullsLast(LocalDate::compareTo)));
        pastAgendas.sort((a, b) -> {
            LocalDate da = a.getDate();
            LocalDate db = b.getDate();
            if (da == null && db == null) {
                return 0;
            }
            if (da == null) {
                return 1;
            }
            if (db == null) {
                return -1;
            }
            return db.compareTo(da);
        });
    }

    private void bindSections() {
        rvThisWeek.setAdapter(new AgendaAdapter(thisWeekAgendas));
        rvPastAgendas.setAdapter(new AgendaAdapter(pastAgendas));

        tvThisWeekCount.setText(String.valueOf(thisWeekAgendas.size()));
        tvPastCount.setText(String.valueOf(pastAgendas.size()));

        Tool.handleEmpty(thisWeekAgendas.isEmpty(), rvThisWeek, llThisWeekEmpty);
        Tool.handleEmpty(pastAgendas.isEmpty(), rvPastAgendas, llPastEmpty);
    }
}

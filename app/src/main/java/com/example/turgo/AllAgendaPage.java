package com.example.turgo;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllAgendaPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllAgendaPage extends Fragment {
    ArrayList<Agenda>agendas;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    RecyclerView rv_allAgenda;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllAgendaPage(ArrayList<Agenda> agendas){
        this.agendas = agendas;
    }
    public AllAgendaPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllAgendaPage.
     */
    // TODO: Rename and change types and number of parameters
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_agenda_page, container, false);
        rv_allAgenda = view.findViewById(R.id.rv_AllAgendas);
        Bundle bundle = getArguments();
        agendas = (ArrayList<Agenda>)bundle.getSerializable(Agenda.SERIALIZE_KEY_CODE);
        AgendaAdapter agendaAdapter = new AgendaAdapter(agendas);
        rv_allAgenda.setAdapter(agendaAdapter);
        // Inflate the layout for this fragment
        return view;
    }
}
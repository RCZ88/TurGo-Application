package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AgendaRepository implements RepositoryClass<Agenda, AgendaFirebase> {

    private DatabaseReference agendaRef;

    public AgendaRepository(String agendaId) {
        agendaRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.AGENDA.getPath())
                .child(agendaId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return agendaRef;
    }

    @Override
    public Class<AgendaFirebase> getFbClass() {
        return AgendaFirebase.class;
    }

    /* =======================
       SIMPLE FIELD UPDATES
       ======================= */

    public void updateDate(String date) {
        agendaRef.child("date").setValue(date);
    }

    public void updateContents(String contents) {
        agendaRef.child("contents").setValue(contents);
    }

    public void updateAgendaImage(String imagePath) {
        agendaRef.child("agendaImage").setValue(imagePath);
    }

    /* =======================
       RELATION REFERENCES
       ======================= */

    public void updateMeeting(Meeting meeting) {
        agendaRef.child("ofMeeting").setValue(meeting.getID());
    }

    public void updateTeacher(Teacher teacher) {
        agendaRef.child("teacher").setValue(teacher.getID());
    }

    public void updateStudent(Student student) {
        agendaRef.child("student").setValue(student.getID());
    }

    public void updateCourse(String courseId) {
        agendaRef.child("ofCourse").setValue(courseId);
    }
}

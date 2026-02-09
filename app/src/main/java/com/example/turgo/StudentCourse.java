package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.UUID;

public class StudentCourse implements Serializable, RequireUpdate<StudentCourse, StudentCourseFirebase, StudentCourseRepository> {
    private final FirebaseNode fbn = FirebaseNode.STUDENTCOURSE;
    private final Class<StudentCourseFirebase>fbc = StudentCourseFirebase.class;
    private final String sc_ID;
    private ArrayList<Schedule> schedulesOfCourse;
    private boolean paymentPreferences; //false: week | true: month
    private boolean privateOrGroup;
    private ArrayList<Task> tasks;
    private ArrayList<Agenda> agendas;
    private ArrayList<TimeSlot>timeSlots;
    private int pricePer;
    public StudentCourse(boolean paymentPreferences, boolean privateOrGroup, int pricePer, ArrayList<TimeSlot>timeSlots){
        schedulesOfCourse = new ArrayList<>();
        this.paymentPreferences = paymentPreferences;
        this.privateOrGroup = privateOrGroup;
        tasks = new ArrayList<>();
        this.timeSlots = timeSlots;
        agendas = new ArrayList<>();
        this.pricePer = pricePer;
        sc_ID = UUID.randomUUID().toString();
    }
    public void assignTask(Task task){
        getStudent(new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Student object) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                object.assignTask(task);
                tasks.add(task);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }
    public void assignAgenda(Agenda agenda) throws IllegalAccessException, InstantiationException {
        StudentCourseRepository studentCourseRepository = new StudentCourseRepository(getID());

        agendas.add(agenda);
        studentCourseRepository.addAgenda(agenda);
        getStudent(new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Student object) throws IllegalAccessException, InstantiationException {
                object.addAgenda(agenda);
                StudentRepository studentRepository = new StudentRepository(object.getID());
                studentCourseRepository.addAgenda(agenda);
                studentRepository.addAllAgenda(agenda);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public ArrayList<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(ArrayList<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public StudentCourse(){
        sc_ID = "";
    }
    public String getSc_ID() {
        return sc_ID;
    }

    public void addSchedule(Schedule schedule){
        schedulesOfCourse.add(schedule);
    }

    public void getStudent(ObjectCallBack<Student> callBack) {
        try {
            findAggregatedObject( Student.class, "studentCourseTaken", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public void getOfCourse(ObjectCallBack<Course> callBack) {
        try {
            findAggregatedObject(Course.class, "studentsCourse", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Schedule> getSchedulesOfCourse() {
        return schedulesOfCourse;
    }

    public void setSchedulesOfCourse(ArrayList<Schedule> schedulesOfCourse) {
        this.schedulesOfCourse = schedulesOfCourse;
    }

    public boolean isPaymentPreferences() {
        return paymentPreferences;
    }

    public void setPaymentPreferences(boolean paymentPreferences) {
        this.paymentPreferences = paymentPreferences;
    }

    public boolean isPrivateOrGroup() {
        return privateOrGroup;
    }

    public void setPrivateOrGroup(boolean privateOrGroup) {
        this.privateOrGroup = privateOrGroup;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public ArrayList<Agenda> getAgendas() {
        return agendas;
    }

    public void setAgendas(ArrayList<Agenda> agendas) {
        this.agendas = agendas;
    }

    public int getPricePer() {
        return pricePer;
    }

    public void setPricePer(int pricePer) {
        this.pricePer = pricePer;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<StudentCourseFirebase> getFirebaseClass() {
        return fbc;
    }

    @Override
    public String getID() {
        return sc_ID;
    }

    @Override
    public Class<StudentCourseRepository> getRepositoryClass() {
        return StudentCourseRepository.class;
    }

}

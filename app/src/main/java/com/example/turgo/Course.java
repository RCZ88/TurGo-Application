package com.example.turgo;

import static com.example.turgo.Tool.getDrawableFromId;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Course implements Serializable {
    private final String courseID;
    private static final String FIREBASE_DB_REFERENCE = "Courses";
    Context context;
    private Bitmap logo;
    private Bitmap background;
    private CourseType courseType;
    private String courseName;
    private Teacher teacher;
    private ArrayList<Student> students;
    private ArrayList<Schedule> schedules;
    private RTDBManager<Course>rtdbManager;
    private ArrayList<Agenda>agendas;
    public Course(Context context, CourseType courseType, String courseName, Teacher teacher, ArrayList<Schedule>schedules){
        courseID = UUID.randomUUID().toString();
        this.context = context;
        this.courseType = courseType;
        this.courseName = courseName;
        this.teacher = teacher;
        this.schedules = schedules;
        students = new ArrayList<>();
        agendas = new ArrayList<>();
        setCourseLogo();
        rtdbManager = new RTDBManager<>();
    }
    public void updateDB(Course course){
        rtdbManager.storeData(FIREBASE_DB_REFERENCE, courseName, course, "Course", "Course");
    }
    public void setCourseLogo(){
        Drawable logo = null;
        switch(courseType.getCourseType()){
            case "Piano":
                logo = getDrawableFromId(context, R.drawable.piano);
            case "Guitar":
                logo = getDrawableFromId(context, R.drawable.guitar);
            case "Vocal":
                logo = getDrawableFromId(context, R.drawable.microphone);
            case "Violin":
                logo = getDrawableFromId(context, R.drawable.violin);
            case "Drum":
                logo = getDrawableFromId(context, R.drawable.drum);
            case "Chinese":
                logo = getDrawableFromId(context, R.drawable.lanterns);
            case "English":
                logo = getDrawableFromId(context, R.drawable.english);
            case "Math":
                logo = getDrawableFromId(context, R.drawable.calculator);
            case "Bimbel":
                logo = getDrawableFromId(context, R.drawable.book);
        }
        this.logo = Tool.drawableToBitmap(logo);
    }


    public void setCourseType(CourseType courseType){ this.courseType = courseType; }

    public CourseType getCourseType(){ return courseType; }

    public void addStudent(Student student){
        students.add(student);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<Schedule> schedules) {
        this.schedules = schedules;
    }

    public Bitmap getLogo() {
        return logo;
    }

    public String getDaysOfSchedule(){
        String[]days = new String[schedules.size()];
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<schedules.size(); i++){
            days[i] = schedules.get(i).getDay().toString();
            if(i+1 != schedules.size()){
                sb.append(days[i] + ", ");
            }else{
                sb.append(days[i]);
            }
        }
        return sb.toString();
    }
    public String getCourseID(){
        return this.courseID;
    }
    public void setLogo(Bitmap logo) {
        this.logo = logo;
    }

    public Agenda getCurrentAgenda(){
        return agendas.get(agendas.size()-1);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Bitmap getBackground() {
        return background;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public RTDBManager<Course> getRtdbManager() {
        return rtdbManager;
    }

    public void setRtdbManager(RTDBManager<Course> rtdbManager) {
        this.rtdbManager = rtdbManager;
    }

    public ArrayList<Agenda> getAgenda() {
        return agendas;
    }

    public void setAgenda(ArrayList<Agenda> agenda) {
        this.agendas = agenda;
    }
}

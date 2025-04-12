package com.example.turgo;

import static com.example.turgo.Tool.getDrawableFromId;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

public class Course implements Serializable {
    private final String courseID;
    private static final String FIREBASE_DB_REFERENCE = "Courses";
    public static final String SERIALIZE_KEY_CODE = "courseObj";
    private static RTDBManager<Course>rtdbManager;
    Context context;
    private Bitmap logo;
    private Bitmap background;
    private CourseType courseType;
    private String courseName;
    private String courseDescription;
    private int maxStudentPerMeeting; //how much can one meeting hold
    private boolean [] paymentPer; //accept per month or and per meeting
    private boolean [] privateGroup; //accept private and or group?
    private ArrayList<Pricing>prices;
    private ArrayList<DayTimeArrangement> dayTimeArrangement; //the days available for this course
    private Teacher teacher;
    private ArrayList<Student> students;
    private ArrayList<Schedule> schedules;
    private ArrayList<Agenda>agendas;
    private ArrayList<StudentCourse>studentsCourse;
    private double baseCost;
    private double hourlyCost;
    private double monthlyDiscountPercentage;
    private boolean autoAcceptStudent;
    public Course(Context context, CourseType courseType, String courseName, String courseDescription, Teacher teacher, ArrayList<Schedule>schedules, int maxStudentPerMeeting, double baseCost, double hourlyCost, boolean[]paymentPer, boolean[]privateGroup, double monthlyDiscountPercentage, boolean autoAcceptStudent){
        courseID = UUID.randomUUID().toString();
        this.context = context;
        this.courseType = courseType;
        this.courseName = courseName;
        this.teacher = teacher;
        this.courseDescription = courseDescription;
        this.schedules = schedules;
        this.baseCost = baseCost;
        this.hourlyCost = hourlyCost;
        this.maxStudentPerMeeting = maxStudentPerMeeting;
        students = new ArrayList<>();
        agendas = new ArrayList<>();
        studentsCourse = new ArrayList<>();
        dayTimeArrangement = new ArrayList<>();
        this.paymentPer = paymentPer;
        this.privateGroup = privateGroup;
        this.monthlyDiscountPercentage = monthlyDiscountPercentage;
        this.autoAcceptStudent = autoAcceptStudent;
        prices = new ArrayList<>();
        setCourseLogo();
        setCourseBanner(1);
        rtdbManager = new RTDBManager<>();
    }

    public boolean isAutoAcceptStudent() {
        return autoAcceptStudent;
    }

    public void setAutoAcceptStudent(boolean autoAcceptStudent) {
        this.autoAcceptStudent = autoAcceptStudent;
    }

    public ArrayList<StudentCourse> getStudentsCourse() {
        return studentsCourse;
    }

    public double getBaseCost() {
        return baseCost;
    }

    public double getHourlyCost() {
        return hourlyCost;
    }

    public double getMonthlyDiscountPercentage() {
        return monthlyDiscountPercentage;
    }

    public StudentCourse getSCOfStudent(Student student){
        for(StudentCourse sc : studentsCourse){
            if(sc.getStudent() == student){
                return sc;
            }
        }
        return null;
    }
    public void applySchedule(DayOfWeek day, LocalTime start, int duration, boolean isPrivate, int maxPeople, Student student, boolean payment, int cost){
        if(getDTAOfDay(day).getMaxMeeting() < getDTAOfDay(day).getOccupied().size()){
            if(isPrivate){
                getDTAOfDay(day).applySchedule(start, duration, student, payment, cost);
            }else{
                getDTAOfDay(day).applySchedule(start, duration, maxPeople, student, payment, cost);
            }
            updateDB(this);
        }else{
            Log.i("Day selecting", "Max meeting of Teacher has been Reached! Please select a different Day!");
        }
    }
    public String getDaysAvailable(){
        StringBuilder sb = new StringBuilder();
        for(DayTimeArrangement dta : dayTimeArrangement){
            sb.append(dta.getDay().toString() + ", ");
        }
        return sb.toString();
    }
    public Schedule getScheduleFromTimeSlot(TimeSlot ts){
        for(Schedule schedule : schedules){
            if(ts.getStart() == schedule.getMeetingStart()){
                return schedule;
            }
        }
        return null;
    }
    public int amountDaysAvail(){
        return dayTimeArrangement.size();
    }
    public ArrayList<Schedule> getScheduleOfDay(DayOfWeek day){
        ArrayList<Schedule>schedules = new ArrayList<>();
        for(Schedule schedule : this.schedules){
            if(schedule.getDay() == day){
                schedules.add(schedule);
            }
        }
        return schedules;
    }
    public DayTimeArrangement getDTAOfDay(DayOfWeek day){
        for(DayTimeArrangement dta : this.dayTimeArrangement){
            if(dta.getDay() == day){
                return dta;
            }
        }
        return null;
    }
    public ArrayList<DayOfWeek>filterFullDays(boolean isPrivate, int duration){//filter out full days.
        ArrayList<DayOfWeek>daysAvail = new ArrayList<>();
        for(DayTimeArrangement dta : dayTimeArrangement){
            if(!(dta.findFreeSlots(isPrivate, 5, duration).isEmpty())){
                daysAvail.add(dta.getDay());
            }
        }
        return daysAvail;
    }
    public ArrayList<TimeSlot> findFreeSpotOfDay(DayOfWeek day, boolean isPrivate, int maxPeople, int duration){ //find free spots of a certain day
        if(isPrivate){maxPeople = 1;}
        return getDTAOfDay(day).findFreeSlots(isPrivate, maxPeople, duration);
    }

    public void updateDB(Course course){
        rtdbManager.storeData(FIREBASE_DB_REFERENCE, courseName, course, "Course", "Course");
    }

    public ArrayList<DayTimeArrangement> getDayTimeArrangement() {
        return dayTimeArrangement;
    }

    public void setDayTimeArrangement(ArrayList<DayTimeArrangement> dayTimeArrangement) {
        this.dayTimeArrangement = dayTimeArrangement;
    }

    public void addNewDayTimeArr(DayTimeArrangement dta){
        this.dayTimeArrangement.add(dta);
    }

    public void changeCourseBanner(int color){
        setCourseBanner(color);
    }
    public void setCourseBanner(int color){
        Drawable banner = null;
        switch(color){
            case 1:
                banner = getDrawableFromId(context, R.drawable.banner_blue);
            case 2:
                banner = getDrawableFromId(context, R.drawable.banner_green);
            case 3:
                banner = getDrawableFromId(context, R.drawable.banner_red);
        }
        this.background = Tool.drawableToBitmap(banner);
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

    public int getMaxStudentPerMeeting() {
        return maxStudentPerMeeting;
    }

    public void setMaxStudentPerMeeting(int maxStudentPerMeeting) {
        this.maxStudentPerMeeting = maxStudentPerMeeting;
    }

    public boolean[] getPaymentPer() {
        return paymentPer;
    }

    public void setPaymentPer(boolean[] paymentPer) {
        this.paymentPer = paymentPer;
    }

    public boolean[] getPrivateGroup() {
        return privateGroup;
    }

    public void setPrivateGroup(boolean[] privateGroup) {
        this.privateGroup = privateGroup;
    }

    public ArrayList<Pricing> getPrices() {
        return prices;
    }

    public void setPrices(ArrayList<Pricing> prices) {
        this.prices = prices;
    }

    public void addPricing(Pricing pricing){
        this.prices.add(pricing);
    }
    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }
    public double calcPrice(boolean isPrivate, int stuCount, double duration){
        double multiplier = 0;
        if(isPrivate){
            multiplier = 1.5;
        }else{
            multiplier = 1.2 - (0.1 * stuCount);
        }
        return (baseCost + duration/60 * hourlyCost) * multiplier;
    }

    public ArrayList<Agenda> getAgendas() {
        return agendas;
    }

    public void setAgendas(ArrayList<Agenda> agendas) {
        this.agendas = agendas;
    }

    public void setCourseType(CourseType courseType){ this.courseType = courseType; }

    public CourseType getCourseType(){ return courseType; }

    public void addStudent(Student student, boolean paymentPreferences, boolean privateOrGroup, int payment, Schedule schedule){
        StudentCourse sc = new StudentCourse(student, this, paymentPreferences, privateOrGroup, payment);
        sc.addSchedule(schedule);
        studentsCourse.add(sc);
        students.add(student);
        updateDB(this);
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
    public void addSchedule(Schedule schedule){
        schedules.add(schedule);
        updateDB(this);

    }
    public Bitmap getLogo() {
        return logo;
    }
    public ArrayList<Schedule> getScheduleOfStudent(Student student){
        ArrayList<Schedule>sos = new ArrayList<>();
        for(Schedule schedule : schedules){
            if(schedule.getStudents().contains(student)){
                sos.add(schedule);
            }
        }
        return sos;
    }
    public String getDaysOfSchedule(Student student){
        ArrayList<Schedule>schedules = getScheduleOfStudent(student);
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

package com.example.turgo;

import static com.example.turgo.Tool.getDrawableFromId;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseError;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class Course implements Serializable, RequireUpdate<Course, CourseFirebase, CourseRepository>{
    private final String courseID;
    private Class<CourseFirebase> fbc = CourseFirebase.class;
    public static final String SERIALIZE_KEY_CODE = "courses";
    public static final int PER_MONTH_INDEX = 0;
    public static final int PER_MEETING_INDEX = 1;
    public static final int PRIVATE_INDEX = 0;
    public static final int GROUP_INDEX = 1;
    private final FirebaseNode fbn = FirebaseNode.COURSE;
    Context context;
    private ArrayList<String> imagesCloudinary;
    private String logoCloudinary;
    private String backgroundCloudinary;
    private String teacher;
    private CourseType courseType;
    private String courseName;
    private String courseDescription;
    private int maxStudentPerMeeting; //how much can one meeting hold
    private ArrayList<Boolean> paymentPer; //accept per month or and per meeting
    private ArrayList<Boolean> privateGroup; //accept private and or group?
    private ArrayList<DayTimeArrangement> dayTimeArrangement; //the days available for this course
    private ArrayList<Schedule> schedules;
    private ArrayList<Agenda>agendas;
    private ArrayList<StudentCourse>studentsCourse;
    private ArrayList<String>students;
    private double baseCost;
    private double hourlyCost;
    private double monthlyDiscountPercentage;
    private boolean autoAcceptStudent;
    public Course(Context context, CourseType courseType, String courseName, String courseDescription, String teacher, ArrayList<Schedule>schedules, int maxStudentPerMeeting, double baseCost, double hourlyCost, ArrayList<Boolean>paymentPer, ArrayList<Boolean>privateGroup, double monthlyDiscountPercentage, boolean autoAcceptStudent){
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
        agendas = new ArrayList<>();
        students = new ArrayList<>();
        studentsCourse = new ArrayList<>();
        dayTimeArrangement = new ArrayList<>();
        this.paymentPer = paymentPer;
        this.privateGroup = privateGroup;
        this.monthlyDiscountPercentage = monthlyDiscountPercentage;
        this.autoAcceptStudent = autoAcceptStudent;
        setCourseLogo();
        setCourseBanner(1);
    }
    public Course(){
        courseID = UUID.randomUUID().toString();
        this.imagesCloudinary = new ArrayList<>();
        this.paymentPer = new ArrayList<>();
        this.privateGroup = new ArrayList<>();
        this.dayTimeArrangement = new ArrayList<>();
        this.schedules = new ArrayList<>();
        this.agendas = new ArrayList<>();
        this.studentsCourse = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setMonthlyDiscountPercentage(double monthlyDiscountPercentage) {
        this.monthlyDiscountPercentage = monthlyDiscountPercentage;
    }

    public void setHourlyCost(double hourlyCost) {
        this.hourlyCost = hourlyCost;
    }

    public void setBaseCost(double baseCost) {
        this.baseCost = baseCost;
    }

    public void setStudentsCourse(ArrayList<StudentCourse> studentsCourse) {
        this.studentsCourse = studentsCourse;
    }
    public ArrayList<String> getStudentIds(){
        return students;
    }
    public String getBackgroundCloudinary() {
        return backgroundCloudinary;
    }

    public void setBackgroundCloudinary(String backgroundCloudinary) {
        this.backgroundCloudinary = backgroundCloudinary;
    }

    public String getLogoCloudinary() {
        return logoCloudinary;
    }

    public void setLogoCloudinary(String logoCloudinary) {
        this.logoCloudinary = logoCloudinary;
    }

    public ArrayList<String> getImagesCloudinary() {
        return imagesCloudinary;
    }

    public void setImagesCloudinary(ArrayList<String> imagesCloudinary) {
        this.imagesCloudinary = imagesCloudinary;
    }

    public FirebaseNode getFbn() {
        return fbn;
    }

    public Class<CourseFirebase> getFbc() {
        return fbc;
    }

    public void setFbc(Class<CourseFirebase> fbc) {
        this.fbc = fbc;
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

    public void getSCofStudent(Student student, ObjectCallBack<StudentCourse>callBack) {
        if(!Tool.boolOf(studentsCourse)){
            studentsCourse = new ArrayList<>();
            return;
        }
        for(StudentCourse sc : studentsCourse){
            sc.getStudent(new ObjectCallBack<Student>() {
                @Override
                public void onObjectRetrieved(Student object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                    if(object == student){
                        callBack.onObjectRetrieved(sc);
                    }
                }

                @Override
                public void onError(DatabaseError error) {

                }
            });

        }
    }
//    public void applySchedule(DayOfWeek day, LocalTime start, int duration, boolean isPrivate, int maxPeople, Student student, boolean payment, int cost){
//        if(getDTAOfDay(day).getMaxMeeting() < getDTAOfDay(day).getOccupied().size()){
//            if(isPrivate){
//                getDTAOfDay(day).applySchedule(start, duration, student, payment, cost);
//            }else{
//                getDTAOfDay(day).applySchedule(start, duration, maxPeople, student, payment, cost);
//            }
//            updateDB(this);
//        }else{
//            Log.i("Day selecting", "Max meeting of Teacher has been Reached! Please select a different Day!");
//        }
//    }
    public String getDaysAvailable(){
        StringBuilder sb = new StringBuilder();
        for(DayTimeArrangement dta : dayTimeArrangement){
            sb.append(dta.getDay().toString() + ", ");
        }
        return sb.toString();
    }
    public Schedule getScheduleFromTimeSlot(TimeSlot ts){
        if(schedules == null){
            schedules = new ArrayList<>();
            return null;
        }
        for(Schedule schedule : schedules){
            if(ts.getStart() == schedule.getMeetingStart()){
                return schedule;
            }
        }
        return null;
    }
    public void addStudent(Student student){
       addStudent(student.getID());
    }
    public void addStudent(String studentId){
        students.add(studentId);
    }
    public int amountDaysAvail(){
        return dayTimeArrangement.size();
    }
    public ArrayList<Schedule> getScheduleOfDay(DayOfWeek day){
        if(!Tool.boolOf(schedules)){
            schedules =  new ArrayList<>();
            return schedules;
        }
        ArrayList<Schedule>schedules = new ArrayList<>();
        for(Schedule schedule : this.schedules){
            if(schedule.getDay() == day){
                schedules.add(schedule);
            }
        }
        return schedules;
    }
    public Task<Boolean> hasStudent(Student student) {
        return getStudents().continueWith(task -> {
            if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());

            for (Student s : task.getResult()) {
                if (s.getID().equals(student.getID())) {
                    return true;
                }
            }
            return false;
        });
    }

    public DayTimeArrangement getDTAOfDay(DayOfWeek day){
        if(!Tool.boolOf(dayTimeArrangement)){
            dayTimeArrangement = new ArrayList<>();
        }

        for(DayTimeArrangement dta : this.dayTimeArrangement){
            if(dta.getDay() == day){
                return dta;
            }
        }
        return null;
    }
    public ArrayList<DayOfWeek>filterFullDays(ScheduleQuality sq, int duration, ArrayList<DayTimeArrangement>dtaAvailable){//filter out full days.
        ArrayList<DayOfWeek>daysAvail = new ArrayList<>();
        for(DayTimeArrangement dta : dtaAvailable){
            ArrayList<TimeSlot> freeSlots = dta.findFreeSlots(sq, 5, duration);
            String str = String.join(", ", Tool.streamToArray(freeSlots.stream().map(TimeSlot::toString)));
            Log.d("FilterFullDays", "Free Slots: " + str);
            if(!(freeSlots.isEmpty())){
                daysAvail.add(dta.getDay());
            }
        }
        return daysAvail;
    }
    public ArrayList<TimeSlot> findFreeSpotOfDay(DayOfWeek day, ScheduleQuality sq, int maxPeople, int duration){ //find free spots of a certain day
        if(sq == ScheduleQuality.PRIVATE_ONLY){maxPeople = 1;}
        return getDTAOfDay(day).findFreeSlots(sq, maxPeople, duration);
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<CourseRepository> getRepositoryClass() {
        return CourseRepository.class;
    }

    @Override
    public Class<CourseFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID(){
        return courseID;
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
        String banner = null;
        switch(color){
            case 1:
                this.backgroundCloudinary = BuildInBanner.BLUE.getLink();
            case 2:
                this.backgroundCloudinary = BuildInBanner.GREEN.getLink();
            case 3:
                this.backgroundCloudinary = BuildInBanner.RED.getLink();
        }
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
    }

    public int getMaxStudentPerMeeting() {
        return maxStudentPerMeeting;
    }

    public void setMaxStudentPerMeeting(int maxStudentPerMeeting) {
        this.maxStudentPerMeeting = maxStudentPerMeeting;
    }

    public ArrayList<Boolean> getPaymentPer() {
        return paymentPer;
    }

    public void setPaymentPer(ArrayList<Boolean> paymentPer) {
        this.paymentPer = paymentPer;
    }

    public ArrayList<Boolean> getPrivateGroup() {
        return privateGroup;
    }

    public void setPrivateGroup(ArrayList<Boolean> privateGroup) {
        this.privateGroup = privateGroup;
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

    public void addStudent(Student student, boolean paymentPreferences, boolean privateOrGroup, int payment, ArrayList<Schedule>schedules, ArrayList<TimeSlot> timeSlot) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        StudentCourse sc = new StudentCourse(paymentPreferences, privateOrGroup, payment, timeSlot);
        sc.setSchedulesOfCourse(schedules);
        StudentCourseRepository studentCourseRep = new StudentCourseRepository(sc.getID());
        studentCourseRep.save(sc);

        if(studentsCourse == null){
            studentsCourse = new ArrayList<>();
        }
        CourseRepository courseRepo = new CourseRepository(courseID);

        studentsCourse.add(sc);
        courseRepo.addStudentCourse(sc);

        students.add(student.getID());
        courseRepo.addStudent(student);

        StudentRepository studentRepo = new StudentRepository(student.getID());
        student.getCourseTaken().add(this);

        student.getStudentCourseTaken().add(sc);
        studentRepo.addStudentCourseTaken(sc);

        for(Schedule schedule : schedules){
            DayTimeArrangement dtaOfDay = getDTAOfDay(schedule.getDay());
            if(dtaOfDay == null){
                continue;
            }
            dtaOfDay.getOccupied().add(schedule);
            DTARepository dtaRepository = new DTARepository(dtaOfDay.getID());
            dtaRepository.addOccupied(schedule);
            this.schedules.add(schedule);
            courseRepo.addSchedule(schedule);
        }

    }
    public Task<Void> addStudentAsync(Student student, boolean paymentPreferences, boolean privateOrGroup, int payment, ArrayList<Schedule> schedules, ArrayList<TimeSlot> timeSlot) {
        List<Task<?>> tasks = new ArrayList<>();
        StudentCourse sc = new StudentCourse(paymentPreferences, privateOrGroup, payment, timeSlot);
        sc.setSchedulesOfCourse(schedules);

        StudentCourseRepository studentCourseRep = new StudentCourseRepository(sc.getID());
        tasks.add(logTask("studentCourse.save:" + sc.getID(), studentCourseRep.saveAsync(sc)));

        if (studentsCourse == null) {
            studentsCourse = new ArrayList<>();
        }
        if (this.schedules == null) {
            this.schedules = new ArrayList<>();
        }

        CourseRepository courseRepo = new CourseRepository(courseID);

        studentsCourse.add(sc);
        tasks.add(logTask("course.addStudentCourse:" + sc.getID(),
                courseRepo.addStringToArrayAsync("studentsCourse", sc.getID())));

        students.add(student.getID());
        tasks.add(logTask("course.addStudent:" + student.getID(),
                courseRepo.addStringToArrayAsync("students", student.getID())));

        StudentRepository studentRepo = new StudentRepository(student.getID());
        student.getCourseTaken().add(this);
        student.getStudentCourseTaken().add(sc);
        tasks.add(logTask("student.addCourseTaken:" + this.getID(),
                studentRepo.addStringToArrayAsync("courseTaken", this.getID())));
        tasks.add(logTask("student.addStudentCourseTaken:" + sc.getID(),
                studentRepo.addStringToArrayAsync("studentCourseTaken", sc.getID())));

        for (Schedule schedule : schedules) {
            DayTimeArrangement dtaOfDay = getDTAOfDay(schedule.getDay());
            if (dtaOfDay == null) {
                continue;
            }
            dtaOfDay.getOccupied().add(schedule);
            DTARepository dtaRepository = new DTARepository(dtaOfDay.getID());
            tasks.add(logTask("dta.addOccupied:" + dtaOfDay.getID() + ":" + schedule.getID(),
                    dtaRepository.addStringToArrayAsync("occupied", schedule.getID())));
            this.schedules.add(schedule);
            tasks.add(logTask("course.addSchedule:" + schedule.getID(),
                    courseRepo.addStringToArrayAsync("schedules", schedule.getID())));
        }

        return Tasks.whenAll(tasks);
    }

    private static <T> Task<T> logTask(String label, Task<T> task) {
        task.addOnSuccessListener(result -> Log.d("JoinCourseTask", "SUCCESS: " + label));
        task.addOnFailureListener(e -> Log.e("JoinCourseTask", "FAIL: " + label, e));
        return task;
    }
    public int getMaxMeetingDuration(){
        int maxDuration = 0;
        for(DayTimeArrangement dta : dayTimeArrangement){
            int minute = dta.getDuration();
            Log.d("getDuration", minute + " minutes");
            if(minute > maxDuration){
                maxDuration = minute;
            }
        }
        return maxDuration;
    }
    public ArrayList<DayTimeArrangement> getDtasOfTime(int minutes){
        return Tool.streamToArray(dayTimeArrangement.stream().filter(dta -> dta.getDuration() >= minutes));
    }
    public Task<Meeting> getNextMeetingOfNextSchedule(){
        LocalDate today = LocalDate.now();
        if(!Tool.boolOf(schedules)) return null;
        for(Schedule schedule : schedules){
            if(schedule.getDay().equals(today.getDayOfWeek()) && schedule.getMeetingStart().isAfter(LocalTime.now())){
                return Meeting.getClosestMeetingToSchedule(schedule);
            }
        }
        return null;
    }
    public Task<Teacher> getTeacher(){
        TeacherRepository teacherRepository = new TeacherRepository(teacher);
        return teacherRepository.loadAsNormal();
    }


    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void getTeacher(ObjectCallBack<Teacher>callBack) {
        try {
            findAggregatedObject(Teacher.class, "coursesTeach",callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
    //TODO:Handle the use cases for getStudents (circular reference).
//    public void getStudents(ObjectCallBack<ArrayList<Student>>callBack) {
//        try {
//            findAllAggregatedObjects(Student.class, "courseTaken", callBack);
//        } catch (IllegalAccessException | InstantiationException e) {
//            throw new RuntimeException(e);
//        }
//    }
    public Task<List<Student>> getStudents() {
        ArrayList<Task<Student>> taskRetrieveStudent = new ArrayList<>();
        StudentRepository studentRepository;

        for (String studentId : students) {
            studentRepository = new StudentRepository(studentId);
            taskRetrieveStudent.add(studentRepository.loadAsNormal());
        }

        // ✅ Combines all Tasks into one Task<List<Student>>
        return Tasks.whenAllSuccess(taskRetrieveStudent);
    }


    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<Schedule> schedules) {
        this.schedules = schedules;
    }
    public void addSchedule(Schedule schedule) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        schedules.add(schedule);
        new CourseRepository(getID()).addSchedule(schedule);
    }
    public String getLogo() {
        return logoCloudinary;
    }
    public void getScheduleOfStudent(Student student, ObjectCallBack<ArrayList<Schedule>>callBack){
        ArrayList<Schedule> result = new ArrayList<>();
        List<Task<List<Student>>> tasks = new ArrayList<>();

        for (Schedule schedule : schedules) {
            Task<List<Student>> task = schedule.getStudents();
            tasks.add(task);

            task.addOnSuccessListener(students -> {
                if (students.contains(student)) {
                    result.add(schedule);
                }
            });
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(schedulesResult -> {
            try {
                callBack.onObjectRetrieved(schedules);
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public Task<String> getDaysOfSchedule(Student student) {

        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        getScheduleOfStudent(student, new ObjectCallBack<>() {

            @Override
            public void onObjectRetrieved(ArrayList<Schedule> schedulesOfStudent) {

                if (schedulesOfStudent.isEmpty()) {
                    tcs.setResult("");
                    return;
                }

                Set<String>uniqueDays = new LinkedHashSet<>();
                for(Schedule s : schedules){
                    uniqueDays.add(s.getDay().toString());
                }

                String result = String.join(", ", uniqueDays);
                tcs.setResult(result);
            }

            @Override
            public void onError(DatabaseError error) {
                tcs.setException(error.toException());
            }
        });

        return tcs.getTask();
    }

    public String getCourseID(){
        return this.courseID;
    }
    public void setLogo(String logo) {
        this.logoCloudinary = logo;
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

    public String getBackground() {
        return backgroundCloudinary;
    }

    public void setBackground(String background) {
        this.backgroundCloudinary = background;
    }


    public ArrayList<Agenda> getAgenda() {
        return agendas;
    }

    public void setAgenda(ArrayList<Agenda> agenda) {
        this.agendas = agenda;
    }


    @Override
    public String toString() {
        String dtas = "empty";
        if(dayTimeArrangement != null){
            ArrayList<String> dtasList = dayTimeArrangement.stream().map((dta) ->dta.toString()).collect(Collectors.toCollection(ArrayList::new));
            dtas = "[ "  + String.join( ", ", dtasList) + " ]";
        }

        return "Course{" +
                "courseID='" + courseID + '\'' +
                ", fbc=" + fbc +
                ", fbn=" + fbn +
                ", context=" + context +
                ", imagesCloudinary=" + imagesCloudinary +
                ", logoCloudinary='" + logoCloudinary + '\'' +
                ", backgroundCloudinary='" + backgroundCloudinary + '\'' +
                ", courseType=" + courseType +
                ", courseName='" + courseName + '\'' +
                ", courseDescription='" + courseDescription + '\'' +
                ", maxStudentPerMeeting=" + maxStudentPerMeeting +
                ", paymentPer=" + paymentPer +
                ", privateGroup=" + privateGroup +
                ", dayTimeArrangement=" + dtas +
                ", schedules=" + schedules +
                ", agendas=" + agendas +
                ", studentsCourse=" + studentsCourse +
                ", baseCost=" + baseCost +
                ", hourlyCost=" + hourlyCost +
                ", monthlyDiscountPercentage=" + monthlyDiscountPercentage +
                ", autoAcceptStudent=" + autoAcceptStudent +
                '}';
    }
}

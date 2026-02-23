package com.example.turgo;

import android.util.Log;

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

public class Teacher extends User implements Serializable, RequireUpdate<Teacher, TeacherFirebase, TeacherRepository> {
    private static final FirebaseNode fbn = FirebaseNode.TEACHER;
    private static final int MAX_LATEST_SUBMISSION_SIZE = 3;
    private static final Class<TeacherFirebase> fbc = TeacherFirebase.class;
    public static final String SERIALIZE_KEY_CODE = "teacherObj";
    private String profileImageCloudinary;
    private ArrayList<Course> coursesTeach;
    private ArrayList<String> coursesTeachIds;
    private ArrayList<SubmissionDisplay> latestSubmission;
    private ArrayList<String> latestSubmissionIds;
    private ArrayList<String> courseTypeTeach;
    private ArrayList<Meeting> scheduledMeetings;
    private ArrayList<String> scheduledMeetingsIds;
    private ArrayList<Meeting> completedMeetings;
    private ArrayList<String> completedMeetingsIds;
    private ArrayList<Agenda> agendas;
    private ArrayList<String> agendasIds;
    private ArrayList<DayTimeArrangement> timeArrangements; //one object for each day of the week.
    private ArrayList<String> timeArrangementsIds;
    private String teacherResume;
    private int teachYearExperience;
    public Teacher(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super(UserType.TEACHER, gender, fullName, birthDate, nickname, email, phoneNumber);
        scheduledMeetings = new ArrayList<>();
        scheduledMeetingsIds = new ArrayList<>();
        coursesTeach = new ArrayList<>();
        coursesTeachIds = new ArrayList<>();
        profileImageCloudinary = "https://res.cloudinary.com/daccry0jr/image/upload/v1761196379/islooktidmooszzfrga3.png";
        courseTypeTeach = new ArrayList<>();
        agendas = new ArrayList<>();
        agendasIds = new ArrayList<>();
        timeArrangements = new ArrayList<>();
        timeArrangementsIds = new ArrayList<>();
        completedMeetings = new ArrayList<>();
        completedMeetingsIds = new ArrayList<>();
        latestSubmission = new ArrayList<>();
        latestSubmissionIds = new ArrayList<>();
    }


    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<TeacherRepository> getRepositoryClass() {
        return TeacherRepository.class;
    }

    @Override
    public Class<TeacherFirebase> getFirebaseClass() {
        return fbc;
    }

    @Override
    public String getID() {
        Log.d("Teacher", "getID: " + getUid());
        return getUid();
    }

    public void addLatestSubmission(SubmissionDisplay submission){
        getRepositoryInstance().addStringToArray("latestSubmission", submission.getID());
        latestSubmission.add(0, submission);
        if(latestSubmission.size() > MAX_LATEST_SUBMISSION_SIZE){
            SubmissionDisplay toRemove = latestSubmission.get(latestSubmission.size() - 1);
            latestSubmission.remove(latestSubmission.size() - 1);
            if (toRemove != null) {
                toRemove.getRepositoryInstance().remove();
                getRepositoryInstance().removeStringFromArray("latestSubmission", toRemove.getID());
            }
        }
        submission.getRepositoryInstance().save(submission);
    }

    public ArrayList<SubmissionDisplay> getLatestSubmission() {
        return latestSubmission;
    }

    public ArrayList<String> getLatestSubmissionIds() {
        latestSubmissionIds = syncIdsFromObjects(latestSubmissionIds, latestSubmission);
        return latestSubmissionIds;
    }

    public void setLatestSubmissionIds(ArrayList<String> latestSubmissionIds) {
        this.latestSubmissionIds = latestSubmissionIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<SubmissionDisplay>> getLatestSubmissionTask() {
        return loadByIds(getLatestSubmissionIds(), id -> new SubmissionDisplayRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> latestSubmission = objects);
    }

    public void setLatestSubmission(ArrayList<SubmissionDisplay> latestSubmission) {
        this.latestSubmission = latestSubmission;
    }

    public String getProfileImageCloudinary() {
        return profileImageCloudinary;
    }

    public void setProfileImageCloudinary(String profileImageCloudinary) {
        this.profileImageCloudinary = profileImageCloudinary;
    }
    public ArrayList<Schedule>getAllSchedule(){
        ArrayList<Schedule> allSchedule = new ArrayList<>();
        for(DayTimeArrangement dta : timeArrangements){
            allSchedule.addAll(dta.getOccupied());
        }
        return allSchedule;
    }

    public static String getSerializeKeyCode() {
        return SERIALIZE_KEY_CODE;
    }

    public Class<TeacherFirebase> getFbc() {
        return fbc;
    }

    public FirebaseNode getFbn() {
        return fbn;
    }

    public Teacher(){
        scheduledMeetings = new ArrayList<>();
        scheduledMeetingsIds = new ArrayList<>();
        coursesTeach = new ArrayList<>();
        coursesTeachIds = new ArrayList<>();
        profileImageCloudinary = "https://res.cloudinary.com/daccry0jr/image/upload/v1761196379/islooktidmooszzfrga3.png";
        courseTypeTeach = new ArrayList<>();
        agendas = new ArrayList<>();
        agendasIds = new ArrayList<>();
        timeArrangements = new ArrayList<>();
        timeArrangementsIds = new ArrayList<>();
        completedMeetings = new ArrayList<>();
        completedMeetingsIds = new ArrayList<>();
        latestSubmission = new ArrayList<>();
        latestSubmissionIds = new ArrayList<>();
    }

    @Override
    public String getSerializeCode() {
        return SERIALIZE_KEY_CODE;
    }

    private interface Loader<T> {
        com.google.android.gms.tasks.Task<T> load(String id);
    }

    private <T extends RequireUpdate<?, ?, ?>> ArrayList<String> syncIdsFromObjects(ArrayList<String> ids, ArrayList<T> objects) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        if (objects != null) {
            for (T object : objects) {
                if (object == null || !Tool.boolOf(object.getID())) {
                    continue;
                }
                if (!ids.contains(object.getID())) {
                    ids.add(object.getID());
                }
            }
        }
        return ids;
    }

    private <T> com.google.android.gms.tasks.Task<ArrayList<T>> loadByIds(ArrayList<String> ids, Loader<T> loader) {
        TaskCompletionSource<ArrayList<T>> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(ids)) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }
        ArrayList<com.google.android.gms.tasks.Task<T>> tasks = new ArrayList<>();
        for (String id : ids) {
            if (!Tool.boolOf(id)) {
                continue;
            }
            tasks.add(loader.load(id).continueWith(task -> task.isSuccessful() ? task.getResult() : null));
        }
        if (tasks.isEmpty()) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }
        Tasks.whenAllComplete(tasks).addOnCompleteListener(done -> {
            ArrayList<T> result = new ArrayList<>();
            for (com.google.android.gms.tasks.Task<T> task : tasks) {
                if (task.isSuccessful() && task.getResult() != null) {
                    result.add(task.getResult());
                }
            }
            tcs.setResult(result);
        });
        return tcs.getTask();
    }

    public void createAgenda(String contents, LocalDate date, Meeting ofMeeting, Student student, Course ofCourse) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Agenda agenda = new Agenda(contents, date, ofMeeting, this, student, ofCourse.getCourseID());
        this.agendas.add(agenda);
        getRepositoryInstance().save(this);
        student.addAgenda(agenda);
        student.getStudentCourseFromCourse(ofCourse, new ObjectCallBack<StudentCourse>() {
            @Override
            public void onObjectRetrieved(StudentCourse object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                object.assignAgenda(agenda);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
        student.getRepositoryInstance().save(student);
    }
    public void addTask(Task task, Course course) {
        task.getStudentAssigned().addOnSuccessListener(students->{
            for(Student student : students){
                student.getStudentCourseFromCourse(course, new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(StudentCourse object) {
                        object.assignTask(task);
                    }

                    @Override
                    public void onError(DatabaseError error) {

                    }
                });
                student.getRepositoryInstance().save(student);
            }
        });


    }
    public void addCourse(Course course){
        coursesTeach.add(course);
    }

    public ArrayList<Course> getCoursesTeach(){
        return coursesTeach;
    }

    public ArrayList<String> getCoursesTeachIds() {
        coursesTeachIds = syncIdsFromObjects(coursesTeachIds, coursesTeach);
        return coursesTeachIds;
    }

    public void setCoursesTeachIds(ArrayList<String> coursesTeachIds) {
        this.coursesTeachIds = coursesTeachIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Course>> getCoursesTeachTask() {
        return loadByIds(getCoursesTeachIds(), id -> new CourseRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> coursesTeach = objects);
    }

    public void addCourseTeach(String courseType){
        courseTypeTeach.add(courseType);
    }

    public ArrayList<String> getCourseTypeTeach() {
        return courseTypeTeach;
    }

    public ArrayList<Meeting> getScheduledMeetings(){
        return scheduledMeetings;
    }

    public ArrayList<String> getScheduledMeetingsIds() {
        scheduledMeetingsIds = syncIdsFromObjects(scheduledMeetingsIds, scheduledMeetings);
        return scheduledMeetingsIds;
    }

    public void setScheduledMeetingsIds(ArrayList<String> scheduledMeetingsIds) {
        this.scheduledMeetingsIds = scheduledMeetingsIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Meeting>> getScheduledMeetingsTask() {
        return loadByIds(getScheduledMeetingsIds(), id -> new MeetingRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> scheduledMeetings = objects);
    }

    public int getTeachYearExperience() {
        return teachYearExperience;
    }

    public ArrayList<DayTimeArrangement> getTimeArrangements() {
        if(timeArrangements == null){
            timeArrangements = new ArrayList<>();
        }
        return timeArrangements;
    }

    public ArrayList<String> getTimeArrangementsIds() {
        timeArrangementsIds = syncIdsFromObjects(timeArrangementsIds, timeArrangements);
        return timeArrangementsIds;
    }

    public void setTimeArrangementsIds(ArrayList<String> timeArrangementsIds) {
        this.timeArrangementsIds = timeArrangementsIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<DayTimeArrangement>> getTimeArrangementsTask() {
        return loadByIds(getTimeArrangementsIds(), id -> new DTARepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> timeArrangements = objects);
    }

    public void setTimeArrangements(ArrayList<DayTimeArrangement> timeArrangements) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        this.timeArrangements = timeArrangements;
    }

    public void addDTA(DayTimeArrangement dta) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        timeArrangements.add(dta);
    }
    public Schedule getNextSchedule(){
        ArrayList<Schedule> allSchedule = getAllSchedule();
        Schedule.sortSchedule(allSchedule);
        for(Schedule schedule : allSchedule){
            if(schedule.getMeetingStart().isAfter(LocalTime.now()) || schedule.getNextMeetingDate().isAfter(LocalDate.now())){
                return schedule;
            }
        }
        return null;
    }

    public void setTeachYearExperience(int teachYearExperience) {
        this.teachYearExperience = teachYearExperience;
    }

    public String getProfileImage() {
        return profileImageCloudinary;
    }


    public void setCoursesTeach(ArrayList<Course> coursesTeach) {
        this.coursesTeach = coursesTeach;
    }

    public void setCourseTypeTeach(ArrayList<String> courseTypeTeach) {
        this.courseTypeTeach = courseTypeTeach;
    }

    public void setScheduledMeetings(ArrayList<Meeting> scheduledMeetings) {
        this.scheduledMeetings = scheduledMeetings;
    }

    public ArrayList<Agenda> getAgendas() {
        return agendas;
    }

    public ArrayList<String> getAgendasIds() {
        agendasIds = syncIdsFromObjects(agendasIds, agendas);
        return agendasIds;
    }

    public void setAgendasIds(ArrayList<String> agendasIds) {
        this.agendasIds = agendasIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Agenda>> getAgendasTask() {
        return loadByIds(getAgendasIds(), id -> new AgendaRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> agendas = objects);
    }

    public void setAgendas(ArrayList<Agenda> agendas) {
        this.agendas = agendas;
    }

    public String getTeacherResume() {
        return teacherResume;
    }

    public void setTeacherResume(String teacherResume) {
        this.teacherResume = teacherResume;
    }

    public void addScheduledMeeting(Meeting meeting){
        scheduledMeetings.add(meeting);
    }

    public ArrayList<Meeting> getCompletedMeetings() {
        return completedMeetings;
    }

    public ArrayList<String> getCompletedMeetingsIds() {
        completedMeetingsIds = syncIdsFromObjects(completedMeetingsIds, completedMeetings);
        return completedMeetingsIds;
    }

    public void setCompletedMeetingsIds(ArrayList<String> completedMeetingsIds) {
        this.completedMeetingsIds = completedMeetingsIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Meeting>> getCompletedMeetingsTask() {
        return loadByIds(getCompletedMeetingsIds(), id -> new MeetingRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> completedMeetings = objects);
    }

    public void setCompletedMeetings(ArrayList<Meeting> completedMeetings) {
        this.completedMeetings = completedMeetings;
    }

    public TeacherMini toTM(){
        ArrayList<String>courseNames = new ArrayList<>();
        for(Course course : coursesTeach){
            courseNames.add(course.getCourseName());
        }
        return new TeacherMini(this.getFullName(), String.join(", ", courseNames), this.getPfpCloudinary(), this.getID());
    }

    public ArrayList<Schedule> getSchedulesOfDay(DayOfWeek day){
        for(DayTimeArrangement dta : timeArrangements){
            if(dta.getDay() == day){
                return dta.getOccupied();
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return "Teacher{" +
                "teachYearExperience=" + teachYearExperience +
                ", teacherResume='" + teacherResume + '\'' +
                ", timeArrangements=" + timeArrangements +
                ", agendas=" + agendas +
                ", completedMeetings=" + completedMeetings +
                ", scheduledMeetings=" + scheduledMeetings +
                ", courseTypeTeach=" + courseTypeTeach +
                ", latestSubmission=" + latestSubmission +
                ", coursesTeach=" + coursesTeach +
                ", profileImageCloudinary='" + profileImageCloudinary + '\'' +
                '}';
    }
}

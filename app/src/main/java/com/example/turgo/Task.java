package com.example.turgo;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task implements Serializable, RequireUpdate<Task,TaskFirebase, TaskRepository> {
    private final FirebaseNode fbn = FirebaseNode.TASK;
    private final Class<TaskFirebase> fbc = TaskFirebase.class;
    private String taskID;
    public static String SERIALIZE_KEY_CODE = "taskObj";
    private static RTDBManager<Task> rtdbManager;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String taskOfCourse;
    private String fromSchedule;
    private LocalDate dateAssigned;
    private String dropbox;
    private boolean manualCompletionRequired;
    private String publisher;
    private ArrayList<String>studentsAssign;
    private transient Course taskOfCourseObject;
    private transient Schedule fromScheduleObject;
    private transient Dropbox dropboxObject;
    private transient Teacher publisherObject;

    public Task(String title, String description, LocalDateTime submissionDate, String taskOfCourse, String fromSchedule, String publisher, ArrayList<String>students){
        this.taskID = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.dueDate = submissionDate;
        this.taskOfCourse = taskOfCourse;
        this.fromSchedule = fromSchedule;
        this.dateAssigned = LocalDate.now();
        this.manualCompletionRequired = !Tool.boolOf(this.dropbox);
        this.publisher = publisher;
        this.studentsAssign = students;

        rtdbManager = new RTDBManager<>();
    }
    public Task(String title, String description, LocalDateTime submissionDate, Course taskOfCourse, Schedule fromSchedule, Teacher publisher, ArrayList<String>students){
        this(
                title,
                description,
                submissionDate,
                taskOfCourse != null ? taskOfCourse.getID() : null,
                fromSchedule != null ? fromSchedule.getID() : null,
                publisher != null ? publisher.getID() : null,
                students
        );
        this.taskOfCourseObject = taskOfCourse;
        this.fromScheduleObject = fromSchedule;
        this.publisherObject = publisher;
    }

    public Task(){
        this.dropbox = null;
        this.manualCompletionRequired = true;
        this.studentsAssign = new ArrayList<>();
        rtdbManager = new RTDBManager<>();
    }

    public com.google.android.gms.tasks.Task<Void> enableDropbox(){
        Dropbox createdDropbox = new Dropbox(this);
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        createdDropbox.setup()
                .addOnSuccessListener(unused -> {
                    this.dropboxObject = createdDropbox;
                    this.dropbox = createdDropbox.getID();
                    this.manualCompletionRequired = false;
                    tcs.setResult(null);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public void submit(file file, Student student, boolean late){
        if (dropboxObject == null) {
            throw new IllegalStateException("Dropbox not loaded for task: " + taskID);
        }
        if (file != null && (file.getOfTask() == null || !Tool.boolOf(file.getOfTask().getID()))) {
            file.setOfTask(this);
        }
        Submission slot = dropboxObject.getSubmissionSlot(student);
        if (slot == null) {
            throw new IllegalStateException("No submission slot for student on task: " + taskID);
        }
        slot.addFile(file, late);
    }

    public com.google.android.gms.tasks.Task<Submission> getSubmissionOfStudent(Student student){
        TaskCompletionSource<Submission>tcs = new TaskCompletionSource<>();
        if(!Tool.boolOf(getDropbox())){
            Log.d("Task", "Dropbox is disabled!");
            return null;
        }
        getDropboxObject().addOnSuccessListener(db ->{
            for (Submission submission : db.getSubmissions()) {
                Student owner = submission.getOf();
                if(owner != null && student != null && Tool.boolOf(owner.getID()) && owner.getID().equals(student.getID())){
                    tcs.setResult(submission);
                    return;
                }
            }
            tcs.setResult(null);
        });
        return tcs.getTask();
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getTaskOfCourse() {
        return taskOfCourse;
    }

    public void setTaskOfCourse(String taskOfCourse) {
        this.taskOfCourse = taskOfCourse;
    }

    public String getFromSchedule() {
        return fromSchedule;
    }

    public void setFromSchedule(String fromSchedule) {
        this.fromSchedule = fromSchedule;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public RTDBManager<Task> getRtdbManager() {
        return rtdbManager;
    }

    public void setRtdbManager(RTDBManager<Task> rtdbManager) {
        this.rtdbManager = rtdbManager;
    }

    public LocalDate getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(LocalDate dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<TaskRepository> getRepositoryClass() {
        return TaskRepository.class;
    }

    @Override
    public Class<TaskFirebase> getFirebaseClass() {
        return fbc;
    }

    public boolean isComplete(Student student){
        if (student == null || dropboxObject == null || dropboxObject.getSubmissions() == null) {
            return false;
        }
        for(Submission submission : dropboxObject.getSubmissions()){
            Student owner = submission.getOf();
            if(owner != null && Tool.boolOf(owner.getID()) && owner.getID().equals(student.getID())){
                if(submission.isCompleted()){
                    return true;
                }
                break;
            }
        }
        return false;
    }

//    public void getStudentsAssigned(ObjectCallBack<ArrayList<Student>>callBack) {
//        try{
//            findAllAggregatedObjects(Student.class, "allTask", callBack);
//        }catch (IllegalAccessException | InstantiationException e) {
//            throw new RuntimeException(e);
//        }
//    }
    public com.google.android.gms.tasks.Task<List<Student>> getStudentAssigned(){
        if (studentsAssign == null || studentsAssign.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }
        List<com.google.android.gms.tasks.Task<Student>> taskList = new ArrayList<>();
        for(String studentId : studentsAssign){
            StudentRepository studentRepository = new StudentRepository(studentId);
            taskList.add(studentRepository.loadAsNormal());
        }
        return Tasks.whenAllSuccess(taskList);
    }


    public String getDropbox() {
        return dropbox;
    }

    public void setDropbox(String dropbox) {
        this.dropbox = dropbox;
        this.manualCompletionRequired = !Tool.boolOf(dropbox);
    }

    public Dropbox getDropboxCached() {
        return dropboxObject;
    }

    public com.google.android.gms.tasks.Task<Dropbox> getDropboxObject() {
        if (dropboxObject != null) {
            return Tasks.forResult(dropboxObject);
        }
        TaskCompletionSource<Dropbox> tcs = new TaskCompletionSource<>();

        if (!Tool.boolOf(dropbox)) {
            // Recovery path for legacy tasks where dropbox field was not written.
            FirebaseDatabase.getInstance()
                    .getReference(FirebaseNode.DROPBOX.getPath())
                    .orderByChild("ofTask")
                    .equalTo(getID())
                    .limitToFirst(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) {
                            tcs.setResult(null);
                            return;
                        }
                        String recoveredDropboxId = null;
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            recoveredDropboxId = child.getKey();
                            break;
                        }
                        if (!Tool.boolOf(recoveredDropboxId)) {
                            tcs.setResult(null);
                            return;
                        }
                        this.dropbox = recoveredDropboxId;
                        this.manualCompletionRequired = false;
                        TaskRepository repository = new TaskRepository(getID());
                        repository.getDbReference().child("dropbox").setValue(recoveredDropboxId);
                        repository.getDbReference().child("manualCompletionRequired").setValue(false);

                        DropboxRepository dropboxRepository = new DropboxRepository(recoveredDropboxId);
                        dropboxRepository.loadAsNormal()
                                .addOnSuccessListener(loaded -> {
                                    dropboxObject = loaded;
                                    tcs.setResult(loaded);
                                })
                                .addOnFailureListener(tcs::setException);
                    })
                    .addOnFailureListener(tcs::setException);
            return tcs.getTask();
        }

        DropboxRepository repository = new DropboxRepository(dropbox);
        repository.loadAsNormal()
                .addOnSuccessListener(loaded -> {
                    dropboxObject = loaded;
                    tcs.setResult(loaded);
                })
                .addOnFailureListener(exception ->{
                    Log.d("Task", "Exception when Loading Dropbox as Normal (249): " + exception);
                    tcs.setException(exception);
                });
        return tcs.getTask();
    }

    public com.google.android.gms.tasks.Task<Course> getTaskOfCourseObject() {
        if (taskOfCourseObject != null) {
            return Tasks.forResult(taskOfCourseObject);
        }
        if (!Tool.boolOf(taskOfCourse)) {
            return Tasks.forResult(null);
        }
        TaskCompletionSource<Course> tcs = new TaskCompletionSource<>();
        CourseRepository repository = new CourseRepository(taskOfCourse);
        repository.loadAsNormal()
                .addOnSuccessListener(loaded -> {
                    taskOfCourseObject = loaded;
                    tcs.setResult(loaded);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public com.google.android.gms.tasks.Task<Schedule> getFromScheduleObject() {
        if (fromScheduleObject != null) {
            return Tasks.forResult(fromScheduleObject);
        }
        if (!Tool.boolOf(fromSchedule)) {
            return Tasks.forResult(null);
        }
        TaskCompletionSource<Schedule> tcs = new TaskCompletionSource<>();
        ScheduleRepository repository = new ScheduleRepository(fromSchedule);
        repository.loadAsNormal()
                .addOnSuccessListener(loaded -> {
                    fromScheduleObject = loaded;
                    tcs.setResult(loaded);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public com.google.android.gms.tasks.Task<Teacher> getPublisherObject() {
        if (publisherObject != null) {
            return Tasks.forResult(publisherObject);
        }
        if (!Tool.boolOf(publisher)) {
            return Tasks.forResult(null);
        }
        TaskCompletionSource<Teacher> tcs = new TaskCompletionSource<>();
        TeacherRepository repository = new TeacherRepository(publisher);
        repository.loadAsNormal()
                .addOnSuccessListener(loaded -> {
                    publisherObject = loaded;
                    tcs.setResult(loaded);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public void setTaskOfCourseObject(Course taskOfCourseObject) {
        this.taskOfCourseObject = taskOfCourseObject;
        this.taskOfCourse = taskOfCourseObject != null ? taskOfCourseObject.getID() : null;
    }

    public void setFromScheduleObject(Schedule fromScheduleObject) {
        this.fromScheduleObject = fromScheduleObject;
        this.fromSchedule = fromScheduleObject != null ? fromScheduleObject.getID() : null;
    }

    public void setPublisherObject(Teacher publisherObject) {
        this.publisherObject = publisherObject;
        this.publisher = publisherObject != null ? publisherObject.getID() : null;
    }

    public void setDropboxObject(Dropbox dropboxObject) {
        this.dropboxObject = dropboxObject;
        this.dropbox = dropboxObject != null ? dropboxObject.getID() : null;
        this.manualCompletionRequired = dropboxObject == null;
    }

    public ArrayList<String> getStudentsAssign() {
        return studentsAssign;
    }

    public void setStudentsAssign(ArrayList<String> studentsAssign) {
        this.studentsAssign = studentsAssign;
    }

    public boolean isManualCompletionRequired() {
        return manualCompletionRequired || !Tool.boolOf(dropbox);
    }

    public void setManualCompletionRequired(boolean manualCompletionRequired) {
        this.manualCompletionRequired = manualCompletionRequired;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskID='" + taskID + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", taskOfCourse='" + taskOfCourse + '\'' +
                ", fromSchedule='" + fromSchedule + '\'' +
                ", dateAssigned=" + dateAssigned +
                ", dropbox='" + dropbox + '\'' +
                ", manualCompletionRequired=" + manualCompletionRequired +
                ", publisher='" + publisher + '\'' +
                ", studentsAssign=" + studentsAssign +
                ", taskOfCourseObject=" + taskOfCourseObject +
                ", fromScheduleObject=" + fromScheduleObject +
                ", dropboxObject=" + dropboxObject +
                ", publisherObject=" + publisherObject +
                '}';
    }

    @Override
    public String getID() {
        return this.taskID;
    }
}

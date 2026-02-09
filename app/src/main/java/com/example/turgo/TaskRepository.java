package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TaskRepository implements RepositoryClass<Task, TaskFirebase> {

    private DatabaseReference taskRef;

    public TaskRepository(String taskId) {
        taskRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.TASK.getPath())
                .child(taskId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return taskRef;
    }

    @Override
    public Class<TaskFirebase> getFbClass() {
        return TaskFirebase.class;
    }

    /* =======================
       BASIC FIELD UPDATES
       ======================= */

    public void updateTitle(String newTitle) {
        taskRef.child("title").setValue(newTitle);
    }

    public void updateDescription(String newDescription) {
        taskRef.child("description").setValue(newDescription);
    }

    public void updateDueDate(String dueDateIso) {
        taskRef.child("dueDate").setValue(dueDateIso);
    }

    public void updateDateAssigned(String dateAssignedIso) {
        taskRef.child("dateAssigned").setValue(dateAssignedIso);
    }

    /* =======================
       RELATION REFERENCES
       ======================= */

    public void updateTaskOfCourse(Course course) {
        taskRef.child("taskOfCourse").setValue(course.getID());
    }

    public void updateFromSchedule(Schedule schedule) {
        taskRef.child("fromSchedule").setValue(schedule.getID());
    }

    public void updatePublisher(Teacher teacher) {
        taskRef.child("publisher").setValue(teacher.getID());
    }

    public void updateDropbox(Dropbox dropbox) {
        taskRef.child("dropbox").setValue(dropbox.getID());
    }

    /* =======================
       STUDENT ASSIGNMENT
       ======================= */

    public void addAssignedStudent(Student student) {
        addStringToArray("studentsAssign", student.getID());
    }

    public void removeAssignedStudent(String studentId) {
        removeStringFromArray("studentsAssign", studentId);
    }
}

package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

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

    @Override
    public com.google.android.gms.tasks.Task<Task> loadAsNormal() {
        TaskCompletionSource<Task> taskSource = new TaskCompletionSource<>();
        String dbKey = taskRef.getKey();
        if (!Tool.boolOf(dbKey)) {
            taskSource.setResult(null);
            return taskSource.getTask();
        }

        taskRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                taskSource.setResult(null);
                return;
            }

            TaskFirebase firebaseObject = snapshot.getValue(TaskFirebase.class);
            if (firebaseObject == null) {
                taskSource.setResult(null);
                return;
            }

            String resolvedId = Tool.boolOf(firebaseObject.getID()) ? firebaseObject.getID() : dbKey;
            try {
                firebaseObject.constructClass(Task.class, resolvedId, new ConstructClassCallback() {
                    @Override
                    public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                        Task loaded = (Task) object;
                        if (!Tool.boolOf(loaded.getTaskID())) {
                            loaded.setTaskID(resolvedId);
                        }
                        taskSource.setResult(loaded);
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        taskSource.setException(error.toException());
                    }
                });
            } catch (Exception e) {
                taskSource.setException(e);
            }
        }).addOnFailureListener(taskSource::setException);

        return taskSource.getTask();
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
        taskRef.child("manualCompletionRequired").setValue(false);
    }

    public void updateManualCompletionRequired(boolean manualCompletionRequired) {
        taskRef.child("manualCompletionRequired").setValue(manualCompletionRequired);
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

    /**
     * Load only the fields the Dashboard task card needs, returning a partial
     * Task built with the empty constructor and only the necessary setters called.
     *
     * Fields loaded: taskID, title, dueDate, dropbox, manualCompletionRequired
     *
     * Avoids constructClass() and the heavy studentsAssign / publisher / schedule
     * reads that the dashboard never uses.
     */
    public com.google.android.gms.tasks.Task<Task> loadPartialForDashboard() {
        com.google.android.gms.tasks.TaskCompletionSource<Task> tcs =
                new com.google.android.gms.tasks.TaskCompletionSource<>();

        loadFields("taskID", "title", "dueDate", "dropbox", "manualCompletionRequired")
                .addOnSuccessListener(m -> {
                    Task task = new Task();   // empty constructor, safe defaults

                    // taskID — fall back to the db key if not stored in the document
                    String id = m.get("taskID") instanceof String ? (String) m.get("taskID") : taskRef.getKey();
                    task.setTaskID(id);

                    task.setTitle(m.get("title") instanceof String ? (String) m.get("title") : null);

                    // dueDate is stored as LocalDateTime.toString() in Firebase
                    Object dueDateRaw = m.get("dueDate");
                    if (dueDateRaw instanceof String && Tool.boolOf((String) dueDateRaw)) {
                        try {
                            task.setDueDate(java.time.LocalDateTime.parse((String) dueDateRaw));
                        } catch (Exception ignored) {
                            task.setDueDate(null);
                        }
                    }

                    Object dropboxRaw = m.get("dropbox");
                    task.setDropbox(dropboxRaw instanceof String ? (String) dropboxRaw : null);

                    Object manualRaw = m.get("manualCompletionRequired");
                    if (manualRaw instanceof Boolean) {
                        task.setManualCompletionRequired((Boolean) manualRaw);
                    }

                    tcs.setResult(task);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }
}

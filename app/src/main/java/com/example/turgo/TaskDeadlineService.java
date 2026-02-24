package com.example.turgo;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TaskDeadlineService {

    private static final String TAG = "TaskDeadlineService";

    private TaskDeadlineService() {
    }

    public static Task<Void> processOverdueNonDropboxTasks() {
        return FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.TASK.getPath())
                .get()
                .onSuccessTask(snapshot -> {
                    List<Task<?>> allUpdates = new ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        String taskId = taskSnapshot.getKey();
                        if (!Tool.boolOf(taskId)) {
                            continue;
                        }
                        TaskFirebase taskFb = taskSnapshot.getValue(TaskFirebase.class);
                        if (!isOverdueNonDropbox(taskFb)) {
                            continue;
                        }
                        if (taskFb.getStudentsAssign() == null || taskFb.getStudentsAssign().isEmpty()) {
                            continue;
                        }

                        for (String studentId : taskFb.getStudentsAssign()) {
                            if (!Tool.boolOf(studentId)) {
                                continue;
                            }
                            StudentRepository sr = new StudentRepository(studentId);
                            allUpdates.add(sr.removeStringFromArrayAsync("uncompletedTask", taskId));
                        }
                    }
                    return allUpdates.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(allUpdates);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed overdue non-dropbox task processing", e));
    }

    private static boolean isOverdueNonDropbox(TaskFirebase taskFb) {
        if (taskFb == null || !Tool.boolOf(taskFb.getDueDate())) {
            return false;
        }
        LocalDateTime dueDate;
        try {
            dueDate = LocalDateTime.parse(taskFb.getDueDate());
        } catch (Exception e) {
            return false;
        }
        boolean nonDropbox = !Tool.boolOf(taskFb.getDropbox());
        boolean manualRequired = taskFb.isManualCompletionRequired() || nonDropbox;
        return manualRequired && dueDate.isBefore(LocalDateTime.now());
    }
}

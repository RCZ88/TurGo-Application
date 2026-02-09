package com.example.turgo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.database.*;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class MeetingProcessorWorker extends Worker {

    private static final int WEEKS_AHEAD = 4;
    private static final String TAG = "MeetingProcessor";

    public MeetingProcessorWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting daily meeting processor at " + LocalDateTime.now());

            // Step 1: Process all active courses
            processAllCourses();

            // Step 2: Cleanup old meetings
            cleanupOldMeetings();

            Log.d(TAG, "Meeting processor completed successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Meeting processor failed: " + e.getMessage(), e);
            return Result.retry(); // Retry on failure
        }
    }

    private void processAllCourses() {
        DatabaseReference coursesRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.COURSE.getPath());

        coursesRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                CourseFirebase courseFirebase = courseSnapshot.getValue(CourseFirebase.class);
                if (courseFirebase != null) {
                    try {
                        courseFirebase.convertToNormal(new ObjectCallBack<>() {
                            @Override
                            public void onObjectRetrieved(Course object) {
                                processCourseMeetings(object);
                            }

                            @Override
                            public void onError(DatabaseError error) {

                            }
                        });
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch courses: " + e.getMessage()));
    }

    private void processCourseMeetings(Course course) {
        ArrayList<Schedule> schedules = course.getSchedules();
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        for (Schedule schedule : schedules) {
            // Generate meetings for next 4 weeks
            for (int week = 0; week < WEEKS_AHEAD; week++) {
                LocalDate meetingDate = today.plusWeeks(week)
                        .with(java.time.temporal.TemporalAdjusters.nextOrSame(schedule.getDay()));

                // Check if this meeting already exists
                String meetingId = generateMeetingId(schedule.getID(), meetingDate);
                checkAndCreateMeeting(schedule, meetingDate, meetingId);
            }
        }
    }

    private void checkAndCreateMeeting(Schedule schedule, LocalDate meetingDate, String meetingId) {
        MeetingRepository meetingRepo = new MeetingRepository(meetingId);

        meetingRepo.load(new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(MeetingFirebase meetingFirebase) {
                if (meetingFirebase == null) {
                    // Meeting doesn't exist - create it
                    createNewMeeting(schedule, meetingDate);
                } else {
                    // Meeting exists - check if alarm assigned
                    Meeting meeting = Await.get(objectCallBack -> {
                        try {
                            meetingFirebase.convertToNormal(objectCallBack);
                        } catch (ParseException | InvocationTargetException |
                                 NoSuchMethodException | IllegalAccessException |
                                 InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    if (!meeting.isAlarmAssigned()) {
                        assignAlarmsToMeeting(meeting, schedule);
                    }
                }
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    private void createNewMeeting(Schedule schedule, LocalDate meetingDate) {


        // Create meeting WITHOUT student (system-generated)
        Meeting meeting = new Meeting(schedule, meetingDate, null, schedule.getID());
        Log.d(TAG, "Creating new meeting: " + meeting.getMeetingID());
        // Save to Firebase
        MeetingRepository meetingRepo = new MeetingRepository(meeting.getID());
        try {
            meetingRepo.save(meeting);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save meeting: " + e.getMessage());
        }
    }

    private void assignAlarmsToMeeting(Meeting meeting, Schedule schedule) {
        // Get all students in this schedule
        schedule.getStudents().addOnSuccessListener(students ->{
            if (students == null || students.isEmpty()) {
                Log.d(TAG, "No students in schedule " + schedule.getID());
                return;
            }

            Log.d(TAG, "Assigning alarms for meeting " + meeting.getMeetingID() +
                    " to " + students.size() + " students");

            // Assign alarm to each student
            meeting.assignAlarmNotification((ArrayList<Student>) students, getApplicationContext());

            // Mark as assigned
            meeting.setAlarmAssigned(true);
            meeting.setAlarmAssignedAt(LocalDateTime.now());

            // Update in Firebase
            MeetingRepository meetingRepo = new MeetingRepository(meeting.getMeetingID());
            try {
                meetingRepo.updateAlarmAssigned(true);
                meetingRepo.updateAlarmAssignedAt(LocalDateTime.now());
            } catch (Exception e) {
                Log.e(TAG, "Failed to update alarm status: " + e.getMessage());
            }
        });

    }

    private void cleanupOldMeetings() {
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);

        DatabaseReference meetingsRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MEETING.getPath());

        meetingsRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot meetingSnapshot : snapshot.getChildren()) {
                MeetingFirebase meetingFb = meetingSnapshot.getValue(MeetingFirebase.class);
                if (meetingFb != null) {
                    Meeting meeting = Await.get(objectCallBack -> {
                        try {
                            meetingFb.convertToNormal(objectCallBack);
                        } catch (ParseException | InvocationTargetException |
                                 NoSuchMethodException | IllegalAccessException |
                                 InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    if (meeting.getDateOfMeeting().isBefore(oneWeekAgo)) {
                        // Delete old meeting
                        meetingSnapshot.getRef().removeValue();
                        Log.d(TAG, "Deleted old meeting: " + meeting.getMeetingID());
                    }
                }
            }
        });
    }

    private String generateMeetingId(String scheduleId, LocalDate date) {
        return scheduleId + "_" + date.toString();
    }
}
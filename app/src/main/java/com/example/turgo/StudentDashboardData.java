package com.example.turgo;

import java.util.ArrayList;

/**
 * Lightweight data holder for the Student Dashboard screen.
 * Contains only the fields this screen actually needs — no full object graphs.
 * Populated by {@link StudentRepository#loadDashboardData()}.
 */
public class StudentDashboardData {
    public double percentageCompleted;
    /**
     * Partially-constructed Task objects (taskID, title, dueDate, dropbox,
     * manualCompletionRequired). Enough to render a task card in the adapter.
     * For anything deeper (submission check, full detail page) use loadAsNormal().
     */
    public ArrayList<Task> uncompletedTasks;
    /** ID of the student's next meeting (null/empty if none). */
    public String nextMeetingId;

    // Upcoming Meeting Display Details (Lite Loaded)
    public String nextCourseName;
    public String nextCourseLogo;
    public String nextCourseDays;
    public String nextTeacherName;
    public String nextRoomTag;
    public String nextMeetingTime; // e.g. "08:00 - 09:00"
    public String nextMeetingDate; // e.g. "2023-10-27"
    public int nextCourseDuration;
    public String nextCourseId;
    /** IDs of schedules completed this week — only the count is shown on Dashboard. */
    public ArrayList<String> scheduleCompletedThisWeekIds;
    /** The week key used to detect stale weekly data. */
    public String completionWeekKey;

    public StudentDashboardData() {
        uncompletedTasks = new ArrayList<>();
        scheduleCompletedThisWeekIds = new ArrayList<>();
    }
}

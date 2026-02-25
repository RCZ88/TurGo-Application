package com.example.turgo;

import java.util.ArrayList;

/**
 * Lightweight DTO for the Teacher Dashboard screen.
 * Holds only the fields required for the dashboard display to avoid full object hydration.
 */
public class TeacherDashboardData {
    // Next Schedule Display (Upcoming)
    public String nextCourseName;
    public String nextScheduleTime;
    public String nextRoomTag;

    // Active Courses List
    public ArrayList<Course> coursesTeach = new ArrayList<>();
    public ArrayList<Meeting> nextMeetingOfCourses = new ArrayList<>();
    public ArrayList<Integer> studentCountOfCourses = new ArrayList<>();

    // Recent Submissions
    public ArrayList<SubmissionDisplay> latestSubmissions = new ArrayList<>();
}

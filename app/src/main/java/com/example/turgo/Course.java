package com.example.turgo;

import java.time.DayOfWeek;
import java.util.ArrayList;

public class Course {
    private CourseType courseType;
    private String courseName;
    private Teacher teacher;
    private ArrayList<Student> students;
    private ArrayList<Schedule> schedules;
    public Course(CourseType courseType, String courseName, Teacher teacher, ArrayList<Schedule>schedules){
        this.courseType = courseType;
        this.courseName = courseName;
        this.teacher = teacher;
        this.schedules = schedules;
        students = new ArrayList<>();
    }
    public void setCourseType(CourseType courseType){ this.courseType = courseType; }

    public CourseType getCourseType(){ return courseType; }

    public void addStudent(Student student){
        students.add(student);
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
}

package com.example.turgo;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class Teacher extends User implements Serializable {
    private ArrayList<Course> coursesTeach;
    private ArrayList<String> courseTypeTeach;
    public Teacher(String fullName, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super("TEACHER", fullName, birthDate, nickname, email, phoneNumber, "teaObj");
        coursesTeach = new ArrayList<>();
        courseTypeTeach = new ArrayList<>();
    }
    public Teacher(){}

    public void addCourse(Course course){
        coursesTeach.add(course);
    }
    public ArrayList<Course> getCoursesTeach(){
        return coursesTeach;
    }

    public void addCourseTeach(String courseType){
        courseTypeTeach.add(courseType);
    }

    public ArrayList<String> getCourseTypeTeach() {
        return courseTypeTeach;
    }


    @Override
    public String toString() {
        return super.toString() + "Teacher{" +
                "courseTypeTeach=" + courseTypeTeach +
                '}';
    }
}

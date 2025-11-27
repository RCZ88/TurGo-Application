package com.example.turgo;

import android.util.Log;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class TeacherFirebase extends UserFirebase implements FirebaseClass<Teacher> {
    // Firebase-compatible fields

    private String profileImageCloudinary; // Store as profileImageCloudinary instead of Drawable
    private ArrayList<String> coursesTeach; // Store course IDs instead of Course objects
    private ArrayList<String> courseTypeTeach; // Already strings, keep as is
    private ArrayList<String> scheduledMeetings; // Store meeting IDs instead of Meeting objects
    private ArrayList<String> agendas; // Store agenda IDs instead of Agenda objects
    private ArrayList<String> timeArrangements; // Store arrangement IDs instead of DayTimeArrangement objects
    private String teacherResume;
    private int teachYearExperience;

    // Default constructor required for Firebase
    public TeacherFirebase() {
        super(UserType.TEACHER.type());
        // Initialize empty lists
        coursesTeach = new ArrayList<>();
        courseTypeTeach = new ArrayList<>();
        scheduledMeetings = new ArrayList<>();
        agendas = new ArrayList<>();
        timeArrangements = new ArrayList<>();
    }

    @Override
    public void importObjectData(Teacher from) {
        setID(from.getID());
        setFullName(from.getFullName());
        setNickname(from.getNickname());
        setBirthDate(from.getBirthDate());
        setAge(from.getAge());
        setEmail(from.getEmail());
        setGender(from.getGender());
        setPhoneNumber(from.getPhoneNumber());
        setLanguage(from.getLanguage().getDisplayName());
        setTheme(from.getTheme().getTheme());

        if (from.getProfileImage() != null) {
            profileImageCloudinary = from.getProfileImage();
        } else {
            profileImageCloudinary = ""; // or default placeholder path
        }

        if (from.getCoursesTeach() != null) {
            coursesTeach = convertToIdList(from.getCoursesTeach());
        } else {
            coursesTeach = new ArrayList<>();
        }

        if (from.getCourseTypeTeach() != null) {
            courseTypeTeach = from.getCourseTypeTeach();
        } else {
            courseTypeTeach = new ArrayList<>();
        }

        if (from.getScheduledMeetings() != null) {
            scheduledMeetings = convertToIdList(from.getScheduledMeetings());
        } else {
            scheduledMeetings = new ArrayList<>();
        }

        if (from.getAgendas() != null) {
            agendas = convertToIdList(from.getAgendas());
        } else {
            agendas = new ArrayList<>();
        }

        if (from.getTimeArrangements() != null) {
            timeArrangements = convertToIdList(from.getTimeArrangements());
        } else {
            timeArrangements = new ArrayList<>();
        }

        if (from.getTeacherResume() != null) {
            teacherResume = from.getTeacherResume();
        } else {
            teacherResume = "";
        }

        teachYearExperience = from.getTeachYearExperience();
    }

//
//    public String getPfpCloudinary() {
//        return pfpCloudinary;
//    }
//
//    public void setPfpCloudinary(String pfpCloudinary) {
//        this.pfpCloudinary = pfpCloudinary;
//    }

    @Override
    public String getID() {
        return super.getID();
    }
    @Override
    public void convertToNormal(ObjectCallBack<Teacher> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Teacher.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Teacher) object);
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e("Error", error.getMessage());
            }
        });
    }



    public String getProfileImageCloudinary() {
        return profileImageCloudinary;
    }

    public void setProfileImageCloudinary(String profileImageCloudinary) {
        this.profileImageCloudinary = profileImageCloudinary;
    }

    public ArrayList<String> getCoursesTeach() {
        return coursesTeach;
    }

    public void setCoursesTeach(ArrayList<String> coursesTeach) {
        this.coursesTeach = coursesTeach;
    }

    public ArrayList<String> getCourseTypeTeach() {
        return courseTypeTeach;
    }

    public void setCourseTypeTeach(ArrayList<String> courseTypeTeach) {
        this.courseTypeTeach = courseTypeTeach;
    }

    public ArrayList<String> getScheduledMeetings() {
        return scheduledMeetings;
    }

    public void setScheduledMeetings(ArrayList<String> scheduledMeetings) {
        this.scheduledMeetings = scheduledMeetings;
    }

    public ArrayList<String> getAgendas() {
        return agendas;
    }

    public void setAgendas(ArrayList<String> agendas) {
        this.agendas = agendas;
    }

    public ArrayList<String> getTimeArrangements() {
        return timeArrangements;
    }

    public void setTimeArrangements(ArrayList<String> timeArrangements) {
        this.timeArrangements = timeArrangements;
    }

    public String getTeacherResume() {
        return teacherResume;
    }

    public void setTeacherResume(String teacherResume) {
        this.teacherResume = teacherResume;
    }

    public int getTeachYearExperience() {
        return teachYearExperience;
    }

    public void setTeachYearExperience(int teachYearExperience) {
        this.teachYearExperience = teachYearExperience;
    }
    public TeacherMini toTM(){
        ArrayList<String>courseNames = new ArrayList<>();
        for(String courseIDs : coursesTeach){
            (new Course()).retrieveOnce(new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(CourseFirebase object) {
                    courseNames.add(object.getCourseName());
                }

                @Override
                public void onError(DatabaseError error) {

                }
            }, courseIDs);
        }
        String courseTeach = "";
        if(!courseNames.isEmpty()){
            String.join(", ", courseNames);
        }else{
            courseTeach = "None";
        }
        return new TeacherMini(this.getFullName(), courseTeach, profileImageCloudinary, super.getID());
    }

}
package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class TeacherFirebase extends UserFirebase implements FirebaseClass<Teacher> {
    // Firebase-compatible fields

    private String profileImageID; // Store as profileImageID instead of Drawable
    private ArrayList<String> coursesTeachIds; // Store course IDs instead of Course objects
    private ArrayList<String> courseTypeTeach; // Already strings, keep as is
    private ArrayList<String> scheduledMeetingsIds; // Store meeting IDs instead of Meeting objects
    private ArrayList<String> agendasIds; // Store agenda IDs instead of Agenda objects
    private ArrayList<String> availableTimesIds; // Store arrangement IDs instead of DayTimeArrangement objects
    private String teacherResume;
    private int teachYearExperience;
    private String pfpCloudinary;

    // Default constructor required for Firebase
    public TeacherFirebase() {
        super(UserType.TEACHER.type());
        // Initialize empty lists
        coursesTeachIds = new ArrayList<>();
        courseTypeTeach = new ArrayList<>();
        scheduledMeetingsIds = new ArrayList<>();
        agendasIds = new ArrayList<>();
        availableTimesIds = new ArrayList<>();
    }

    @Override
    public void importObjectData(Teacher from) {
        setID(from.getID());
        setFullName(from.getFullName());
        setNickname(from.getNickname());
        setBirthdate(from.getBirthDate());
        setAge(from.getAge());
        setEmail(from.getEmail());
        setGender(from.getGender());
        setPhoneNumber(from.getPhoneNumber());
        setLanguangeID(from.getLanguage().getDisplayName());
        setTheme(from.getTheme().getTheme());
        setPfpCloudinary(from.getPfpCloudinary());

        // Convert profile image Drawable to path/string if available
        if (from.getProfileImage() != null) {
            // This would need implementation based on how you store images
            // For now, we'll assume a method to convert Drawable to path
            profileImageID = from.getProfileImage();
        }

        // Convert ArrayList<Course> to ArrayList<String> of IDs
        if (from.getCoursesTeach() != null) {
            coursesTeachIds = convertToIdList(from.getCoursesTeach());
        }

        // Copy course types directly (already strings)
        if (from.getCourseTypeTeach() != null) {
            courseTypeTeach = from.getCourseTypeTeach();
        }

        // Convert ArrayList<Meeting> to ArrayList<String> of IDs
        if (from.getScheduledMeetings() != null) {
            scheduledMeetingsIds = convertToIdList(from.getScheduledMeetings());
        }

        // Convert ArrayList<Agenda> to ArrayList<String> of IDs
        if (from.getAgendas() != null) {
            agendasIds = convertToIdList(from.getAgendas());
        }

        // Convert ArrayList<DayTimeArrangement> to ArrayList<String> of IDs
        if (from.getTimeArrangements() != null) {
            availableTimesIds = convertToIdList(from.getTimeArrangements());
        }

        // Copy primitive fields directly
        teacherResume = from.getTeacherResume();
        teachYearExperience = from.getTeachYearExperience();
    }

    public String getPfpCloudinary() {
        return pfpCloudinary;
    }

    public void setPfpCloudinary(String pfpCloudinary) {
        this.pfpCloudinary = pfpCloudinary;
    }

    @Override
    public String getID() {
        return super.getID();
    }

    @Override
    public Teacher convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (Teacher) constructClass(Teacher.class, super.getID());
    }

    public String getProfileImageID() {
        return profileImageID;
    }

    public void setProfileImageID(String profileImageID) {
        this.profileImageID = profileImageID;
    }

    public ArrayList<String> getCoursesTeachIds() {
        return coursesTeachIds;
    }

    public void setCoursesTeachIds(ArrayList<String> coursesTeachIds) {
        this.coursesTeachIds = coursesTeachIds;
    }

    public ArrayList<String> getCourseTypeTeach() {
        return courseTypeTeach;
    }

    public void setCourseTypeTeach(ArrayList<String> courseTypeTeach) {
        this.courseTypeTeach = courseTypeTeach;
    }

    public ArrayList<String> getScheduledMeetingsIds() {
        return scheduledMeetingsIds;
    }

    public void setScheduledMeetingsIds(ArrayList<String> scheduledMeetingsIds) {
        this.scheduledMeetingsIds = scheduledMeetingsIds;
    }

    public ArrayList<String> getAgendasIds() {
        return agendasIds;
    }

    public void setAgendasIds(ArrayList<String> agendasIds) {
        this.agendasIds = agendasIds;
    }

    public ArrayList<String> getAvailableTimesIds() {
        return availableTimesIds;
    }

    public void setAvailableTimesIds(ArrayList<String> availableTimesIds) {
        this.availableTimesIds = availableTimesIds;
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
        for(String courseIDs : coursesTeachIds){
            (new Course()).retrieveOnce(new ObjectCallBack<CourseFirebase>() {
                @Override
                public void onObjectRetrieved(CourseFirebase object) {
                    courseNames.add(object.getCourseName());
                }

                @Override
                public void onError(DatabaseError error) {

                }
            }, courseIDs);
        }
        return new TeacherMini(this.getFullName(), String.join(", ", courseNames), pfpCloudinary, super.getID());
    }

}
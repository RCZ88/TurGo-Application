package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CourseTypeRepository implements RepositoryClass<CourseType, CourseTypeFirebase> {

    private DatabaseReference courseTypeRef;

    public CourseTypeRepository(String courseTypeId) {
        courseTypeRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.COURSE_TYPE.getPath())
                .child(courseTypeId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return courseTypeRef;
    }

    @Override
    public Class<CourseTypeFirebase> getFbClass() {
        return CourseTypeFirebase.class;
    }

    /* =======================
       FIELD UPDATES
       ======================= */

    public void updateCourseTypeName(String newCourseType) {
        courseTypeRef.child("courseType").setValue(newCourseType);
    }
}

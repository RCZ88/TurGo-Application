package com.example.turgo;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class OneTimeDtaCleanup {
    private static final String TAG = "OneTimeDtaCleanup";

    private OneTimeDtaCleanup() {}

    public static final class Options {
        public boolean deleteMalformedDtas = true;
        public boolean pruneTeacherReferences = true;
        public boolean pruneCourseReferences = true;
        public boolean reconstructMissingDtas = true;
        public String onlyTeacherId = null;
    }

    public static final class Report {
        public int totalDtaNodes;
        public int validDtaNodes;
        public int malformedDtaNodes;
        public int deletedMalformedDtaNodes;
        public int reconstructedDtaNodes;
        public int occupiedSyncedDtaNodes;
        public int teachersScanned;
        public int teachersUpdated;
        public int teacherRefsRemoved;
        public int coursesScanned;
        public int coursesUpdated;
        public int courseRefsRemoved;
        public final ArrayList<String> malformedDtaIds = new ArrayList<>();
        public final ArrayList<String> removedTeacherRefs = new ArrayList<>();
        public final ArrayList<String> removedCourseRefs = new ArrayList<>();
    }

    public static Task<Report> run(Options options) {
        TaskCompletionSource<Report> tcs = new TaskCompletionSource<>();
        Options safeOptions = options != null ? options : new Options();

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dtaRef = root.child(FirebaseNode.DTA.getPath());
        DatabaseReference teacherRef = root.child(FirebaseNode.TEACHER.getPath());
        DatabaseReference courseRef = root.child(FirebaseNode.COURSE.getPath());
        DatabaseReference scheduleRef = root.child(FirebaseNode.SCHEDULE.getPath());

        dtaRef.get().addOnSuccessListener(dtaSnapshot -> {
            Report report = new Report();
            Map<String, Object> updates = new HashMap<>();
            Set<String> validDtaIds = new LinkedHashSet<>();
            Map<String, DataSnapshot> validDtaNodes = new HashMap<>();

            for (DataSnapshot dtaChild : dtaSnapshot.getChildren()) {
                String dtaId = dtaChild.getKey();
                if (!Tool.boolOf(dtaId)) {
                    continue;
                }
                report.totalDtaNodes++;

                boolean valid = isValidDtaNode(dtaChild);
                if (valid) {
                    report.validDtaNodes++;
                    validDtaIds.add(dtaId);
                    validDtaNodes.put(dtaId, dtaChild);

                    // Keep both aliases consistent.
                    String fbDtaId = dtaChild.child("DTA_ID").getValue(String.class);
                    String fbId = dtaChild.child("id").getValue(String.class);
                    if (!dtaId.equals(fbDtaId)) {
                        updates.put(FirebaseNode.DTA.getPath() + "/" + dtaId + "/DTA_ID", dtaId);
                    }
                    if (!dtaId.equals(fbId)) {
                        updates.put(FirebaseNode.DTA.getPath() + "/" + dtaId + "/id", dtaId);
                    }
                } else {
                    report.malformedDtaNodes++;
                    report.malformedDtaIds.add(dtaId);
                    if (safeOptions.deleteMalformedDtas) {
                        updates.put(FirebaseNode.DTA.getPath() + "/" + dtaId, null);
                        report.deletedMalformedDtaNodes++;
                    }
                }
            }

            teacherRef.get().addOnSuccessListener(teacherSnapshot -> {
                courseRef.get().addOnSuccessListener(courseSnapshot -> {
                    scheduleRef.get().addOnSuccessListener(scheduleSnapshot -> {
                        Map<String, List<String>> teacherRefsByTeacher = new HashMap<>();
                        for (DataSnapshot teacherChild : teacherSnapshot.getChildren()) {
                            String teacherId = teacherChild.getKey();
                            if (!Tool.boolOf(teacherId)) {
                                continue;
                            }
                            teacherRefsByTeacher.put(teacherId, readStringList(teacherChild.child("timeArrangements")));
                        }

                        Map<String, List<String>> courseRefsByCourse = new HashMap<>();
                        Map<String, String> teacherByCourse = new HashMap<>();
                        for (DataSnapshot courseChild : courseSnapshot.getChildren()) {
                            String courseId = courseChild.getKey();
                            if (!Tool.boolOf(courseId)) {
                                continue;
                            }
                            courseRefsByCourse.put(courseId, readStringList(courseChild.child("dayTimeArrangement")));
                            teacherByCourse.put(courseId, courseChild.child("teacher").getValue(String.class));
                        }

                        if (safeOptions.reconstructMissingDtas) {
                            Map<String, DayBucket> scheduleBuckets = buildScheduleBuckets(scheduleSnapshot);

                            for (Map.Entry<String, DayBucket> entry : scheduleBuckets.entrySet()) {
                                String key = entry.getKey(); // courseId|DAY
                                DayBucket bucket = entry.getValue();
                                String courseId = bucket.courseId;
                                String teacherId = teacherByCourse.get(courseId);
                                if (!Tool.boolOf(courseId) || !Tool.boolOf(teacherId)) {
                                    continue;
                                }
                                if (Tool.boolOf(safeOptions.onlyTeacherId) && !safeOptions.onlyTeacherId.equals(teacherId)) {
                                    continue;
                                }

                                List<String> courseRefs = courseRefsByCourse.computeIfAbsent(courseId, k -> new ArrayList<>());
                                List<String> teacherRefs = teacherRefsByTeacher.computeIfAbsent(teacherId, k -> new ArrayList<>());

                                String matchedDtaId = findMatchingDtaId(validDtaNodes, validDtaIds, courseRefs, courseId, bucket.day);

                                if (!Tool.boolOf(matchedDtaId)) {
                                    matchedDtaId = UUID.randomUUID().toString();
                                    Map<String, Object> newDta = new HashMap<>();
                                    newDta.put("DTA_ID", matchedDtaId);
                                    newDta.put("id", matchedDtaId);
                                    newDta.put("atCourse", courseId);
                                    newDta.put("day", bucket.day);
                                    newDta.put("start", bucket.start.toString());
                                    newDta.put("end", bucket.end.toString());
                                    newDta.put("maxMeeting", Math.max(1, bucket.scheduleIds.size()));
                                    newDta.put("occupied", bucket.scheduleIds);
                                    updates.put(FirebaseNode.DTA.getPath() + "/" + matchedDtaId, newDta);
                                    validDtaIds.add(matchedDtaId);
                                    report.reconstructedDtaNodes++;
                                } else {
                                    DataSnapshot existingNode = validDtaNodes.get(matchedDtaId);
                                    List<String> existingOccupied = existingNode != null
                                            ? readStringList(existingNode.child("occupied"))
                                            : new ArrayList<>();
                                    Set<String> merged = new LinkedHashSet<>(existingOccupied);
                                    merged.addAll(bucket.scheduleIds);
                                    if (!existingOccupied.equals(new ArrayList<>(merged))) {
                                        updates.put(FirebaseNode.DTA.getPath() + "/" + matchedDtaId + "/occupied", new ArrayList<>(merged));
                                        report.occupiedSyncedDtaNodes++;
                                    }
                                }

                                if (!courseRefs.contains(matchedDtaId)) {
                                    courseRefs.add(matchedDtaId);
                                }
                                if (!teacherRefs.contains(matchedDtaId)) {
                                    teacherRefs.add(matchedDtaId);
                                }
                            }
                        }

                        if (safeOptions.pruneTeacherReferences) {
                            for (Map.Entry<String, List<String>> teacherEntry : teacherRefsByTeacher.entrySet()) {
                                String teacherId = teacherEntry.getKey();
                                if (!Tool.boolOf(teacherId)) {
                                    continue;
                                }
                                if (Tool.boolOf(safeOptions.onlyTeacherId) && !safeOptions.onlyTeacherId.equals(teacherId)) {
                                    continue;
                                }
                                report.teachersScanned++;
                                List<String> original = teacherEntry.getValue();
                                List<String> cleaned = cleanRefList(original, validDtaIds, refId -> {
                                    report.teacherRefsRemoved++;
                                    report.removedTeacherRefs.add(teacherId + ":" + refId);
                                });
                                if (!original.equals(cleaned)) {
                                    updates.put(FirebaseNode.TEACHER.getPath() + "/" + teacherId + "/timeArrangements", cleaned);
                                    report.teachersUpdated++;
                                }
                            }
                        }

                        if (safeOptions.pruneCourseReferences) {
                            for (Map.Entry<String, List<String>> courseEntry : courseRefsByCourse.entrySet()) {
                                String courseId = courseEntry.getKey();
                                if (!Tool.boolOf(courseId)) {
                                    continue;
                                }
                                String teacherId = teacherByCourse.get(courseId);
                                if (Tool.boolOf(safeOptions.onlyTeacherId) && !safeOptions.onlyTeacherId.equals(teacherId)) {
                                    continue;
                                }
                                report.coursesScanned++;
                                List<String> original = courseEntry.getValue();
                                List<String> cleaned = cleanRefList(original, validDtaIds, refId -> {
                                    report.courseRefsRemoved++;
                                    report.removedCourseRefs.add(courseId + ":" + refId);
                                });
                                if (!original.equals(cleaned)) {
                                    updates.put(FirebaseNode.COURSE.getPath() + "/" + courseId + "/dayTimeArrangement", cleaned);
                                    report.coursesUpdated++;
                                }
                            }
                        }

                        if (updates.isEmpty()) {
                            Log.d(TAG, "No cleanup updates required.");
                            tcs.setResult(report);
                            return;
                        }

                        root.updateChildren(updates)
                                .addOnSuccessListener(unused -> {
                                    Log.d(TAG, "Cleanup applied. updates=" + updates.size());
                                    tcs.setResult(report);
                                })
                                .addOnFailureListener(tcs::setException);
                    }).addOnFailureListener(tcs::setException);
                }).addOnFailureListener(tcs::setException);
            }).addOnFailureListener(tcs::setException);
        }).addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    public static Task<Report> runForTeacher(String teacherId) {
        Options options = new Options();
        options.onlyTeacherId = teacherId;
        return run(options);
    }

    private static boolean isValidDtaNode(DataSnapshot dtaNode) {
        String day = dtaNode.child("day").getValue(String.class);
        String start = dtaNode.child("start").getValue(String.class);
        String end = dtaNode.child("end").getValue(String.class);

        if (!Tool.boolOf(day) || !Tool.boolOf(start) || !Tool.boolOf(end)) {
            return false;
        }
        try {
            java.time.DayOfWeek.valueOf(day);
            java.time.LocalTime.parse(start);
            java.time.LocalTime.parse(end);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static List<String> readStringList(DataSnapshot listNode) {
        ArrayList<String> result = new ArrayList<>();
        if (listNode == null || !listNode.exists()) {
            return result;
        }
        for (DataSnapshot item : listNode.getChildren()) {
            String value = item.getValue(String.class);
            if (Tool.boolOf(value)) {
                result.add(value);
            }
        }
        return result;
    }

    private interface RemovedRefCallback {
        void onRemoved(String refId);
    }

    private static List<String> cleanRefList(List<String> original, Set<String> validDtaIds, RemovedRefCallback removedRefCallback) {
        ArrayList<String> cleaned = new ArrayList<>();
        Set<String> dedupe = new LinkedHashSet<>();
        for (String refId : original) {
            if (!validDtaIds.contains(refId)) {
                removedRefCallback.onRemoved(refId);
                continue;
            }
            if (dedupe.add(refId)) {
                cleaned.add(refId);
            }
        }
        return cleaned;
    }

    private static final class DayBucket {
        String courseId;
        String day;
        LocalTime start;
        LocalTime end;
        ArrayList<String> scheduleIds = new ArrayList<>();
    }

    private static Map<String, DayBucket> buildScheduleBuckets(DataSnapshot scheduleSnapshot) {
        Map<String, DayBucket> buckets = new HashMap<>();
        for (DataSnapshot scheduleChild : scheduleSnapshot.getChildren()) {
            String scheduleId = scheduleChild.getKey();
            String courseId = scheduleChild.child("ofCourse").getValue(String.class);
            String day = scheduleChild.child("day").getValue(String.class);
            String startStr = scheduleChild.child("meetingStart").getValue(String.class);
            String endStr = scheduleChild.child("meetingEnd").getValue(String.class);

            if (!Tool.boolOf(scheduleId) || !Tool.boolOf(courseId) || !Tool.boolOf(day) || !Tool.boolOf(startStr) || !Tool.boolOf(endStr)) {
                continue;
            }
            try {
                DayOfWeek.valueOf(day);
                LocalTime start = LocalTime.parse(startStr);
                LocalTime end = LocalTime.parse(endStr);
                String key = courseId + "|" + day;
                DayBucket bucket = buckets.get(key);
                if (bucket == null) {
                    bucket = new DayBucket();
                    bucket.courseId = courseId;
                    bucket.day = day;
                    bucket.start = start;
                    bucket.end = end;
                    buckets.put(key, bucket);
                } else {
                    if (start.isBefore(bucket.start)) {
                        bucket.start = start;
                    }
                    if (end.isAfter(bucket.end)) {
                        bucket.end = end;
                    }
                }
                if (!bucket.scheduleIds.contains(scheduleId)) {
                    bucket.scheduleIds.add(scheduleId);
                }
            } catch (Exception ignored) {
            }
        }
        return buckets;
    }

    private static String findMatchingDtaId(Map<String, DataSnapshot> validDtaNodes,
                                            Set<String> validDtaIds,
                                            List<String> courseRefs,
                                            String courseId,
                                            String day) {
        // Prefer an already-linked course DTA with matching course/day.
        for (String refId : courseRefs) {
            if (!validDtaIds.contains(refId)) {
                continue;
            }
            DataSnapshot dtaNode = validDtaNodes.get(refId);
            if (dtaNode == null) {
                continue;
            }
            String atCourse = dtaNode.child("atCourse").getValue(String.class);
            String dtaDay = dtaNode.child("day").getValue(String.class);
            if (courseId.equals(atCourse) && day.equals(dtaDay)) {
                return refId;
            }
        }

        // Fallback: any valid DTA matching course/day.
        for (Map.Entry<String, DataSnapshot> entry : validDtaNodes.entrySet()) {
            DataSnapshot dtaNode = entry.getValue();
            String atCourse = dtaNode.child("atCourse").getValue(String.class);
            String dtaDay = dtaNode.child("day").getValue(String.class);
            if (courseId.equals(atCourse) && day.equals(dtaDay)) {
                return entry.getKey();
            }
        }

        return null;
    }
}

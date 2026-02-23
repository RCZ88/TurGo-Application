package com.example.turgo;

import android.util.Log;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

public class DayTimeArrangement implements Serializable, RequireUpdate<DayTimeArrangement,DTAFirebase, DTARepository>{
    //exclusively new schedule with 1 people ony

    private final FirebaseNode fbn = FirebaseNode.DTA;
    private final Class<DTAFirebase>fbc = DTAFirebase.class;
    private final String DTA_ID;
    private DayOfWeek day;
    private LocalTime start;//start teaching
    private LocalTime end;//end teaching for the day
    private ArrayList<Schedule> occupied;
    private int maxMeeting;

    public DayTimeArrangement(){
        DTA_ID = UUID.randomUUID().toString();
        occupied = new ArrayList<>();
    }

    public DayTimeArrangement(Course atCourse, DayOfWeek day, LocalTime start, LocalTime end, int maxMeeting) {
        this.day = day;
        this.start = start;
        this.end = end;
        if(atCourse != null && atCourse.getSchedules() != null){
            occupied = atCourse.getScheduleOfDay(day);
        }else{
            occupied = new ArrayList<>();
        }

        this.maxMeeting = maxMeeting;
        DTA_ID = UUID.randomUUID().toString();
    }
    public int getDuration(){
        return Math.toIntExact(Duration.between(start, end).toMinutes());
    }
    public ArrayList<TimeSlot> findFreeSlots(ScheduleQuality sq, int maxPeople, int duration){
        String log = "findFreeSlots";
        Log.d(log, "Duration: "+ duration);
        int modifierMinutesMultiplier = 15;
        int maxSlide = duration/modifierMinutesMultiplier ;

        ArrayList<TimeSlot> freeSlots = new ArrayList<>();
        if(occupied == null){
            occupied = new ArrayList<>();
        }
        occupied.sort(Comparator.comparing(Schedule::getMeetingStart));

        int currentModifier;
        if(occupied == null || occupied.isEmpty()){
            for(int i = 0; i< maxSlide ; i++){
                currentModifier = i * modifierMinutesMultiplier;
                freeSlots.addAll(getSlotsForGivenTimePeriod(duration, currentModifier));
            }

        }else{
            if(sq == ScheduleQuality.PRIVATE_ONLY){
                for(int i = 0; i< maxSlide ; i++){
                    currentModifier = i * modifierMinutesMultiplier;
                    Log.d("findFreeSlots", "Finding for Offset: " + currentModifier);
                    Schedule firstSchedule = occupied.get(0);
                    if(firstSchedule.getMeetingStart().isAfter(start)){
                        freeSlots.addAll(getSlotsForGivenTimePeriod(duration, currentModifier));
                    }

                    for (int j = 0; j < occupied.size()-1; j++) {
                        Schedule schedule = occupied.get(j);
                        Schedule nextSchedule = occupied.get(j+1);
                        if(nextSchedule.getMeetingStart().isBefore(schedule.getMeetingEnd())){
                            continue;
                        }
                        freeSlots.addAll(getSlotsForGivenTimePeriod(duration, currentModifier));
                    }

                    Schedule finalSchedule = occupied.get(occupied.size() - 1);
                    if(finalSchedule.getMeetingEnd().isBefore(end)){
                        freeSlots.addAll(getSlotsForGivenTimePeriod(duration, currentModifier));
                    }
                }

            }else if(sq == ScheduleQuality.FLEXIBLE){
                for(int i = 0; i< maxSlide ; i++){
                    currentModifier = i * modifierMinutesMultiplier;
                    Log.d("findFreeSlots", "Finding for Offset: " + i * modifierMinutesMultiplier);
                    freeSlots.addAll(getSlotsForGivenTimePeriod(duration, currentModifier));

                }

                if(!occupied.isEmpty()){
                    for(TimeSlot ts : freeSlots){
                        for(Schedule schedule :occupied){
                            if(Tool.isOverlapping(ts.getStart(), ts.getEnd(), schedule.getMeetingStart(), schedule.getMeetingEnd())){
                                ts.getSchedules().add(schedule);
                                if(schedule.isPrivate() || schedule.getNumberOfStudents() >=maxPeople){
                                    freeSlots.remove(ts);
                                }
                            }
                        }
                    }
                }
            }else if(sq == ScheduleQuality.GROUP_ONLY){
                ArrayList<LocalTime>startExisted = new ArrayList<>();
                ArrayList<Schedule>uniqueOccupied = new ArrayList<>();
                for(Schedule schedule : occupied){
                    if(!startExisted.contains(schedule.getMeetingStart())) {
                        startExisted.add(schedule.getMeetingStart());
                        uniqueOccupied.add(schedule);
                    }
                }
                for(int i = 0; i<maxSlide ; i++){
                    currentModifier = i * modifierMinutesMultiplier;
                    Log.d("findFreeSlots", "Finding for Offset: " + i * currentModifier);
                    for(Schedule schedule : uniqueOccupied){
                        ArrayList<TimeSlot>timeSlots = getSlotsForGivenTimePeriod(duration, currentModifier);
                        timeSlots.forEach((ts)->{
                            ts.getSchedules().add(schedule);
                        });
                        freeSlots.addAll(timeSlots);
                    }
                }
            }
        }

        String str = String.join("}, {", Tool.streamToArray(freeSlots.stream().map(TimeSlot::toString)));
        str = "{"+ str + "}";
        Log.d("FilterFullDays", "Free Slots (BEFORE FILTER SAME START TIME): " + str);

        ArrayList<LocalTime>startTime = new ArrayList<>();

        ArrayList<TimeSlot> copy = new ArrayList<>(freeSlots);
        for (TimeSlot ts : copy) {  // Iterate copy
            if (startTime.contains(ts.getStart())) {
                Log.d("findFreeSlot", "Found Same StartTime of: " + ts);
                freeSlots.remove(ts);  // Safe on original
            } else {
                startTime.add(ts.getStart());
            }
        }
        return freeSlots;
    }
    private ArrayList<TimeSlot> getSlotsForGivenTimePeriod(int duration, int minuteModifier){
        LocalTime minCopy = start.plusMinutes(minuteModifier);
        LocalTime maxCopy = end.plusMinutes(minuteModifier);
        int durationAvail = Math.abs((int)Duration.between(maxCopy, minCopy).toMinutes());
        String log = "findFreeSlots";
        Log.d(log, "Duration Available: " + durationAvail);

        ArrayList<TimeSlot>slots = new ArrayList<>();
        if(durationAvail >= duration){
            int maxAmount =  (int)Math.floor((double) durationAvail/duration);
            Log.d(log, "Max Amount of Meeting: " + maxAmount);
            for(int i = 0; i <maxAmount; i++){
                LocalTime slotStart = minCopy.plusMinutes((long) i * duration);
                LocalTime slotEnd = slotStart.plusMinutes(duration);  // Calculate from start!
                TimeSlot ts = new TimeSlot(day, slotStart, slotEnd, minuteModifier);
                slots.add(ts);
            }
        }

        return slots;
    }

    public static ArrayList<String> getDaysOfDtas(ArrayList<DayTimeArrangement>schedules){
        return schedules.stream().map(dta -> dta.getDay().toString()).collect(Collectors.toCollection(ArrayList::new));
    }
    public static ArrayList<String> getTimesOfDtas(ArrayList<DayTimeArrangement>dtas){
        return dtas.stream().map(dta -> dta.getStart().toString() + " - " + dta.getEnd().toString()).collect(Collectors.toCollection(ArrayList::new));
    }

    //FOR IF CUSTOM INPUT OWN TIME.
//    public void applySchedule(LocalTime start, int duration, Student student, boolean paymentPreferences, int cost){//private
//        ArrayList<TimeSlot> freeSlots = findFreeSlots(true, 1, duration);
//        for(TimeSlot ts : freeSlots){
//            if(isTimeEqualOrBetween(start, start.plus(Duration.ofMinutes(duration)), ts.getStart(), ts.getEnd())){
//                Schedule schedule = new Schedule(atCourse, start, duration ,day, Room.getEmptyRoom(start, start.plus(Duration.ofMinutes(duration)), day), true);
//                schedule.addStudent(student);
//                atCourse.addSchedule(schedule);
//                occupied.add(schedule);
//                break;
//            }
//        }
//    }
//
//    public void applySchedule(LocalTime start, int duration, int maxPeople, Student student, boolean paymentPreferences, int cost){
//        ArrayList<TimeSlot>freeSlots = findFreeSlots(false, maxPeople, duration);
//        for(TimeSlot timeSlot : freeSlots){
//            if(isTimeEqualOrBetween(start, start.plus(Duration.ofMinutes(duration)), timeSlot.getStart(), timeSlot.getEnd())){
//                boolean found = false;
//                for(Schedule schedule : occupied){
//                    if(!(schedule.isPrivate()) && schedule.getMeetingStart().equals(start)){
//                        System.out.println("Schedule found! Adding student...");
//                        schedule.addStudent(student);
//                        System.out.println("Student Successfully Added");
//                        found = true;
//                        break;
//                    }
//                }
//                if(!found){
//                    System.out.println("No Existing Schedule found of that Time! Creating a new Schedule");
//                    Schedule newSchedule = new Schedule(atCourse, start, duration, day, Room.getEmptyRoom(start, start.plus(Duration.ofMinutes(duration)), day), false);
//                    newSchedule.addStudent(student);
//                    atCourse.addSchedule(newSchedule);
//                    occupied.add(newSchedule);
//                    System.out.println("Schedule successfully created!");
//                    break;
//                }
//            }
//        }
//    }
//    public ArrayList<TimeSlot>splitSlots(int duration, ArrayList<TimeSlot>emptySlots){
//        Duration d = Duration.ofMinutes(duration);
//        ArrayList<TimeSlot>timeSlots = new ArrayList<>();
//        for(TimeSlot ts : emptySlots){
//            if(Tool.isShorter(d, ts.getTime())){
//                int amount = (int) (ts.getTime().getSeconds()/d.getSeconds());
//                LocalTime currentStart = ts.getStart();
//                for(int i = 0; i<amount; i++){
//                    LocalTime end = currentStart.plus(Duration.ofMinutes(duration));
//                    timeSlots.add(new TimeSlot(day, currentStart, end, ts.getExistingSchedule()));
//                    currentStart = end;
//                }
//            }
//        }
//        return timeSlots;
//    }
    private boolean isTimeEqualOrBetween(LocalTime checkStart, LocalTime checkEnd, LocalTime start, LocalTime end){
        return ((checkStart.equals(start) || (checkStart.isAfter(start) && checkStart.isBefore(end))) && (checkEnd.isBefore(end) || checkEnd.equals(end) && checkEnd.isAfter(start)));
    }

    public ArrayList<Schedule> getOccupied() {
        return occupied;
    }

    public void getOfTeacher(ObjectCallBack<Teacher>callBack) {
        try {
            findAggregatedObject(Teacher.class, "timeArrangements", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public void getAtCourse(ObjectCallBack<Course>callBack) {
        try {
            findAggregatedObject( Course.class, "dayTimeArrangement", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public int getMaxMeeting() {
        return maxMeeting;
    }

    public void setMaxMeeting(int maxMeeting) {
        this.maxMeeting = maxMeeting;
    }


    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<DTARepository> getRepositoryClass() {
        return DTARepository.class;
    }

    @Override
    public Class<DTAFirebase> getFirebaseClass() {
        return fbc;
    }

    @Override
    public String getID() {
        return DTA_ID;
    }

    @Override
    public String toString() {
        return "DayTimeArrangement{" +
                "fbn=" + fbn +
                ", fbc=" + fbc +
                ", DTA_ID='" + DTA_ID + '\'' +
                ", day=" + day +
                ", start=" + start +
                ", end=" + end +
                ", occupied=" + occupied +
                ", maxMeeting=" + maxMeeting +
                '}';
    }
}

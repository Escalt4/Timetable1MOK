package com.example.timetable1mok;

public class SetTimetableEvent {
    public final Pair[][][][] timetable;
    public final Integer group;
    public final Integer week;

    public SetTimetableEvent(Pair[][][][] timetable, Integer group, Integer week) {
        this.timetable = timetable;
        this.group = group;
        this.week = week;
    }

    public Pair[][][][] getTimetable() {
        return timetable;
    }

    public Integer getGroup() {
        return group;
    }

    public Integer getWeek() {
        return week;
    }

}

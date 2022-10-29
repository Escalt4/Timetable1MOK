package com.example.timetable1mok;

public class SetTimetableEvent {
    public final Pair[][][][] timetable;
    public final Integer group;
    public final Integer weekType;
    public final Integer weekNum;

    public SetTimetableEvent(Pair[][][][] timetable, Integer group, Integer weekType, Integer weekNum) {
        this.timetable = timetable;
        this.group = group;
        this.weekType = weekType;
        this.weekNum = weekNum;
    }

    public Pair[][][][] getTimetable() {
        return timetable;
    }

    public Integer getGroup() {
        return group;
    }

    public Integer getWeekType() {
        return weekType;
    }

    public Integer getWeekNum() {
        return weekNum;
    }
}

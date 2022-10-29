package com.example.timetable1mok;

public class SetTimetableEvent {
    public final Pair[][][][] timetable;
    public final Integer group;
    public final Integer weekType;
    public final Integer weekNum;
    public final Integer highlightWeek;
    public final Integer highlightDay;
    public final Integer highlightPair;
    public final Integer highlightBreak;
    public final String brakeBlockLabel;

    public SetTimetableEvent(Pair[][][][] timetable,
                             Integer group,
                             Integer weekType,
                             Integer weekNum,
                             Integer highlightWeek,
                             Integer highlightDay,
                             Integer highlightPair,
                             Integer highlightBreak,
                             String brakeBlockLabel) {
        this.timetable = timetable;
        this.group = group;
        this.weekType = weekType;
        this.weekNum = weekNum;
        this.highlightWeek = highlightWeek;
        this.highlightDay = highlightDay;
        this.highlightPair = highlightPair;
        this.highlightBreak = highlightBreak;
        this.brakeBlockLabel = brakeBlockLabel;
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

    public Integer getHighlightDay() {
        return highlightDay;
    }

    public Integer getHighlightPair() {
        return highlightPair;
    }

    public Integer getHighlightBreak() {
        return highlightBreak;
    }

    public Integer getHighlightWeek() {
        return highlightWeek;
    }

    public String getBrakeBlockLabel() {
        return brakeBlockLabel;
    }
}

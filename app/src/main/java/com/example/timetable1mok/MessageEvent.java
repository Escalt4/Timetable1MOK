package com.example.timetable1mok;

public class MessageEvent {
    public final Pair[][][][] timetable;
    public final Integer group;
    public final Integer week;
    public final Integer highlightDay;
    public final Integer highlightPair;

    public MessageEvent(Pair[][][][] timetable, Integer group, Integer week, Integer highlightDay, Integer highlightPair) {
        this.timetable = timetable;
        this.group = group;
        this.week = week;
        this.highlightDay = highlightDay;
        this.highlightPair = highlightPair;
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

    public Integer getHighlightDay() {
        return highlightDay;
    }

    public Integer getHighlightPair() {
        return highlightPair;
    }
}

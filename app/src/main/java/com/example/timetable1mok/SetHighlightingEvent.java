package com.example.timetable1mok;

public class SetHighlightingEvent {
    public final Integer highlightWeek;
    public final Integer highlightDay;
    public final Integer highlightPair;
    public final Integer highlightBreak;

    public SetHighlightingEvent(Integer highlightWeek, Integer highlightDay, Integer highlightPair, Integer highlightBreak) {
        this.highlightWeek = highlightWeek;
        this.highlightDay = highlightDay;
        this.highlightPair = highlightPair;
        this.highlightBreak = highlightBreak;
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
}

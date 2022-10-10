package com.example.timetable1mok;

public class UpdateHighlightEvent {
    public final Integer highlightDay;
    public final Integer highlightPair;
    public final Integer highlightBreak;

    public UpdateHighlightEvent(Integer highlightDay, Integer highlightPair, Integer highlightBreak) {
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
}
package com.example.timetable1mok;

public class MessageEvent {
    public final boolean state;
    public final Integer pairNum;
    public final Integer currentDayNum;

    public MessageEvent(boolean state, Integer pairNum, Integer currentDayNum) {
        this.state = state;
        this.pairNum = pairNum;
        this.currentDayNum = currentDayNum;
    }


    public boolean getState() {
        return state;
    }


    public Integer getPairNum() {
        return pairNum;
    }


    public Integer getCurrentDayNum() {
        return currentDayNum;
    }
}

package com.example.timetable1mok;

import java.util.ArrayList;

public class MyTime {

    Integer hour = 0;
    Integer minute = 0;
    Integer second = 0;

//    ArrayList<String> temp = new ArrayList<>();


    MyTime() {
    }

    MyTime(Integer hour, Integer minute, Integer second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    MyTime(Integer hour, Integer minute) {
        this.hour = hour;
        this.minute = minute;
    }

    static public MyTime subtractionTimes(MyTime time1, MyTime time2) {
        Integer second1 = time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
        Integer second2 = time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();

        Integer subtraction = second1 - second2;


        if (subtraction < 0) {
            return new MyTime();
        }

        Integer time3Hour = subtraction / 60 / 60;
        Integer time3Minute = subtraction - time3Hour * 60 * 60;
        Integer time3Second = subtraction - time3Hour * 60 * 60 - time3Minute * 60;

        return new MyTime(time3Hour, time3Minute, time3Second);
    }


    static public String subtractionTimesFormatString(MyTime time1, MyTime time2) {
        Integer second1 = time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
        Integer second2 = time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();

        Integer subtraction = second1 - second2;


        if (subtraction < 0) {
            subtraction = 0;
        }

        Integer time3Hour = subtraction / 60 / 60;
        Integer time3Minute = (subtraction - time3Hour * 60 * 60)/60;
        Integer time3Second = subtraction - time3Hour * 60 * 60 - time3Minute * 60;

        String time = "";

        if (time3Hour < 10) {
            time += "0";
        }
        time += time3Hour + ":";
        if (time3Minute < 10) {
            time += "0";
        }
        time += time3Minute + ":";
        if (time3Second < 10) {
            time += "0";
        }
        time += time3Second;

        return time;
    }


    static public Boolean isBetweenTimes(MyTime time0, MyTime time1, MyTime time2) {
        Integer second0 = time0.getHour() * 60 * 60 + time0.getMinute() * 60 + time0.getSecond();
        Integer second1 = time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
        Integer second2 = time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();

        if (second0 > second1 && second0 < second2) {
            return true;
        } else {
            return false;
        }
    }


//    public MyTime additionTimes(MyTime time1, MyTime time2) {
//        Integer second1 = time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
//        Integer second2 = time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();
//
//        Integer addition = second1 + second2;
//
//        Integer time3Hour = addition / 60 / 60;
//        Integer time3Minute = addition - time3Hour * 60 * 60;
//        Integer time3Second = addition - time3Hour * 60 * 60 - time3Minute * 60;
//
//        return new MyTime(time3Hour, time3Minute, time3Second);
//    }


    public Integer getHour() {
        return hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public Integer getSecond() {
        return second;
    }

}

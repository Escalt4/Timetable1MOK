package com.example.timetable1mok;

public class MyTime {
    Integer hour = 0;
    Integer minute = 0;
    Integer second = 0;
    Integer day = 0;


    MyTime(Integer day, Integer hour, Integer minute, Integer second) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
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

    MyTime() {
    }


//    static public MyTime subtractionTimes(MyTime time1, MyTime time2) {
//        Integer second1 = time1.getDay() * 60 * 60 * 24 + time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
//        Integer second2 = time2.getDay() * 60 * 60 * 24 + time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();
//
//        Integer subtraction = second1 - second2;
//
//        Integer time3Day = subtraction / 60 / 60 / 24;
//        Integer time3Hour = (subtraction - time3Day * 60 * 60 * 24) / 60 / 60;
//        Integer time3Minute = (subtraction - time3Day * 60 * 60 * 24 - time3Hour * 60 * 60) / 60;
//        Integer time3Second = subtraction - time3Day * 60 * 60 * 24 - time3Hour * 60 * 60 - time3Minute * 60;
//
//        return new MyTime(time3Day, time3Hour, time3Minute, time3Second);
//    }

    static public Integer subtractionTimesSecond(MyTime time1, MyTime time2) {
        Integer second1 = time1.getDay() * 60 * 60 * 24 + time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
        Integer second2 = time2.getDay() * 60 * 60 * 24 + time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();

        return second1 - second2;
    }

    static public String timesFormatString(MyTime time0) {
        String time = "";

        if (time0.getHour() < 10) {
            time += "0";
        }
        time += time0.getHour() + ":";
        if (time0.getMinute() < 10) {
            time += "0";
        }
        time += time0.getMinute() + ":";
        if (time0.getSecond() < 10) {
            time += "0";
        }
        time += time0.getSecond();

        return time;
    }

    static public String subtractionTimesFormatString(MyTime time1, MyTime time2) {
        Integer second1 = time1.getDay() * 60 * 60 * 24 + time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
        Integer second2 = time2.getDay() * 60 * 60 * 24 + time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();

        Integer subtraction = second1 - second2;


        if (subtraction < 0) {
            subtraction = 0;
        }

        Integer time3Day = subtraction / 60 / 60 / 24;
        Integer time3Hour = (subtraction - time3Day * 60 * 60 * 24) / 60 / 60;
        Integer time3Minute = (subtraction - time3Day * 60 * 60 * 24 - time3Hour * 60 * 60) / 60;
        Integer time3Second = subtraction - time3Day * 60 * 60 * 24 - time3Hour * 60 * 60 - time3Minute * 60;

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
        Integer second0 = time0.getDay() * 60 * 60 * 24 + time0.getHour() * 60 * 60 + time0.getMinute() * 60 + time0.getSecond();
        Integer second1 = time1.getDay() * 60 * 60 * 24 + time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
        Integer second2 = time2.getDay() * 60 * 60 * 24 + time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();

        if (second0 >= second1 && second0 <= second2) {
            return true;
        } else {
            return false;
        }
    }


    static public MyTime additionTimes(MyTime time1, MyTime time2) {
        Integer second1 = time1.getDay() * 60 * 60 * 24 + time1.getHour() * 60 * 60 + time1.getMinute() * 60 + time1.getSecond();
        Integer second2 = time2.getDay() * 60 * 60 * 24 + time2.getHour() * 60 * 60 + time2.getMinute() * 60 + time2.getSecond();

        Integer addition = second1 + second2;

        Integer time3Day = addition / 60 / 60 / 24;
        Integer time3Hour = (addition - time3Day * 60 * 60 * 24) / 60 / 60;
        Integer time3Minute = (addition - time3Day * 60 * 60 * 24 - time3Hour * 60 * 60) / 60;
        Integer time3Second = addition - time3Day * 60 * 60 * 24 - time3Hour * 60 * 60 - time3Minute * 60;

        return new MyTime(time3Day, time3Hour, time3Minute, time3Second);
    }

    public Integer getDay() {
        return day;
    }

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

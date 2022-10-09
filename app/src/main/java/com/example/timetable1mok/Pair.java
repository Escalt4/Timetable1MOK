package com.example.timetable1mok;

public class Pair {
    String subject;         // название предмета
    String cabinetNum;      // номер кабинета
    String teacher;         // имя преподавателя
    Integer typePair;       // тип пары (лекция / практика)
    MyTime timeStart;       // время начала пары
    MyTime timeEnd;         // время окончания пары

    public Pair(String subject, String cabinetNum, String teacher, Integer typePair, MyTime timeStart, MyTime timeEnd) {
        this.subject = subject;
        this.cabinetNum = cabinetNum;
        this.teacher = teacher;
        this.typePair = typePair;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    public String getSubject() {
        return subject;
    }

    public String getCabinetNum() {
        return cabinetNum;
    }

    public String getTeacher() {
        return teacher;
    }

    public Integer getTypePair() {
        return typePair;
    }

    public String getTypePairString() {
        if (typePair == 0) {
            return "Лекция";
        }
        if (typePair == 1) {
            return "Практика";
        }
        return "";
    }

    public MyTime getTimeStart() {
        return timeStart;
    }

    public MyTime getTimeEnd() {
        return timeEnd;
    }

    public String getTimes() {
        String time = "";

        time += timeStart.getHour() + ":";
        if (timeStart.getMinute() < 10){
            time += "0";
        }
        time += timeStart.getMinute() + " - ";

        time += timeEnd.getHour() + ":";
        if (timeEnd.getMinute() < 10){
            time += "0";
        }
        time += timeEnd.getMinute();

        return time;
    }
}

package com.example.timetable1mok;

public class Pair {
    Integer dayNum;         // номер дня недели
    Integer group;          // группа
    Integer typeWeek;       // тип недели (верхняя / нижняя)
    Integer pairNum;        // какая пара по счету
    String subject;         // название предмета
    Integer cabinetNum;     // номер кабинета
    String teacher;         // имя преподавателя
    Integer typePair;       // тип пары (лекция / практика)


    public Pair(Integer dayNum, Integer group, Integer typeWeek, Integer pairNum, String subject, Integer cabinetNum, String teacher, Integer typePair) {
        this.dayNum = dayNum;
        this.group = group;
        this.typeWeek = typeWeek;
        this.pairNum = pairNum;
        this.subject = subject;
        this.cabinetNum = cabinetNum;
        this.teacher = teacher;
        this.typePair = typePair;
    }

    public Integer getDayNum() {
        return dayNum;
    }

    public Integer getGroup() {
        return group;
    }

    public Integer getTypeWeek() {
        return typeWeek;
    }

    public Integer getPairNum() {
        return pairNum;
    }

    public String getSubject() {
        return subject;
    }

    public Integer getCabinetNum() {
        return cabinetNum;
    }

    public String getTeacher() {
        return teacher;
    }

    public Integer getTypePair() {
        return typePair;
    }
}

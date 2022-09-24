package com.example.timetable1mok;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Calendar;

public class MyAdapter extends FragmentStateAdapter {
    private String[][][][][] timetable;
    private String[][] calls;
    private Integer changedGroup;
    private Calendar calendar;
    private Integer changedWeek;
    private Integer currentDay;
    private Integer currentMonth;
    private Integer[] currentWeekDays;


    public MyAdapter(FragmentActivity fragmentActivity, String[][][][][] timetable, String[][] calls, Integer changedGroup, Calendar calendar, Integer changedWeek, Integer currentDay, Integer currentMonth, Integer[] currentWeekDays) {
        super(fragmentActivity);
        this.timetable = timetable;
        this.calls = calls;
        this.changedGroup = changedGroup;
        this.calendar = calendar;
        this.changedWeek = changedWeek;
        this.currentDay = currentDay;
        this.currentMonth = currentMonth;
        this.currentWeekDays = currentWeekDays;

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (PageFragment.newInstance(position, timetable, calls, changedGroup,calendar, changedWeek, currentDay, currentMonth, currentWeekDays));
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}

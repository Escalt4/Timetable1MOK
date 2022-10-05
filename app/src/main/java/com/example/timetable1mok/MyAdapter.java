package com.example.timetable1mok;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyAdapter extends FragmentStateAdapter {
    private String[][][][][] timetable;
    private String[][] calls;
    private Integer group;
    private Integer week;


    public MyAdapter(FragmentActivity fragmentActivity, String[][][][][] timetable, String[][] calls, Integer group, Integer week) {
        super(fragmentActivity);
        this.timetable = timetable;
        this.calls = calls;
        this.group = group;
        this.week = week;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PageFragment.newInstance(position, timetable, calls, group, week);
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}

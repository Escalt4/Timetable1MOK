package com.example.timetable1mok;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class PageFragment extends Fragment {
    private int pageNumber;
    private String[][][][][] timetable;
    private String[][] calls;
    private Integer changedGroup;
    private Calendar calendar;
    private Integer changedWeek;
    private Integer currentDay;
    private Integer currentMonth;
    private Integer[] currentWeekDays;

    String LogTag = "MyApp";


    public static PageFragment newInstance(int page, String[][][][][] timetable, String[][] calls, Integer changedGroup, Calendar calendar, Integer changedWeek, Integer currentDay, Integer currentMonth, Integer[] currentWeekDays) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt("num", page);
        args.putSerializable("timetable", timetable);
        args.putSerializable("calls", calls);
        args.putInt("changedGroup", changedGroup);
        args.putSerializable("calendar", calendar);
        args.putInt("changedWeek", changedWeek);
        args.putInt("currentDay", currentDay);
        args.putInt("currentMonth", currentMonth);
        args.putSerializable("currentWeekDays", currentWeekDays);
        fragment.setArguments(args);
        return fragment;
    }

    public PageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("num") : 1;
        timetable = (String[][][][][]) getArguments().getSerializable("timetable");
        calls = (String[][]) getArguments().getSerializable("calls");
        changedGroup = getArguments().getInt("changedGroup");
        calendar = (Calendar) getArguments().getSerializable("calendar");
        changedWeek = getArguments().getInt("changedWeek");
        currentDay = getArguments().getInt("currentDay");
        currentMonth = getArguments().getInt("currentMonth");
        currentWeekDays = (Integer[]) getArguments().getSerializable("currentWeekDays");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_page, container, false);
        try {
            for (int i = 0; i < 5; i++) {
                Integer cutoff = 0;
                for (int j = 4; j > -1; j--) {
                    if (timetable[pageNumber][changedGroup - 1][changedWeek - 1][j][0] != null && j > cutoff) {
                        cutoff = j;
                    }
                }

                TextView textViewNumder = result.findViewById(getResources().getIdentifier("textViewNumder" + (i + 1), "id", "com.example.timetable1mok"));
                textViewNumder.setText(" " + (i + 1));

                TextView textViewTime = result.findViewById(getResources().getIdentifier("textViewTime" + (i + 1), "id", "com.example.timetable1mok"));
                if (pageNumber == 0) {
                    textViewTime.setText(calls[0][i].replace("-", "\n"));
                } else {
                    if (i < 4) {
                        textViewTime.setText(calls[1][i].replace("-", "\n"));
                    }
                }

                if (timetable[pageNumber][changedGroup - 1][changedWeek - 1][i][0] == null) {
                    if (i >= cutoff) {
                        LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", "com.example.timetable1mok"));
                        linearLayout.setVisibility(View.GONE);
                    }
                } else {
                    TextView textViewSubject = result.findViewById(getResources().getIdentifier("textViewSubject" + (i + 1), "id", "com.example.timetable1mok"));
                    textViewSubject.setText(timetable[pageNumber][changedGroup - 1][changedWeek - 1][i][0]);

                    TextView textViewInfo = result.findViewById(getResources().getIdentifier("textViewInfo" + (i + 1), "id", "com.example.timetable1mok"));
                    textViewInfo.setText(timetable[pageNumber][changedGroup - 1][changedWeek - 1][i][1] + " | " + timetable[pageNumber][changedGroup - 1][changedWeek - 1][i][2]);
                }
            }
        } catch (Exception ex) {
            Log.e(LogTag, ex.getMessage());
        }
        return result;
    }


}
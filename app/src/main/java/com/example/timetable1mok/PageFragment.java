package com.example.timetable1mok;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PageFragment extends Fragment {
    String LogTag = "MyApp";
    private int pageNumber;
    private Pair[][][][] timetable;
    private Integer group;
    private Integer week;
    private Integer highlightDay = -1;
    private Integer highlightPair = -1;
    private Integer highlightBreak = -1;
    private View result;

    public static PageFragment newInstance(int page) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        result = inflater.inflate(R.layout.fragment_page, container, false);
        EventBus.getDefault().register(this);
        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onUpdateTimetableEvent(UpdateTimetableEvent event) {
        timetable = event.getTimetable();
        group = event.getGroup();
        week = event.getWeek();

        updateTimetable();
    }

    public void updateTimetable() {
        for (int i = 0; i < 5; i++) {
            Integer lastPair = 5;
            for (int j = 4; j >= 0; j--) {
                if (timetable[pageNumber][group - 1][week - 1][j] != null) {
                    lastPair = j;
                    break;
                }
            }

            LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", getActivity().getPackageName()));
            if (timetable[pageNumber][group - 1][week - 1][i] == null) {
                if (i > lastPair) {
                    linearLayout.setVisibility(View.GONE);
                } else {
                    linearLayout.setVisibility(View.INVISIBLE);
                }
            } else {
                linearLayout.setVisibility(View.VISIBLE);

                TextView textViewNumder = result.findViewById(getResources().getIdentifier("textViewNumder" + (i + 1), "id", getActivity().getPackageName()));
                textViewNumder.setText(String.valueOf(i + 1));

                TextView textViewSubject = result.findViewById(getResources().getIdentifier("textViewSubject" + (i + 1), "id", getActivity().getPackageName()));
                textViewSubject.setText(timetable[pageNumber][group - 1][week - 1][i].getSubject());

                TextView textViewInfo1 = result.findViewById(getResources().getIdentifier("textViewInfo1" + (i + 1), "id", getActivity().getPackageName()));
                textViewInfo1.setText(timetable[pageNumber][group - 1][week - 1][i].getTimes() + " | " + timetable[pageNumber][group - 1][week - 1][i].getTypePairString());

                TextView textViewInfo2 = result.findViewById(getResources().getIdentifier("textViewInfo2" + (i + 1), "id", getActivity().getPackageName()));
                textViewInfo2.setText(timetable[pageNumber][group - 1][week - 1][i].getCabinetNum() + " | " + timetable[pageNumber][group - 1][week - 1][i].getTeacher());
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onHighlightEvent(UpdateHighlightEvent event) {
        highlightDay = event.getHighlightDay();
        highlightPair = event.getHighlightPair();
        highlightBreak = event.getHighlightBreak();

        updateHighlight();
    }

    public void updateHighlight() {
        for (int i = 0; i < 5; i++) {
            LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", getActivity().getPackageName()));
            if (highlightDay == pageNumber && highlightPair == i) {
                linearLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_enable));
            } else {
                linearLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner));
            }

            LinearLayout linearLayout1 = result.findViewById(getResources().getIdentifier("linearLayout1" + (i + 1), "id", getActivity().getPackageName()));
            if (highlightDay == pageNumber && highlightBreak == i) {
                linearLayout1.setVisibility(View.VISIBLE);
            } else {
                linearLayout1.setVisibility(View.GONE);
            }
        }
    }
}
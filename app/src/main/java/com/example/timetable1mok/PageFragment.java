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
    String LOG_TAG = "MyApp";

    private int pageNumber;
    private View result;

    private Pair[][][][] timetable;
    private Integer group;
    private Integer weekType;
    private Integer weekNum;
    private Integer highlightWeek = -1;
    private Integer highlightDay = -1;
    private Integer highlightPair = -1;
    private Integer highlightBreak = -1;

    public static PageFragment newInstance(int page) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putInt("group", this.group);
        outState.putInt("weekType", this.weekType);
        outState.putInt("weekNum", this.weekNum);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        result = inflater.inflate(R.layout.fragment_page, container, false);

        if (savedInstanceState != null) {
            this.group = savedInstanceState.getInt("group");
            this.weekType = savedInstanceState.getInt("weekType");
            this.weekNum = savedInstanceState.getInt("weekNum");
        }

        EventBus.getDefault().register(this);

        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onSetTimetableEvent(SetTimetableEvent event) {
        timetable = event.getTimetable();
        group = event.getGroup();
        weekType = event.getWeekType();
        weekNum = event.getWeekNum();
        highlightWeek = event.getHighlightWeek();
        highlightDay = event.getHighlightDay();
        highlightPair = event.getHighlightPair();
        highlightBreak = event.getHighlightBreak();

        setTimetable();
        deleteHighlighting();
        setHighlighting();
    }

    public void setTimetable() {
        for (int i = 0; i < 5; i++) {
            Integer lastPair = 5;
            for (int j = 4; j >= 0; j--) {
                if (timetable[pageNumber][group - 1][weekType - 1][j] != null) {
                    lastPair = j;
                    break;
                }
            }

            LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", getActivity().getPackageName()));
            if (timetable[pageNumber][group - 1][weekType - 1][i] == null) {
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
                textViewSubject.setText(timetable[pageNumber][group - 1][weekType - 1][i].getSubject());

                TextView textViewInfo1 = result.findViewById(getResources().getIdentifier("textViewInfo1" + (i + 1), "id", getActivity().getPackageName()));
                textViewInfo1.setText(timetable[pageNumber][group - 1][weekType - 1][i].getTimesAsString() + " | " + timetable[pageNumber][group - 1][weekType - 1][i].getTypePairString());

                TextView textViewInfo2 = result.findViewById(getResources().getIdentifier("textViewInfo2" + (i + 1), "id", getActivity().getPackageName()));
                textViewInfo2.setText(timetable[pageNumber][group - 1][weekType - 1][i].getCabinetNum() + " | " + timetable[pageNumber][group - 1][weekType - 1][i].getTeacher());
            }
        }
    }

    public void deleteHighlighting() {
        for (int i = 0; i < 5; i++) {
            LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", getActivity().getPackageName()));
            linearLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner));

            LinearLayout linearLayout1 = result.findViewById(getResources().getIdentifier("linearLayout1" + (i + 1), "id", getActivity().getPackageName()));
            linearLayout1.setVisibility(View.GONE);
        }
    }

    public void setHighlighting() {
        Log.d(LOG_TAG, "pageNumber " + pageNumber + " " + highlightWeek + " " + highlightDay + " " + highlightPair + " " + highlightBreak + " " + weekNum);

        if (highlightWeek == weekNum && highlightDay == pageNumber) {
            for (int i = 0; i < 5; i++) {
                LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", getActivity().getPackageName()));
                if (highlightPair == i) {
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
}
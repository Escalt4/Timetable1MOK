package com.example.timetable1mok;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class PageFragment extends Fragment {
    String[] lessonsType = {"Лекция", "Практика"};

    private int pageNumber;
    private String[][][][][] timetable;
    private String[][] calls;
    private Integer group;
    private Integer week;

    String LogTag = "MyApp";

    public static PageFragment newInstance(int page, String[][][][][] timetable, String[][] calls, Integer group, Integer week) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt("num", page);
        args.putSerializable("timetable", timetable);
        args.putSerializable("calls", calls);
        args.putInt("group", group);
        args.putInt("week", week);
        fragment.setArguments(args);
        return fragment;
    }

    public void setSelectedItem(boolean selectedItem) {
        if (selectedItem) {
//        LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", "com.example.timetable1mok"));

            LinearLayout linearLayout = getView().findViewById(R.id.linearLayout1);
            linearLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_enable));
        } else {
            LinearLayout linearLayout = getView().findViewById(R.id.linearLayout1);
            linearLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("num") : 1;
        timetable = (String[][][][][]) getArguments().getSerializable("timetable");
        calls = (String[][]) getArguments().getSerializable("calls");
        group = getArguments().getInt("group");
        week = getArguments().getInt("week");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_page, container, false);
        try {
//            new ProgressTask().execute();

//            Calendar cur = new GregorianCalendar();
//            cur.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
//            cur.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));

            for (int i = 0; i < 5; i++) {
                Integer calls_type;
                if (pageNumber == 0) {
                    calls_type = 0;
                } else {
                    calls_type = 1;
                }

                Integer cutoff = 0;
                for (int j = 4; j > -1; j--) {
                    if (timetable[pageNumber][group - 1][week - 1][j][0] != null && j > cutoff) {
                        cutoff = j;
                    }
                }

                TextView textViewNumder = result.findViewById(getResources().getIdentifier("textViewNumder" + (i + 1), "id", "com.example.timetable1mok"));
                textViewNumder.setText("" + (i + 1));

                TextView textViewTime = result.findViewById(getResources().getIdentifier("textViewTime" + (i + 1), "id", "com.example.timetable1mok"));
                textViewTime.setText(calls[calls_type][i].replace("-", "\n"));

                if (timetable[pageNumber][group - 1][week - 1][i][0] == null) {
                    if (i >= cutoff) {
                        LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", "com.example.timetable1mok"));
                        linearLayout.setVisibility(View.GONE);
                    }
                } else {
//                    Calendar start1 = new GregorianCalendar();
//                    start1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(calls[calls_type][i].split("-")[0].split("\\.")[0]));
//                    start1.set(Calendar.MINUTE, Integer.parseInt(calls[calls_type][i].split("-")[0].split("\\.")[1]));
//
//                    Calendar end1 = new GregorianCalendar();
//                    end1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(calls[calls_type][i].split("-")[1].split("\\.")[0]));
//                    end1.set(Calendar.MINUTE, Integer.parseInt(calls[calls_type][i].split("-")[1].split("\\.")[1]));

//                    LinearLayout linearLayout = result.findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", "com.example.timetable1mok"));
//                    if (cur.getTimeInMillis() > start1.getTimeInMillis() && end1.getTimeInMillis() > cur.getTimeInMillis()) {
//                        linearLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_enable));
//                    }

                    TextView textViewSubject = result.findViewById(getResources().getIdentifier("textViewSubject" + (i + 1), "id", "com.example.timetable1mok"));
                    textViewSubject.setText(timetable[pageNumber][group - 1][week - 1][i][0]);

                    TextView textViewInfo = result.findViewById(getResources().getIdentifier("textViewInfo" + (i + 1), "id", "com.example.timetable1mok"));
                    textViewInfo.setText(timetable[pageNumber][group - 1][week - 1][i][1] + " | " +
                            timetable[pageNumber][group - 1][week - 1][i][2] + "\n" +
                            lessonsType[Integer.parseInt(timetable[pageNumber][group - 1][week - 1][i][3])]);
                }
            }
        } catch (Exception ex) {
            Log.e(LogTag, ex.getMessage());
            Log.e(LogTag, Log.getStackTraceString(ex));
        }
        return result;
    }

//    class ProgressTask extends AsyncTask<Void, Integer, Void> {
//        @Override
//        protected Void doInBackground(Void... unused) {
//            for (int i = 0; i < integers.length; i++) {
//
//                publishProgress(i);
//                SystemClock.sleep(400);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... items) {
//            indicatorBar.setProgress(items[0] + 1);
//            statusView.setText("Статус: " + String.valueOf(items[0] + 1));
//        }
//
//        @Override
//        protected void onPostExecute(Void unused) {
//            Toast.makeText(getActivity(), "Задача завершена", Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }
}
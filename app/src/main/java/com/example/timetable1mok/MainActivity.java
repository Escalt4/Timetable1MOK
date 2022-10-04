package com.example.timetable1mok;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    //    String DATABASE_FILE_NAME = "DATABASE";
    String LogTag = "MyApp";
    Integer[] dayOfWeek = {123, 6, 0, 1, 2, 3, 4, 5};
    String[] tabsName = {"ПН", "ВТ", "СР", "ЧТ", "ПТ"};
    String[] monthName = {"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};

    MyTime[][] callsTime = new MyTime[2][11];

    SharedPreferences settings;
    SharedPreferences.Editor prefEditor;

    TextView textViewTimer;
    TextView textViewCurWeek;
    Button buttonGroup;
    Button buttonToCurrentDate;
    ViewPager2 pager;
    Calendar calendar;

    String[][][][][] timetable;
    String[][] calls;

    Integer group; // выбраная группа
    Integer week; // четная или нечетная неделя
    Integer currentDayNum; // день недели числом
    Integer[] weekDays; // список дней текущей недели
    Integer[] weekMonth; // список дней текущей недели

    long curDateMillis;
    Integer calls_type;

    String startDate;
    String endDate;

    Integer currentTab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        textViewCurWeek = findViewById(R.id.textViewCurWeek);
        buttonGroup = findViewById(R.id.buttonGroup);
        buttonToCurrentDate = findViewById(R.id.buttonToCurrentDate);
        textViewTimer = findViewById(R.id.textViewTimer);

        initializeDate();

        settings = getSharedPreferences("Settings", MODE_PRIVATE);
        prefEditor = settings.edit();
        group = settings.getInt("group", 2);
        buttonGroup.setText(group + " подгруппа");

        getTimetable();
    }


    public void initializeDate() {
        calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) > 17) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        currentDayNum = dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)];
        if (currentDayNum < 5) {
            currentTab = currentDayNum;
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            currentTab = 0;
        }

        calendar.set(Calendar.DAY_OF_WEEK, 2);
        startDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        calendar.set(Calendar.DAY_OF_WEEK, 2 + 6);
        endDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", calendar.get(Calendar.MONTH) + 1);

        if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            week = 2;
            textViewCurWeek.setText(startDate + " - " + endDate + " нижняя неделя");
        } else {
            textViewCurWeek.setText(startDate + " - " + endDate + " верхняя неделя");
            week = 1;
        }

        weekDays = new Integer[7];
        weekMonth = new Integer[7];
        for (int d = 0; d < 7; d++) {
            calendar.set(Calendar.DAY_OF_WEEK, d + 2);
            weekDays[d] = calendar.get(Calendar.DAY_OF_MONTH);
            weekMonth[d] = calendar.get(Calendar.MONTH);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        curDateMillis = calendar.getTimeInMillis();

        for (int i = 0; i < 5; i++) {
            if (currentDayNum == 0) {
                calls_type = 0;
            } else {
                calls_type = 1;
            }
        }
    }


    public void getTimetable() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.timetable_1mok);

            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String text = new String(buffer);

            String[] timetableBlocks = text.split("========");

            timetable = new String[5][2][2][5][4];
            calls = new String[2][5];

            calls[1][4] = "00.00-00.00";

            for (int i = 0; i < timetableBlocks.length; i++) {
                String[] blocksPart = timetableBlocks[i].split("--------");

                if (i == 0) {
                    for (int j = 1; j < blocksPart.length; j++) {
                        String[] strings = blocksPart[j].split("\n");

                        if (j == 1) {
                            for (int k = 1; k < strings.length; k++) {
                                calls[0][k - 1] = strings[k].replace("\"", "").replace("\r", "").replace("\n", "");
                            }
                        } else {
                            for (int k = 1; k < strings.length; k++) {
                                calls[1][k - 1] = strings[k].replace("\"", "").replace("\r", "").replace("\n", "");
                            }
                        }
                    }

                } else {
                    for (int j = 1; j < blocksPart.length; j++) {
                        String[] strings = blocksPart[j].split("\n");

                        for (int k = 1; k < strings.length; k = k + 6) {
                            String[] temp1 = strings[k].split(" ");

                            for (int q = 0; q < temp1.length; q++) {
                                int groop_ = Integer.parseInt(Character.toString(temp1[q].charAt(0))) - 1;
                                int week_ = Integer.parseInt(Character.toString(temp1[q].charAt(1))) - 1;
                                int day_ = Integer.parseInt(strings[k + 1].replace("\r", "")) - 1;

                                for (int w = 0; w < 4; w++) {
                                    timetable[i - 1][groop_][week_][day_][w] = strings[k + 2 + w].replace("\r", "").replace("\n", "");
                                }
                            }
                        }
                    }
                }
            }

            initializeCalls();
            startNewTimer();

            setTimetable();


        } catch (Exception ex) {
            Log.e(LogTag, Log.getStackTraceString(ex));
        }
    }

    void initializeCalls() {
        // todo добавлять звонки в зависимости от расписания
        for (int i = 0; i < 2; i++) {
            int k = 0;
            for (int j = 0; j < (5 - i); j++) {
                callsTime[i][k] = new MyTime(
                        j,
                        Integer.parseInt(calls[i][j].split("-")[0].split("\\.")[0]),
                        Integer.parseInt(calls[i][j].split("-")[0].split("\\.")[1],
                                0
                        )
                );

                callsTime[i][k + 1] = new MyTime(
                        j,
                        Integer.parseInt(calls[i][j].split("-")[1].split("\\.")[0]),
                        Integer.parseInt(calls[i][j].split("-")[1].split("\\.")[1],
                                0
                        )
                );
                k += 2;
            }
        }
//        callsTime[0][10] = MyTime.additionTimes(callsTime[0][0], new MyTime(24, 0, 0));
//        callsTime[1][8] = MyTime.additionTimes(callsTime[1][0], new MyTime(24, 0, 0));
    }

    void setTimetable() {
        pager = findViewById(R.id.pager);
        FragmentStateAdapter pageAdapter = new MyAdapter(this, timetable, calls, group, week);
        pager.setAdapter(pageAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                tab.setText(tabsName[position] + "\n" + weekDays[position] + " " + monthName[weekMonth[position]]);
            }
        });
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                buttonToCurrentDate.setEnabled(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        pager.setCurrentItem(currentTab, false);
        buttonToCurrentDate.setEnabled(false);

//        FragmentManager manager = getSupportFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();

//        Fragment myFragment = getSupportFragmentManager().findFragmentByTag("f" + pager.getCurrentItem());
//
//        myFragment.

    }


    String label;
    MyTime cur;
    MyTime timeStart;
    MyTime timeEnd;
    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            cur = new MyTime(
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    Calendar.getInstance().get(Calendar.SECOND)
            );

            if (MyTime.isBetweenTimes(cur, timeStart, timeEnd)) {
                textViewTimer.setText(label + MyTime.subtractionTimesFormatString(timeEnd, cur));
                doCicleTimer();
            } else {
                startNewTimer();
            }
        }
    };


    public void doCicleTimer() {
        handler.postDelayed(runnable, 250);
    }


    public void startNewTimer() {
        cur = new MyTime(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                Calendar.getInstance().get(Calendar.SECOND)
        );

        for (int i = 0; i < (10 - calls_type * 2); i++) {
            if (MyTime.isBetweenTimes(cur, callsTime[calls_type][i], callsTime[calls_type][i + 1])) {
                if (i % 2 == 0) {
                    label = "До конца пары:\n";
                } else {
                    label = "До начала пары:\n";
                }

                timeStart = callsTime[calls_type][i];
                timeEnd = callsTime[calls_type][i + 1];
                doCicleTimer();
                break;
            }
        }
    }

    public void updateDate() {
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        startDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        calendar.set(Calendar.DAY_OF_WEEK, 2 + 6);
        endDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", calendar.get(Calendar.MONTH) + 1);

        if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            week = 2;
            textViewCurWeek.setText(startDate + " - " + endDate + " нижняя неделя");
        } else {
            textViewCurWeek.setText(startDate + " - " + endDate + " верхняя неделя");
            week = 1;
        }

        weekDays = new Integer[7];
        weekMonth = new Integer[7];
        for (int d = 0; d < 7; d++) {
            calendar.set(Calendar.DAY_OF_WEEK, d + 2);
            weekDays[d] = calendar.get(Calendar.DAY_OF_MONTH);
            weekMonth[d] = calendar.get(Calendar.MONTH);
        }

        currentTab = pager.getCurrentItem();
        setTimetable();
    }

    // Обработка нажатий кнопок
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonGroup:
                if (group == 1) {
                    group = 2;
                } else {
                    group = 1;
                }

                buttonGroup.setText(group + " подгруппа");
                prefEditor.putInt("group", group);
                prefEditor.apply();

                currentTab = pager.getCurrentItem();
                setTimetable();
                break;

            case R.id.buttonToCurrentDate:
                initializeDate();
                setTimetable();
                buttonToCurrentDate.setEnabled(false);
                break;

            case R.id.buttonWeekUp:
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                updateDate();
                buttonToCurrentDate.setEnabled(true);
                break;

            case R.id.buttonWeekDown:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                updateDate();
                buttonToCurrentDate.setEnabled(true);
                break;

            default:
                break;
        }
    }
}
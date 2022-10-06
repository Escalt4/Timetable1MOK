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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    String LogTag = "MyApp";
    Integer[] dayOfWeek = {6, 0, 1, 2, 3, 4, 5};
    String[] tabsName = {"ПН", "ВТ", "СР", "ЧТ", "ПТ"};
    String[] monthName = {"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};

    SharedPreferences settings;
    SharedPreferences.Editor prefEditor;

    Button buttonGroup;
    TextView textViewTimer;
    TextView textViewCurWeek;
    Button buttonToCurrentDate;
    ViewPager2 pager;

    String[][][][][] timetable;
    String[][] calls;
    MyTime[][] callsTime = new MyTime[2][11];

    MyTime[][] baseCallMonday;
    MyTime[][] baseCallOther;

    MyTime[][][][][] callSchedule;

    Calendar calendar;
    Integer group; // выбраная группа
    Integer currentDayNum; // день недели числом
    Integer week; // четная или нечетная неделя
    Integer[] weekDays; // список дней текущей недели
    Integer[] weekMonth;
    Integer calls_type;
    Integer currentTab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        buttonGroup = findViewById(R.id.buttonGroup);
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewCurWeek = findViewById(R.id.textViewCurWeek);
        buttonToCurrentDate = findViewById(R.id.buttonToCurrentDate);

        // определение переменных связаных с датой
        updateDateVariables(true);

        // востановление настроек
        settings = getSharedPreferences("Settings", MODE_PRIVATE);
        prefEditor = settings.edit();
        group = settings.getInt("group", 2);
        buttonGroup.setText(group + "");

        // получение расписания из файла
        getCalls();
        getTimetable();
    }


    @Override
    public void onStop() {
        super.onStop();

        prefEditor.putInt("group", group);
        prefEditor.apply();
    }


    // определение переменных связаных с датой
    public void updateDateVariables(boolean CurDate) {
        if (CurDate) {
            calendar = Calendar.getInstance();
            if (calendar.get(Calendar.HOUR_OF_DAY) > 17) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        currentDayNum = dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        if (CurDate) {
            if (currentDayNum < 5) {
                currentTab = currentDayNum;
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                currentTab = 0;
            }
        }

        if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            week = 2;
            textViewCurWeek.setText("Нижняя неделя");
        } else {
            textViewCurWeek.setText("Верхняя неделя");
            week = 1;
        }

        weekDays = new Integer[7];
        weekMonth = new Integer[7];
        for (int d = 0; d < 7; d++) {
            calendar.set(Calendar.DAY_OF_WEEK, d + 2);
            weekDays[d] = calendar.get(Calendar.DAY_OF_MONTH);
            weekMonth[d] = calendar.get(Calendar.MONTH);
        }

        for (int i = 0; i < 5; i++) {
            if (currentDayNum == 0) {
                calls_type = 0;
            } else {
                calls_type = 1;
            }
        }

        if (!CurDate) {
            currentTab = pager.getCurrentItem();
            setTimetable();
        }
    }


    public MyTime[] splitCallsString(String str) {
        str = str.replace("\"", "").replace("\r", "").replace("\n", "");

        String[] strParts = str.split("-");

        MyTime[] calls = new MyTime[2];

        calls[0] = new MyTime(
                Integer.parseInt(strParts[0].split("\\.")[0]),
                Integer.parseInt(strParts[0].split("\\.")[1])
        );

        calls[1] = new MyTime(
                Integer.parseInt(strParts[1].split("\\.")[0]),
                Integer.parseInt(strParts[1].split("\\.")[1])
        );

        return calls;
    }


    // получение расписания звонков из файла
    public void getCalls() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.calls_1mok);

            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String text = new String(buffer);

            String[] callsBlocks = text.split("========");

            String[] Monday = callsBlocks[0].split("\n");
            String[] Other = callsBlocks[1].split("\n");

            // номер пары; время начала или конца
            baseCallMonday = new MyTime[5][2];
            baseCallOther = new MyTime[5][2];

            for (int i = 0; i < Monday.length; i++) {
                baseCallMonday[i] = splitCallsString(Monday[i]);
            }
            for (int i = 0; i < Other.length; i++) {
                baseCallOther[i] = splitCallsString(Other[i]);
            }

        } catch (Exception ex) {
            Log.e(LogTag, Log.getStackTraceString(ex));
        }
    }


    // получение расписания из файла
    public void getTimetable() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.timetable_1mok);

            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String text = new String(buffer);

            String[] timetableBlocks = text.split("========");

            //
            timetable = new String[5][2][2][5][4];

            for (int i = 0; i < timetableBlocks.length; i++) {
                String[] blocksPart = timetableBlocks[i].split("--------");

                if (i == 0) {
                    for (int j = 1; j < blocksPart.length; j++) {
                        String[] strings = blocksPart[j].split("\n");

                        if (j == 1) {
                            for (int k = 1; k < strings.length; k++) {
//                                callSchedule[0][k - 1] = splitCallsString(strings[k]);
                                calls[0][k - 1] = strings[k].replace("\"", "").replace("\r", "").replace("\n", "");
                            }
                        } else {
                            for (int k = 1; k < strings.length; k++) {
//                                callSchedule[1][k - 1] = splitCallsString(strings[k]);
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
                                Log.d(LogTag, day_ + " ");
                                for (int w = 0; w < 4; w++) {
                                    callSchedule[i - 1][groop_][week_][day_] = splitCallsString(calls[week_][i - 1]);
                                    timetable[i - 1][groop_][week_][day_][w] = strings[k + 2 + w].replace("\r", "").replace("\n", "");
                                }
                            }
                        }
                    }
                }
            }
            Log.d(LogTag, MyTime.timesFormatString(callSchedule[0][1][1][1][0]));
            Log.d(LogTag, MyTime.timesFormatString(callSchedule[0][1][1][1][1]));

            setTimetable();

            initializeCalls();
            startNewTimer();
        } catch (Exception ex) {
            Log.e(LogTag, Log.getStackTraceString(ex));
        }
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

    }


    void initializeCalls() {
        for (int i = 0; i < 2; i++) {
            int k = 0;
            for (int j = 0; j < (5 - i); j++) {
                callsTime[i][k] = new MyTime(
                        Integer.parseInt(calls[i][j].split("-")[0].split("\\.")[0]),
                        Integer.parseInt(calls[i][j].split("-")[0].split("\\.")[1])
                );

                callsTime[i][k + 1] = new MyTime(
                        Integer.parseInt(calls[i][j].split("-")[1].split("\\.")[0]),
                        Integer.parseInt(calls[i][j].split("-")[1].split("\\.")[1])
                );
                k += 2;
            }
        }
//        callsTime[0][10] = MyTime.additionTimes(callsTime[0][0], new MyTime(24, 0, 0));
//        callsTime[1][8] = MyTime.additionTimes(callsTime[1][0], new MyTime(24, 0, 0));
    }


    String label;
    Integer curPair = 0;
    ;
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
                EventBus.getDefault().post(new MessageEvent(true, curPair, currentDayNum));
                doCicleTimer();
            } else {
                if (curPair != 0) {
                    EventBus.getDefault().post(new MessageEvent(false, curPair, currentDayNum));
                }
                startNewTimer();
            }
        }
    };


    // таймер до начала/конца пары
    public void doCicleTimer() {
        handler.postDelayed(runnable, 250);
    }


    public void startNewTimer() {
        if (currentDayNum == 5 || currentDayNum == 6) {
            textViewTimer.setText("Сейчас нет пар");
            return;
        }

        cur = new MyTime(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                Calendar.getInstance().get(Calendar.SECOND)
        );

        boolean started = false;

        for (int i = 0; i < (10 - calls_type * 2) - 1; i++) {
            if (MyTime.isBetweenTimes(cur, callsTime[calls_type][i], callsTime[calls_type][i + 1])) {
                if (i % 2 == 0) {
                    label = "До конца пары:\n";
                    curPair = i / 2 + 1;
                } else {
                    curPair = 0;
                    label = "До начала пары:\n";
                }
                timeStart = callsTime[calls_type][i];
                timeEnd = callsTime[calls_type][i + 1];
                started = true;
                doCicleTimer();
                break;

            }
        }

        if (!started) {
            textViewTimer.setText("Сейчас нет пар");
        }
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

                buttonGroup.setText(group + "");
                currentTab = pager.getCurrentItem();
                setTimetable();
                break;

            case R.id.buttonToCurrentDate:
                updateDateVariables(true);
                setTimetable();
                buttonToCurrentDate.setEnabled(false);
                break;

            case R.id.buttonWeekUp:
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                updateDateVariables(false);
                buttonToCurrentDate.setEnabled(true);
                break;

            case R.id.buttonWeekDown:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                updateDateVariables(false);
                buttonToCurrentDate.setEnabled(true);
                break;

            default:
                break;
        }
    }
}
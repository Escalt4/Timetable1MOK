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

    Pair[][][][] timetable;
    MyTime[][][] callsSchedule;

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
        createTimetablePages();
        
    }

    
    @Override
    public void onStop() {
        super.onStop();

        // сохранение настроек
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
//            setTimetable();
        }
    }


    // получение времени начала/каонца пары из строки
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
            String text = new String(buffer).replace("\r", "");

            String[] callsBlocks = text.split("========\n");

            String[] Monday = callsBlocks[0].split("\n");
            String[] Other = callsBlocks[1].split("\n");

            // структура    номер дня недели; номер пары; время начала или конца;
            callsSchedule = new MyTime[5][5][2];

            for (int i = 0; i < Monday.length; i++) {
                callsSchedule[0][i] = splitCallsString(Monday[i]);
            }
            for (int i = 0; i < Other.length; i++) {
                callsSchedule[1][i] = splitCallsString(Other[i]);
                callsSchedule[2][i] = splitCallsString(Other[i]);
                callsSchedule[3][i] = splitCallsString(Other[i]);
                callsSchedule[4][i] = splitCallsString(Other[i]);
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
            String text = new String(buffer).replace("\r", "");

            String[] timetableBlocks = text.split("========\n");

            // структура    номер дня недели; группа; тип недели (верхняя / нижняя); какая пара по счету
            timetable = new Pair[5][2][2][5];

            // цикл по дням неледи
            for (int i = 0; i < timetableBlocks.length; i++) {
                String[] string = timetableBlocks[i].split("\n");
                // цикл по блокам занятий
                for (int j = 0; j < string.length; j = j + 6) {
                    String[] groopAndWeekList = string[j].split(" ");
                    // цикл по вариантам группа/неделя
                    for (int k = 0; k < groopAndWeekList.length; k++) {
                        Integer g = Integer.parseInt(groopAndWeekList[k].split("")[0]) - 1;
                        Integer w = Integer.parseInt(groopAndWeekList[k].split("")[1]) - 1;
                        Integer p = Integer.parseInt(string[j + 1]) - 1;
                        
                        timetable[i][g][w][p] = new Pair(
                                string[j + 2],
                                string[j + 3],
                                string[j + 4],
                                Integer.parseInt(string[j + 5]),
                                callsSchedule[i][p][0],
                                callsSchedule[i][p][1]);
                    }
                }

            }
            
//                Log.d(LogTag, MyTime.timesFormatString(callSchedule[0][1][1][1][0]));
//                Log.d(LogTag, MyTime.timesFormatString(callSchedule[0][1][1][1][1]));

//                setTimetable();
//
//                initializeCalls();
//                startNewTimer();
        } catch (Exception ex) {
            Log.e(LogTag, Log.getStackTraceString(ex));
        }
    }
    

    // создание вкладок с расписанием
    public void createTimetablePages() {
        pager = findViewById(R.id.pager);
        FragmentStateAdapter pageAdapter = new MyAdapter(this, timetable, group, week);
        pager.setAdapter(pageAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                tab.setText(tabsName[position] + "\n" + weekDays[position] + " " + monthName[weekMonth[position]]);
            }
        });
        tabLayoutMediator.attach();

        // слушатель смены вкладок
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

        //
        pager.setCurrentItem(currentTab, false);
        buttonToCurrentDate.setEnabled(false);
    }


//    String label;
//    Integer curPair = 0;
//    ;
//    MyTime cur;
//    MyTime timeStart;
//    MyTime timeEnd;
//    Handler handler = new Handler();
//
//    Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            cur = new MyTime(
//                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
//                    Calendar.getInstance().get(Calendar.MINUTE),
//                    Calendar.getInstance().get(Calendar.SECOND)
//            );
//
//            if (MyTime.isBetweenTimes(cur, timeStart, timeEnd)) {
//                textViewTimer.setText(label + MyTime.subtractionTimesFormatString(timeEnd, cur));
//                EventBus.getDefault().post(new MessageEvent(true, curPair, currentDayNum));
//                doCicleTimer();
//            } else {
//                if (curPair != 0) {
//                    EventBus.getDefault().post(new MessageEvent(false, curPair, currentDayNum));
//                }
//                startNewTimer();
//            }
//        }
//    };
//
//
//    // таймер до начала/конца пары
//    public void doCicleTimer() {
//        handler.postDelayed(runnable, 250);
//    }
//
//
//    public void startNewTimer() {
//        if (currentDayNum == 5 || currentDayNum == 6) {
//            textViewTimer.setText("Сейчас нет пар");
//            return;
//        }
//
//        cur = new MyTime(
//                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
//                Calendar.getInstance().get(Calendar.MINUTE),
//                Calendar.getInstance().get(Calendar.SECOND)
//        );
//
//        boolean started = false;
//
//        for (int i = 0; i < (10 - calls_type * 2) - 1; i++) {
//            if (MyTime.isBetweenTimes(cur, callsTime[calls_type][i], callsTime[calls_type][i + 1])) {
//                if (i % 2 == 0) {
//                    label = "До конца пары:\n";
//                    curPair = i / 2 + 1;
//                } else {
//                    curPair = 0;
//                    label = "До начала пары:\n";
//                }
//                timeStart = callsTime[calls_type][i];
//                timeEnd = callsTime[calls_type][i + 1];
//                started = true;
//                doCicleTimer();
//                break;
//
//            }
//        }
//
//        if (!started) {
//            textViewTimer.setText("Сейчас нет пар");
//        }
//    }


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
//                setTimetable();
                break;

            case R.id.buttonToCurrentDate:
                updateDateVariables(true);
//                setTimetable();
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
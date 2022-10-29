package com.example.timetable1mok;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String LOG_TAG = "MyApp";
    static final Integer[] DAYS_OF_WEEK = {6, 0, 1, 2, 3, 4, 5};
    static final Integer[] TABS = {0, 1, 2, 3, 4, 0, 0};
    static final String[] TABS_NAMES = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};
    static final String[] MONTHS_NAMES = {"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};

    SharedPreferences settings;
    SharedPreferences.Editor prefEditor;

    Button buttonGroup;
    TextView textViewTimer;
    TextView textViewCurWeek;
    Button buttonToCurrentDate;
    ViewPager2 pager;
    TabLayout tabLayout;

    Pair[][][][] timetable;
    MyTime[][][] callsSchedule;

    Integer lastHighlightWeek = -1;
    Integer lastHighlightDay = -1;
    Integer lastHighlightPair = -1;
    Integer lastHighlightBreak = -1;

    Calendar curCalendar;           // обьект текущей даты
    Calendar changeCalendar;        // обьект выбраной даты
    Integer curWeekType;            // текущая неделя верхняя или нижняя
    Integer changeWeekType;         // выбраная неделя верхняя или нижняя
    Integer curWeekNum;             // номер текущей недели
    Integer changeWeekNum;          // номер выбраной недели
    Integer curDayOfWeek;           // текущий день недели
    Integer changeDayOfWeek;        // выбраный день недели
    Integer[] changeWeekDays;       // список дней выбраной недели
    Integer[] changeWeekMonth;      // список месяцев дней выбраной недели
    Integer changeGroup;            // выбраная группа
    Integer changeTab = 0;          // выбраная вкладка

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        buttonGroup = findViewById(R.id.buttonGroup);
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewCurWeek = findViewById(R.id.textViewCurWeek);
        buttonToCurrentDate = findViewById(R.id.buttonToCurrentDate);

        settings = getSharedPreferences("Settings", MODE_PRIVATE);
        prefEditor = settings.edit();
        changeGroup = settings.getInt("changeGroup", 2);
        buttonGroup.setText(String.valueOf(changeGroup));

        curCalendar = Calendar.getInstance();
        updCurDateVar();
        changeCalendar = Calendar.getInstance();
        if (DAYS_OF_WEEK[changeCalendar.get(Calendar.DAY_OF_WEEK) - 1] > 4) {
            changeCalendar.add(Calendar.DAY_OF_YEAR, 7);
        }
        updChangeDateVar();

        getCalls();
        getTimetable();
        createTimetablePages();
        setTimetable();

        timer();
    }

    @Override
    public void onPause() {
        super.onPause();

        prefEditor.putInt("changeGroup", changeGroup);
        prefEditor.apply();
    }

    public void updCurDateVar() {
        if (curCalendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            curWeekType = 2;
        } else {
            curWeekType = 1;
        }

        curWeekNum = curCalendar.get(Calendar.WEEK_OF_YEAR);
        curDayOfWeek = DAYS_OF_WEEK[curCalendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    public void updChangeDateVar() {
        changeDayOfWeek = DAYS_OF_WEEK[changeCalendar.get(Calendar.DAY_OF_WEEK) - 1];

        if (changeCalendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            changeWeekType = 2;
            textViewCurWeek.setText("Нижняя неделя");
        } else {
            changeWeekType = 1;
            textViewCurWeek.setText("Верхняя неделя");
        }

        changeWeekNum = changeCalendar.get(Calendar.WEEK_OF_YEAR);

        changeWeekDays = new Integer[7];
        changeWeekMonth = new Integer[7];
        for (int d = 0; d < 7; d++) {
            changeCalendar.set(Calendar.DAY_OF_WEEK, d + 2);
            changeWeekDays[d] = changeCalendar.get(Calendar.DAY_OF_MONTH);
            changeWeekMonth[d] = changeCalendar.get(Calendar.MONTH);
        }
    }

    // получение времени начала/конца пары из строки
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
            Log.e(LOG_TAG, Log.getStackTraceString(ex));
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
                    String[] groupAndWeekList = string[j].split(" ");
                    // цикл по вариантам группа/неделя
                    for (int k = 0; k < groupAndWeekList.length; k++) {
                        int g = Integer.parseInt(groupAndWeekList[k].split("")[0]) - 1;
                        int w = Integer.parseInt(groupAndWeekList[k].split("")[1]) - 1;
                        int p = Integer.parseInt(string[j + 1]) - 1;

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
        } catch (Exception ex) {
            Log.e(LOG_TAG, Log.getStackTraceString(ex));
        }
    }

    // создание вкладок с расписанием
    public void createTimetablePages() {
        pager = findViewById(R.id.pager);
        FragmentStateAdapter pageAdapter = new MyAdapter(this);
        pager.setAdapter(pageAdapter);

        tabLayout = findViewById(R.id.tab_layout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                tab.setText(TABS_NAMES[position] + "\n" + changeWeekDays[position] + " " + MONTHS_NAMES[changeWeekMonth[position]]);
            }
        });
        tabLayoutMediator.attach();

        pager.setCurrentItem(TABS[changeDayOfWeek], false);
    }

    public void setTimetable() {
        EventBus.getDefault().postSticky(new SetTimetableEvent(timetable, changeGroup, changeWeekType, changeWeekNum, lastHighlightWeek, lastHighlightDay, lastHighlightPair, lastHighlightBreak));
    }

    // обновление заголовков вкладок
    public void updateTabsNames() {
        for (int i = 0; i < 5; i++) {
            tabLayout.getTabAt(i).setText(TABS_NAMES[i] + "\n" + changeWeekDays[i] + " " + MONTHS_NAMES[changeWeekMonth[i]]);
        }
    }

    MyTime curTime;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                curCalendar = Calendar.getInstance();
                updCurDateVar();

                curTime = new MyTime(
                        curCalendar.get(Calendar.HOUR_OF_DAY),
                        curCalendar.get(Calendar.MINUTE),
                        curCalendar.get(Calendar.SECOND)
                );

                label1:
                {
                    int dayOfWeek = curDayOfWeek;
                    int dayDelta = 0;
                    int weekType = curWeekType;
                    int weekNum = curWeekNum;
                    while (true) {
                        if (dayOfWeek > 4) {
                            dayOfWeek = 0;
                            dayDelta += (7 - dayOfWeek);

                            if (weekType == 1) {
                                weekType = 2;
                            } else {
                                weekType = 1;
                            }
                            weekNum += 1;
                        }
                        for (int pair = 0; pair < 5; pair++) {
                            if (timetable[dayOfWeek][changeGroup - 1][weekType - 1][pair] != null) {
                                if (MyTime.subtractionTimesSecond(
                                        MyTime.additionTimes(
                                                timetable[dayOfWeek][changeGroup - 1][weekType - 1][pair].getTimeStart(),
                                                new MyTime(dayDelta, 0, 0, 0
                                                )), curTime) >= 0) {
                                    if (lastHighlightWeek != weekNum || lastHighlightDay != dayOfWeek || lastHighlightPair != -1 || lastHighlightBreak != pair) {
                                        lastHighlightWeek = weekNum;
                                        lastHighlightDay = dayOfWeek;
                                        lastHighlightPair = -1;
                                        lastHighlightBreak = pair;

                                        setTimetable();
                                    }
                                    break label1;
                                }
                                if (MyTime.subtractionTimesSecond(
                                        MyTime.additionTimes(
                                                timetable[dayOfWeek][changeGroup - 1][weekType - 1][pair].getTimeEnd(),
                                                new MyTime(dayDelta, 0, 0, 0
                                                )), curTime) >= 0) {
                                    if (lastHighlightWeek != weekNum || lastHighlightDay != dayOfWeek || lastHighlightPair != pair || lastHighlightBreak != -1) {
                                        lastHighlightWeek = weekNum;
                                        lastHighlightDay = dayOfWeek;
                                        lastHighlightPair = pair;
                                        lastHighlightBreak = -1;

                                        setTimetable();
                                    }
                                    break label1;
                                }
                            }
                        }
                        dayOfWeek += 1;
                        dayDelta += 1;
                    }
                }

                changeTab = pager.getCurrentItem();
                if ((curDayOfWeek < 5 && curWeekNum != changeWeekNum) || (curDayOfWeek > 4 && curWeekNum + 1 != changeWeekNum) || TABS[curDayOfWeek] != changeTab) {
                    buttonToCurrentDate.setEnabled(true);
                } else {
                    buttonToCurrentDate.setEnabled(false);
                }

                timer();

            } catch (Exception ex) {
                Log.e(LOG_TAG, Log.getStackTraceString(ex));
            }
        }
    };

    public void timer() {
        handler.postDelayed(runnable, 10);
    }

    // Обработка нажатий кнопок
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonGroup:
                if (changeGroup == 1) {
                    changeGroup = 2;
                } else {
                    changeGroup = 1;
                }

                buttonGroup.setText(String.valueOf(changeGroup));
                setTimetable();
                break;

            case R.id.buttonToCurrentDate:
                changeCalendar = Calendar.getInstance();
                if (DAYS_OF_WEEK[changeCalendar.get(Calendar.DAY_OF_WEEK) - 1] > 4) {
                    changeCalendar.add(Calendar.DAY_OF_YEAR, 7);
                }
                updChangeDateVar();
                updateTabsNames();
                pager.setCurrentItem(TABS[curDayOfWeek], true);
                setTimetable();
                break;

            case R.id.buttonWeekUp:
                changeCalendar.add(Calendar.DAY_OF_YEAR, 7);
                updChangeDateVar();
                updateTabsNames();
                setTimetable();
                break;

            case R.id.buttonWeekDown:
                changeCalendar.add(Calendar.DAY_OF_YEAR, -7);
                updChangeDateVar();
                updateTabsNames();
                setTimetable();
                break;

            default:
                break;
        }
    }
}
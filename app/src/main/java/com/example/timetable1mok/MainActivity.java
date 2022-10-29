package com.example.timetable1mok;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    static final String LOG_TAG = "MyApp";
    static final Integer[] DAYS_OF_WEEK = {5, 6, 0, 1, 2, 3, 4};
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

    Integer curHighlightDay = -1;
    Integer curHighlightPair = -1;
    Integer curHighlightBreak = -1;
    Integer newHighlightDay = -1;
    Integer newHighlightPair = -1;
    Integer newHighlightBreak = -1;

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
        updChangeDateVar();

        getCalls();
        getTimetable();
        createTimetablePages();
        setTimetable();

//        curDateUpd();
        timer();
    }

    @Override
    public void onPause() {
        super.onPause();

        prefEditor.putInt("changeGroup", changeGroup);
        prefEditor.apply();
    }

    public void updCurDateVar() {
        if (changeCalendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            curWeekType = 2;
        } else {
            curWeekType = 1;
        }

        curWeekNum = changeCalendar.get(Calendar.WEEK_OF_YEAR);
        curDayOfWeek = DAYS_OF_WEEK[changeCalendar.get(Calendar.DAY_OF_WEEK)];
    }

    public void updChangeDateVar() {
        if (changeCalendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            changeWeekType = 2;
            textViewCurWeek.setText("Нижняя неделя");
        } else {
            changeWeekType = 1;
            textViewCurWeek.setText("Верхняя неделя");
        }

        changeWeekNum = changeCalendar.get(Calendar.WEEK_OF_YEAR);
        changeDayOfWeek = DAYS_OF_WEEK[changeCalendar.get(Calendar.DAY_OF_WEEK)];

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

    // установка расписания
    public void setTimetable() {
        EventBus.getDefault().postSticky(new SetTimetableEvent(timetable, changeGroup, changeWeekType));
    }

    public void setHighlighting(Integer highlightDay, Integer highlightPair, Integer highlightBreak) {
        EventBus.getDefault().postSticky(new SetHighlightingEvent(highlightDay, highlightPair, highlightBreak));
    }

    public void deleteHighlighting() {
        EventBus.getDefault().postSticky(new DeleteHighlightingEvent());
    }

    // обновление заголовков вкладок
    public void updateTabsNames() {
        for (int i = 0; i < 5; i++) {
            tabLayout.getTabAt(i).setText(TABS_NAMES[i] + "\n" + changeWeekDays[i] + " " + MONTHS_NAMES[changeWeekMonth[i]]);
        }
    }

    MyTime cur;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            curCalendar = Calendar.getInstance();
            updCurDateVar();



//            cur = new MyTime(
//                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
//                    Calendar.getInstance().get(Calendar.MINUTE),
//                    Calendar.getInstance().get(Calendar.SECOND)
//            );

//            Log.d(LOG_TAG, "");

            MyTime nextTime;
            boolean set = false;
            if (changeDayOfWeek < 5) {
                for (int d = changeDayOfWeek; d < 5; d++) {
                    for (int i = 0; i < 5; i++) {
                        if (!set) {
                            if (timetable[d][changeGroup - 1][curWeekType - 1][i] != null) {
                                nextTime = MyTime.additionTimes(timetable[d][changeGroup - 1][curWeekType - 1][i].getTimeStart(), new MyTime(d - changeDayOfWeek, 0, 0, 0));
                                if (MyTime.subtractionTimesSecond(nextTime, cur) > 0) {
                                    textViewTimer.setText("До начала пары:\n" + MyTime.subtractionTimesFormatString(nextTime, cur));
                                    set = true;
                                } else {
                                    nextTime = MyTime.additionTimes(timetable[d][changeGroup - 1][curWeekType - 1][i].getTimeEnd(), new MyTime(d - changeDayOfWeek, 0, 0, 0));
                                    if (MyTime.subtractionTimesSecond(nextTime, cur) > 0) {
                                        textViewTimer.setText("До конца пары:\n" + MyTime.subtractionTimesFormatString(nextTime, cur));
                                        set = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            newHighlightDay = -1;
            newHighlightPair = -1;
            if (changeDayOfWeek < 5) {
                for (int i = 0; i < 5; i++) {
                    if (timetable[changeDayOfWeek][changeGroup - 1][curWeekType - 1][i] != null) {
                        if (MyTime.isBetweenTimes(cur, timetable[changeDayOfWeek][changeGroup - 1][curWeekType - 1][i].getTimeStart(), timetable[changeDayOfWeek][changeGroup - 1][curWeekType - 1][i].getTimeEnd())) {
                            newHighlightDay = changeDayOfWeek;
                            newHighlightPair = i;
                            break;
                        }
                    }
                }
            }

            if (newHighlightDay != curHighlightDay || newHighlightPair != curHighlightPair) {
                curHighlightDay = newHighlightDay;
                curHighlightPair = newHighlightPair;
                setHighlighting(curHighlightDay, curHighlightPair, -1);
            }

            timer();
        }
    };

    public void timer() {
        handler.postDelayed(runnable, 10);
    }

//    Handler handlerCurDateUpd = new Handler();
//    Runnable runnableCurDateUpd = new Runnable() {
//        @Override
//        public void run() {
//            curWeekNum = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
//            curDayOfWeek = DAYS_OF_WEEK[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)];
//
//            changeTab = pager.getCurrentItem();
//
//            if (curWeekNum == changeWeekNum && TABS[curDayOfWeek] == changeTab) {
//                buttonToCurrentDate.setEnabled(false);
//            } else {
//                buttonToCurrentDate.setEnabled(true);
//            }
//
//            curDateUpd();
//        }
//    };
//
//    public void curDateUpd() {
//        handlerCurDateUpd.postDelayed(runnableCurDateUpd, 10);
//    }

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
                updChangeDateVar();
                updateTabsNames();
                pager.setCurrentItem(TABS[changeDayOfWeek], true);
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
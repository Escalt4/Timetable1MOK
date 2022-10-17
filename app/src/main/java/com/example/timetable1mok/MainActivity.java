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

    Calendar calendar;
    Integer week;
    Integer weekNum;
    Integer curWeekNum;
    Integer dayOfWeek;
    Integer curDayOfWeek;
    Integer[] curWeekDays;
    Integer[] curWeekMonth;

    Integer group;

    Integer curTab = 0;

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
        group = settings.getInt("group", 2);
        buttonGroup.setText(group + "");


        weekNum = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        dayOfWeek = DAYS_OF_WEEK[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)];
        calendar = Calendar.getInstance();
        updDateVar();

        getCalls();
        getTimetable();
        createTimetablePages();

        curDateUpd();
        timer();
    }

    @Override
    public void onPause() {
        super.onPause();

        prefEditor.putInt("group", group);
        prefEditor.apply();
    }

    public void updDateVar() {
        if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            week = 2;
            textViewCurWeek.setText("Нижняя неделя");
        } else {
            textViewCurWeek.setText("Верхняя неделя");
            week = 1;
        }

        curWeekNum = calendar.get(Calendar.WEEK_OF_YEAR);
        curDayOfWeek = DAYS_OF_WEEK[calendar.get(Calendar.DAY_OF_WEEK)];

        curWeekDays = new Integer[7];
        curWeekMonth = new Integer[7];
        for (int d = 0; d < 7; d++) {
            calendar.set(Calendar.DAY_OF_WEEK, d + 2);
            curWeekDays[d] = calendar.get(Calendar.DAY_OF_MONTH);
            curWeekMonth[d] = calendar.get(Calendar.MONTH);
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
                tab.setText(TABS_NAMES[position] + "\n" + curWeekDays[position] + " " + MONTHS_NAMES[curWeekMonth[position]]);
            }
        });
        tabLayoutMediator.attach();

        pager.setCurrentItem(TABS[curDayOfWeek], false);

        sendTimetableUpdate();
        sendHighlightUpdate();
    }

    public void sendTimetableUpdate() {
        EventBus.getDefault().postSticky(new UpdateTimetableEvent(timetable, group, week));
    }

    public void sendHighlightUpdate() {
        sendHighlightUpdate(-1, -1, -1);
    }

    public void sendHighlightUpdate(Integer highlightDay, Integer highlightPair, Integer highlightBreak) {
        EventBus.getDefault().postSticky(new UpdateHighlightEvent(highlightDay, highlightPair, highlightBreak));
    }

    // обновление заголовков вкладок
    public void updateTabsNames() {
        for (int i = 0; i < 5; i++) {
            tabLayout.getTabAt(i).setText(TABS_NAMES[i] + "\n" + curWeekDays[i] + " " + MONTHS_NAMES[curWeekMonth[i]]);
        }
    }

    MyTime cur;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            Calendar temp = Calendar.getInstance();
//            if (temp.get(Calendar.HOUR_OF_DAY) > 17) {
//                temp.add(Calendar.DAY_OF_YEAR, 1);
//            }
//            if (temp.get(Calendar.DAY_OF_MONTH) != curDay) {
//                updDateVar(true);
//                buttonToCurrentDate.setEnabled(true);
//            }

            cur = new MyTime(
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    Calendar.getInstance().get(Calendar.SECOND)
            );

//            Log.d(LOG_TAG, "");

            MyTime nextTime;
            boolean set = false;
            if (curDayOfWeek < 5) {
                for (int d = curDayOfWeek; d < 5; d++) {
                    for (int i = 0; i < 5; i++) {
                        if (!set) {
                            if (timetable[d][group - 1][week - 1][i] != null) {
                                nextTime = MyTime.additionTimes(timetable[d][group - 1][week - 1][i].getTimeStart(), new MyTime(d - curDayOfWeek, 0, 0, 0));
                                if (MyTime.subtractionTimesSecond(nextTime, cur) > 0) {
                                    textViewTimer.setText("До начала пары:\n" + MyTime.subtractionTimesFormatString(nextTime, cur));
                                    set = true;
                                } else {
                                    nextTime = MyTime.additionTimes(timetable[d][group - 1][week - 1][i].getTimeEnd(), new MyTime(d - curDayOfWeek, 0, 0, 0));
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
            if (curDayOfWeek < 5) {
                for (int i = 0; i < 5; i++) {
                    if (timetable[curDayOfWeek][group - 1][week - 1][i] != null) {
                        if (MyTime.isBetweenTimes(cur, timetable[curDayOfWeek][group - 1][week - 1][i].getTimeStart(), timetable[curDayOfWeek][group - 1][week - 1][i].getTimeEnd())) {
                            newHighlightDay = curDayOfWeek;
                            newHighlightPair = i;
                            break;
                        }
                    }
                }
            }

            if (newHighlightDay != curHighlightDay || newHighlightPair != curHighlightPair) {
                curHighlightDay = newHighlightDay;
                curHighlightPair = newHighlightPair;
                sendHighlightUpdate(curHighlightDay, curHighlightPair, -1);
            }

            timer();
        }
    };

    // переключение на другой день
//    if (calendar.get(Calendar.HOUR_OF_DAY) > 17) {
//        calendar.add(Calendar.DAY_OF_YEAR, 1);
//    }

    public void timer() {
        handler.postDelayed(runnable, 10);
    }

    Handler handlerCurDateUpd = new Handler();
    Runnable runnableCurDateUpd = new Runnable() {
        @Override
        public void run() {
            weekNum = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
            dayOfWeek = DAYS_OF_WEEK[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)];

            curTab = pager.getCurrentItem();

            if (weekNum == curWeekNum && TABS[dayOfWeek] == curTab) {
                buttonToCurrentDate.setEnabled(false);
            } else {
                buttonToCurrentDate.setEnabled(true);
            }

            curDateUpd();
        }
    };

    public void curDateUpd() {
        handlerCurDateUpd.postDelayed(runnableCurDateUpd, 10);
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

                buttonGroup.setText(String.valueOf(group));
                sendTimetableUpdate();
                break;

            case R.id.buttonToCurrentDate:
                calendar = Calendar.getInstance();
                updDateVar();
                updateTabsNames();
                pager.setCurrentItem(TABS[curDayOfWeek]);
                sendTimetableUpdate();
                break;

            case R.id.buttonWeekUp:
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                updDateVar();
                updateTabsNames();
                sendTimetableUpdate();
                break;

            case R.id.buttonWeekDown:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                updDateVar();
                updateTabsNames();
                sendTimetableUpdate();
                break;

            default:
                break;
        }
    }
}
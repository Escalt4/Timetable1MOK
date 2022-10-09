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
    String LogTag = "MyApp";
    Integer[] dayOfWeek = {5,6, 0, 1, 2, 3, 4};
    String[] tabsName = {"ПН", "ВТ", "СР", "ЧТ", "ПТ"};
    String[] monthName = {"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};

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

    Boolean highlight = false;
    Integer curHighlightDay = -1;
    Integer curHighlightPair = -1;
    Integer newHighlightDay = -1;
    Integer newHighlightPair = -1;

    Calendar calendar;
    Integer group;
    Integer curDay;
    Integer curDayOfWeek;
    Integer week;
    Integer[] weekDays;
    Integer[] weekMonth;
    Integer curTab;
    Boolean changedWeek = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        buttonGroup = findViewById(R.id.buttonGroup);
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewCurWeek = findViewById(R.id.textViewCurWeek);
        buttonToCurrentDate = findViewById(R.id.buttonToCurrentDate);

        // востановление настроек
        settings = getSharedPreferences("Settings", MODE_PRIVATE);
        prefEditor = settings.edit();
        group = settings.getInt("group", 2);
        buttonGroup.setText(group + "");

        // определение переменных связаных с датой
        updDateVar(true);

        // получение расписания из файла
        getCalls();
        getTimetable();

        // создание вкладок с расписанием
        createTimetablePages();

        timer();
    }

    @Override
    public void onPause() {
        super.onPause();

        // сохранение настроек
        prefEditor.putInt("group", group);
        prefEditor.apply();
    }

    // определение переменных связаных с датой
    public void updDateVar(boolean CurDate) {
        if (CurDate) {
            calendar = Calendar.getInstance();
//            if (calendar.get(Calendar.HOUR_OF_DAY) > 17) {
//                calendar.add(Calendar.DAY_OF_YEAR, 1);
//            }
            curDay = calendar.get(Calendar.DAY_OF_MONTH);
        }

        curDayOfWeek = dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)];

        if (CurDate) {
            if (curDayOfWeek < 5) {
                curTab = curDayOfWeek;
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                curTab = 0;
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
        } catch (Exception ex) {
            Log.e(LogTag, Log.getStackTraceString(ex));
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
                tab.setText(tabsName[position] + "\n" + weekDays[position] + " " + monthName[weekMonth[position]]);
            }
        });
        tabLayoutMediator.attach();

        // слушатель смены вкладок
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (curTab == tabLayout.getSelectedTabPosition() && !changedWeek) {
                    buttonToCurrentDate.setEnabled(false);
                } else {
                    buttonToCurrentDate.setEnabled(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        pager.setCurrentItem(curTab, false);

        sendInfo(-1, -1);
        highlight = false;
    }

    // отправка информации в вкладки
    public void sendInfo(Integer highlightDay, Integer highlightPair) {
        EventBus.getDefault().postSticky(new MessageEvent(timetable, group, week, highlightDay, highlightPair));
    }

    // обновление заголовков вкладок
    public void updateTabsNames() {
        for (int i = 0; i < 5; i++) {
            tabLayout.getTabAt(i).setText(tabsName[i] + "\n" + weekDays[i] + " " + monthName[weekMonth[i]]);
        }
    }

    MyTime cur;

    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Calendar temp = Calendar.getInstance();
            if (temp.get(Calendar.HOUR_OF_DAY) > 17) {
                temp.add(Calendar.DAY_OF_YEAR, 1);
            }
            if (temp.get(Calendar.DAY_OF_MONTH) != curDay) {
                updDateVar(true);
                buttonToCurrentDate.setEnabled(true);
                highlight = false;
            }

            cur = new MyTime(
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    Calendar.getInstance().get(Calendar.SECOND)
            );

//            Log.d(LogTag, "");

            MyTime nextTime;
            Boolean set = false;
//            Log.d(LogTag, curDayOfWeek+"");
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
            }}

            newHighlightDay = -1;
            newHighlightPair = -1;
            for (int i = 0; i < 5; i++) {
                if (curDayOfWeek < 5) {
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
                sendInfo(curHighlightDay, curHighlightPair);
            }

            timer();

//                if (MyTime.isBetweenTimes(cur, callsTime[calls_type][i], callsTime[calls_type][i + 1])) {
//
//
//
//            if (MyTime.isBetweenTimes(cur, timeStart, timeEnd)) {
//                textViewTimer.setText(label + MyTime.subtractionTimesFormatString(timeEnd, cur));
//                EventBus.getDefault().post(new MessageEvent(true, curPair, curDayOfWeek));
//                doCicleTimer();
//            } else {
//                if (curPair != 0) {
//                    EventBus.getDefault().post(new MessageEvent(false, curPair, curDayOfWeek));
//                }
//                startNewTimer();
        }
    };

    public void timer() {
        handler.postDelayed(runnable, 10);
    }


//    public void timer234() {


//        if (curDayOfWeek == 5 || curDayOfWeek == 6) {
//            textViewTimer.setText("Сейчас нет пар");
//            return;
//        }

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

                buttonGroup.setText(String.valueOf(group));
                sendInfo(-1, -1);
                highlight = false;
                break;

            case R.id.buttonToCurrentDate:
                updDateVar(true);
                sendInfo(-1, -1);
                highlight = false;
                updateTabsNames();
                pager.setCurrentItem(curTab);
                buttonToCurrentDate.setEnabled(false);
                changedWeek = false;
                break;

            case R.id.buttonWeekUp:
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                updDateVar(false);
                sendInfo(-1, -1);
                highlight = false;
                updateTabsNames();
                buttonToCurrentDate.setEnabled(true);
                changedWeek = true;
                break;

            case R.id.buttonWeekDown:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                updDateVar(false);
                sendInfo(-1, -1);
                highlight = false;
                updateTabsNames();
                buttonToCurrentDate.setEnabled(true);
                changedWeek = true;
                break;

            default:
                break;
        }
    }
}
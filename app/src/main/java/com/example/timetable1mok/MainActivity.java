package com.example.timetable1mok;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    SharedPreferences settings;
    SharedPreferences.Editor prefEditor;

    String DATABASE_FILE_NAME = "DATABASE";
    String LogTag = "MyApp";
    String[] tabsName = {"ПН", "ВТ", "СР", "ЧТ", "ПТ"};

    ViewPager2 pager;
    Calendar calendar;

    Integer changedGroup = 2; // выбраная группа
    Integer changedWeek = 2;

    Integer currentDay; // день недели числом 0 1 2 3 4 5 6
    Integer currentWeek; // четная или нечетная неделя числом 0 1
    Integer currentMonth; // месяц числом
    Integer[] currentWeekDays; // список дней текущей недели

    Integer currentTab = 0; // текущая вкладка


    String[][][][][] timetable;
    String[][] calls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        getCurrentWeekDay();

//        settings = getSharedPreferences("Settings", MODE_PRIVATE);
//        prefEditor = settings.edit();
//        changedGroup = settings.getInt("changedGroup", 2);
//        changedWeek = settings.getInt("changedWeek", currentWeek);


        getTimetable();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        prefEditor.putInt("changedGroup", changedGroup);
//        prefEditor.putInt("changedWeek", changedWeek);
//    }


    public void getCurrentWeekDay() {
        calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            currentWeek = 2;
        } else {
            currentWeek = 1;
        }
        Integer[] dayOfWeek = {123, 6, 0, 1, 2, 3, 4, 5};
        currentDay = dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)];

        currentWeekDays = new Integer[7];
        for (int d = 0; d < 7; d++) {
            calendar.set(Calendar.DAY_OF_WEEK, d + 2);
            currentWeekDays[d] = calendar.get(Calendar.DAY_OF_MONTH);
        }

    }

//    public void openFile() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("*/*");
//        startActivityForResult(intent, 0);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        super.onActivityResult(requestCode, resultCode, resultData);
//        if (resultData != null && resultCode == Activity.RESULT_OK) {
//            Uri uri = resultData.getData();
//            getTimetable(uri);
//        }
//    }

    public void getTimetable() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.timetable_1mok);
//            FileInputStream inputStream = openFileInput(DATABASE_FILE_NAME);
//            InputStream inputStream = getContentResolver().openInputStream(uri);

            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String text = new String(buffer);

//            FileOutputStream fos = openFileOutput(DATABASE_FILE_NAME, MODE_PRIVATE);
//            fos.write(text.getBytes());
//            fos.close();

            String[] timetableBlocks = text.split("========");

            timetable = new String[5][2][2][5][3];
            calls = new String[2][5];

            for (int i = 0; i < timetableBlocks.length; i++) {
                String[] blocksPart = timetableBlocks[i].split("--------");

                if (i == 0) {
                    for (int j = 1; j < blocksPart.length; j++) {
                        String[] strings = blocksPart[j].split("\n");

                        if (j == 1) {
                            for (int k = 1; k < strings.length; k++) {
                                calls[0][k - 1] = strings[k].replace("\"", "");
                            }
                        } else {
                            for (int k = 1; k < strings.length; k++) {
                                calls[1][k - 1] = strings[k].replace("\"", "");
                            }
                        }
                    }

                } else {
                    for (int j = 1; j < blocksPart.length; j++) {
                        String[] strings = blocksPart[j].split("\n");

                        for (int k = 1; k < strings.length; k = k + 5) {
                            String[] temp1 = strings[k].split(" ");

                            for (int q = 0; q < temp1.length; q++) {
                                int groop_ = Integer.parseInt(Character.toString(temp1[q].charAt(0))) - 1;
                                int week_ = Integer.parseInt(Character.toString(temp1[q].charAt(1))) - 1;
                                int day_ = Integer.parseInt(strings[k + 1].replace("\r", "")) - 1;

                                for (int w = 0; w < 3; w++) {
                                    timetable[i - 1]
                                            [groop_]
                                            [week_]
                                            [day_]
                                            [w] = strings[k + 2 + w];
                                }
                            }
                        }
                    }
                }
            }

            setTimetable();

        } catch (Exception ex) {
            Log.e(LogTag, ex.getMessage());
        }

    }


    void setTimetable() {
        pager = findViewById(R.id.pager);
        FragmentStateAdapter pageAdapter = new MyAdapter(this, timetable, calls, changedGroup, calendar, changedWeek, currentDay, currentMonth, currentWeekDays);
        pager.setAdapter(pageAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                tab.setText(tabsName[position]+"\n"+currentWeekDays[position]);
            }
        });
        tabLayoutMediator.attach();

        pager.setCurrentItem(currentTab, false);
        if (currentDay < 5) {
            pager.setCurrentItem(currentDay, false);
            currentTab = currentDay;
        }
    }

    String startDate;
    String endDate;
    String currentWeek_;
    TextView textViewCurWeek;

    void changeWeek() {
        currentWeekDays = new Integer[7];
        for (int d = 0; d < 7; d++) {
            calendar.set(Calendar.DAY_OF_WEEK, d + 2);
            currentWeekDays[d] = calendar.get(Calendar.DAY_OF_MONTH);
        }

        calendar.set(Calendar.DAY_OF_WEEK, 2);
        startDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        calendar.set(Calendar.DAY_OF_WEEK, 2 + 6);
        endDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", calendar.get(Calendar.MONTH) + 1);

        textViewCurWeek = findViewById(R.id.textViewCurWeek);
        if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            changedWeek = 2;
            textViewCurWeek.setText(startDate + " - " + endDate + " нижняя неделя");
        } else {
            textViewCurWeek.setText(startDate + " - " + endDate + " верхняя неделя");
            changedWeek = 1;
        }

        currentTab = pager.getCurrentItem();
        setTimetable();
    }

    // Обработка нажатий кнопок
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonGroup:
                if (changedGroup == 1) {
                    changedGroup = 2;
                } else {
                    changedGroup = 1;
                }

                Button buttonGroup = findViewById(R.id.buttonGroup);
                buttonGroup.setText(changedGroup + " подгруппа");

                currentTab = pager.getCurrentItem();
                setTimetable();
                break;

            case R.id.buttonToCurrentDate:
                calendar = Calendar.getInstance();

                changeWeek();

                break;

            case R.id.buttonWeekUp:
                calendar.add(Calendar.DAY_OF_YEAR, 7);

                changeWeek();

                break;

            case R.id.buttonWeekDown:
                calendar.add(Calendar.DAY_OF_YEAR, -7);

                changeWeek();

                break;

            default:
                break;
        }
    }
}
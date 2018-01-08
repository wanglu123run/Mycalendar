package com.application.mycalendar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.calendar.OnCalendarClickListener;
import com.calendar.schedule.ScheduleLayout;
import com.calendar.schedule.ScheduleRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnCalendarClickListener {

    private List<String> mServiceInfoData;
    private String mCurrentdate;//当前日期

    private ScheduleLayout slSchedule;
    private ScheduleRecyclerView rvScheduleList;
    private Map<String, List<String>> mMonthHintMap;

    private TextView tv_myschedule_year;
    private TextView tv_myschedule_month;
    private ScheduleAdapter mScheduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    protected void initView() {
        ImageView iv_myschedule_left = (ImageView) findViewById(R.id.iv_myschedule_left);
        ImageView iv_myschedule_right = (ImageView) findViewById(R.id.iv_myschedule_right);
        tv_myschedule_year = (TextView) findViewById(R.id.tv_myschedule_year);
        tv_myschedule_month = (TextView) findViewById(R.id.tv_myschedule_month);
        slSchedule = (ScheduleLayout) findViewById(R.id.slSchedule);
        slSchedule.setOnCalendarClickListener(this);
        initScheduleList();
        int currentSelectYear = slSchedule.getCurrentSelectYear();
        int currentSelectMonth = slSchedule.getCurrentSelectMonth();
        setTitleDate(currentSelectYear+"",currentSelectMonth+1+"");
        iv_myschedule_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slSchedule.backMonth();//上个月
            }
        });
        iv_myschedule_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slSchedule.nextMonth();//下个月
            }
        });
    }

    private void initScheduleList() {
        rvScheduleList = slSchedule.getSchedulerRecyclerView();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rvScheduleList.setLayoutManager(manager);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        setDatePicker();
        rvScheduleList.setItemAnimator(itemAnimator);
        if (mScheduleAdapter==null) {
            mScheduleAdapter = new ScheduleAdapter(this);
            rvScheduleList.setAdapter(mScheduleAdapter);
        }
    }

    /**
     * 设置小红点数据和底部数据
     */
    private void initData() {
        mMonthHintMap = new HashMap<>();
        int year = slSchedule.getCurrentSelectYear();
        int month = slSchedule.getCurrentSelectMonth();
        List<String> list = new ArrayList<>();
        for (int y = 1; y < 10; y++) {
            if ((month + 1) < 10) {
                list.add(year + "-0" + (month + 1) + "-0" + y);
            } else {
                list.add(year+"-0"+(month+1)+"-0"+y);
            }
        }
        mMonthHintMap.put(year+"-"+month,list);
//        List<String> list = new ArrayList<>();
//        list.add("2018-01-11");
//        mMonthHintMap.put("2018-0",list);
        handler.sendEmptyMessageDelayed(0, 3000);
        List<String> data = new ArrayList<>();
        for(int x=0;x<20;x++) {
            data.add(x+"");
        }
        slSchedule.setRecordState(true);
        mScheduleAdapter.setData(data);
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            slSchedule.setMonthHintMap(mMonthHintMap);
        }
    };

    /**
     * 拼装当前日期
     */
    private void setDatePicker() {
        if (slSchedule == null) {
            return;
        }
        int year = slSchedule.getCurrentSelectYear();
        int month = slSchedule.getCurrentSelectMonth()+1;
        int day = slSchedule.getCurrentSelectDay();
        String monthStr = "";
        if (month < 10) {
            monthStr = "0" + month;
        } else {
            monthStr=month+"";
        }
        String dayStr = "";
        if (day < 10) {
            dayStr = "0" + day;
        } else {
            dayStr=day+"";
        }
        mCurrentdate = year + "-" + monthStr+"-"+dayStr;
    }

    /**
     * 点击日期
     * @param year
     * @param month
     * @param day
     */
    @Override
    public void onClickDate(int year, int month, int day) {
        setTitleDate(String.valueOf(year),String.valueOf(month+1));
        String date =year+"-"+(month+1)+"-"+day;
    }

    /**
     * 页面切换
     * @param year
     * @param month
     * @param day
     */
    @Override
    public void onPageChange(int year, int month, int day) {
        setTitleDate(String.valueOf(year),String.valueOf(month+1));
        mCurrentdate=year+"-"+(month+1)+"-"+day;
    }

    /**
     * 设置标题
     * @param year
     * @param month
     */
    private void setTitleDate(String year, String month) {
        if (TextUtils.isEmpty(year) || TextUtils.isEmpty(month)) {
            return;
        }
        tv_myschedule_year.setText(year+"年");
        tv_myschedule_month.setText(month+"月");
    }
}

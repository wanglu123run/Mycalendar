package com.calendar.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;

import com.application.mycalendar.R;
import com.calendar.OnCalendarClickListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jimmy on 2016/10/6 0006.
 */
public class MonthCalendarView extends ViewPager implements OnMonthClickListener {

    private MonthAdapter mMonthAdapter;
    private OnCalendarClickListener mOnCalendarClickListener;

    public MonthCalendarView(Context context) {
        this(context, null);
    }

    public MonthCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        addOnPageChangeListener(mOnPageChangeListener);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        initMonthAdapter(context, context.obtainStyledAttributes(attrs, R.styleable.MonthCalendarView));
    }

    private void initMonthAdapter(Context context, TypedArray array) {
        mMonthAdapter = new MonthAdapter(context, array, this);
        setAdapter(mMonthAdapter);
        setCurrentItem(mMonthAdapter.getMonthCount() / 2, false);
    }

    /**
     * 日历点击
     * @param year
     * @param month
     * @param day
     */
    @Override
    public void onClickThisMonth(int year, int month, int day) {
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(year, month, day);
        }
    }

    @Override
    public void onClickLastMonth(int year, int month, int day) {
        MonthView monthDateView = mMonthAdapter.getViews().get(getCurrentItem() - 1);
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day);
        }
        setCurrentItem(getCurrentItem() - 1, true);
    }

    @Override
    public void onClickNextMonth(int year, int month, int day) {
        MonthView monthDateView = mMonthAdapter.getViews().get(getCurrentItem() + 1);
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day);
            monthDateView.invalidate();
        }
        onClickThisMonth(year, month, day);
        setCurrentItem(getCurrentItem() + 1, true);
    }


    /**
     * viewpager切换监听
     */
    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(final int position) {
            MonthView monthView = mMonthAdapter.getViews().get(getCurrentItem());
            if (monthView != null) {
                if (mOnCalendarClickListener != null) {
                    mOnCalendarClickListener.onPageChange(monthView.getSelectYear(), monthView.getSelectMonth(), monthView.getSelectDay());
                }
                monthView.clickThisMonth(monthView.getSelectYear(), monthView.getSelectMonth(), monthView.getSelectDay());
            } else {
                MonthCalendarView.this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onPageSelected(position);
                    }
                }, 50);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /**
     * 跳转到今天
     */
    public void setTodayToView() {
        setCurrentItem(mMonthAdapter.getMonthCount() / 2, true);
        MonthView monthView = mMonthAdapter.getViews().get(mMonthAdapter.getMonthCount() / 2);
        if (monthView != null) {
            Calendar calendar = Calendar.getInstance();
            monthView.clickThisMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        }
    }

    /**
     * 设置点击日期监听
     *
     * @param onCalendarClickListener
     */
    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }

    public SparseArray<MonthView> getMonthViews() {
        return mMonthAdapter.getViews();
    }

    public MonthView getCurrentMonthView() {
        return getMonthViews().get(getCurrentItem());
    }

    public MonthAdapter getMonthAdapter() {
        return mMonthAdapter;
    }



    private void setMonthHint(MonthView monthView) {
        if (monthView == null) {
            return;
        }
        int selectYear = monthView.getSelectYear();
        int selectMonth = monthView.getSelectMonth();
//        int selectDay = monthView.getSelectDay();
        int selectDay = 1;
        String date,month,day;
        //2017-05-01 json
//        if (selectMonth < 10) {
//            month = "0" + selectMonth;
//        } else {
//            month=String.valueOf(selectMonth);
//        }
//        if (selectDay < 10) {
//            day = "0" + selectDay;
//        } else {
//            day=String.valueOf(selectDay);
//        }
        date=selectYear+"-"+selectMonth;
        if (mMonthHintMap == null || mMonthHintMap.size() <= 0) {
            return;
        }
        List<String> integers = mMonthHintMap.get(date);
        if (integers == null || integers.size() <= 0) {
            monthView.clearTaskHintList();
        } else {
            monthView.setTaskHintList(integers);
        }
    }

    /**
     * 圆点的集合
     */
    private Map<String, List<String>> mMonthHintMap;//Map<日期,List<Object>> 月视图

    /**
     * 设置圆点提示的总集合
     */
    public void setMonthHintMap(Map<String,List<String>> maphint) {
        if (mMonthAdapter == null) {
            return;
        }
        mMonthHintMap = maphint;
        int postion = getCurrentItem();//当前的
        MonthView monthDateView =mMonthAdapter.getViews().get(postion);
        setMonthHint(monthDateView);
    }

    public void restartMonth() {
        int postion = getCurrentItem();//当前的
        MonthView monthDateView = mMonthAdapter.getViews().get(postion);
//        monthDateView.restartMonth();
        monthDateView.invalidate();
    }


}

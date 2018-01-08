package com.calendar.week;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.application.mycalendar.R;
import com.calendar.OnCalendarClickListener;

import java.util.List;
import java.util.Map;

/**
 * Created by Jimmy on 2016/10/7 0007.
 */
public class WeekCalendarView extends ViewPager implements OnWeekClickListener {

    private OnCalendarClickListener mOnCalendarClickListener;
    private WeekAdapter mWeekAdapter;

    public WeekCalendarView(Context context) {
        this(context, null);
    }

    public WeekCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        addOnPageChangeListener(mOnPageChangeListener);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        initWeekAdapter(context, context.obtainStyledAttributes(attrs, R.styleable.WeekCalendarView));
    }

    private void initWeekAdapter(Context context, TypedArray array) {
        mWeekAdapter = new WeekAdapter(context, array, this);
        setAdapter(mWeekAdapter);
        setCurrentItem(mWeekAdapter.getWeekCount() / 2, false);
    }

    @Override
    public void onClickDate(int year, int month, int day) {
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(year, month, day);
        }
    }

    private float mDownPosition[] = new float[2];

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownPosition[0] = ev.getRawX();
                mDownPosition[1] = ev.getRawY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                float xm = ev.getRawX();
                float ym = ev.getRawY();
                float mX = xm - mDownPosition[0];
                float mY = ym - mDownPosition[1];
                int measuredWidth = getMeasuredWidth()/15;
                if (mX > 0) {
                    //上个月
                    if (Math.abs(mX)> measuredWidth && Math.abs(mY)< getMeasuredWidth()/4) {
                        if (monthJumpListener != null) {
                            monthJumpListener.weekJumpMonthBack();
                        }
                        return true;
                    }
                } else {
                    //下个月
                    if (Math.abs(mX) > measuredWidth && Math.abs(mY)<getMeasuredWidth()/4) {
                        if (monthJumpListener != null) {
                            monthJumpListener.weekJumpMonthNext();
                        }
                        return true;
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
    private MonthJumpListener monthJumpListener;
    public interface MonthJumpListener{
        void weekJumpMonthBack();
        void weekJumpMonthNext();
    }
    public void setMonthJumpListener(MonthJumpListener listener){
        monthJumpListener=listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(final int position) {
            WeekView weekView = mWeekAdapter.getViews().get(position);
            if (weekView != null) {
                if (mOnCalendarClickListener != null) {
                    mOnCalendarClickListener.onPageChange(weekView.getSelectYear(), weekView.getSelectMonth(), weekView.getSelectDay());
                }
                weekView.clickThisWeek(weekView.getSelectYear(), weekView.getSelectMonth(), weekView.getSelectDay());
            } else {
                WeekCalendarView.this.postDelayed(new Runnable() {
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
     * 设置点击日期监听
     *
     * @param onCalendarClickListener
     */
    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }


    public SparseArray<WeekView> getWeekViews() {
        return mWeekAdapter.getViews();
    }

    public WeekAdapter getWeekAdapter() {
        return mWeekAdapter;
    }

    public WeekView getCurrentWeekView() {
        return getWeekViews().get(getCurrentItem());
    }


    private void setMonthHint(WeekView weekView) {
        if (weekView == null) {
            return;
        }
        int selectYear = weekView.getSelectYear();
        int selectMonth = weekView.getSelectMonth();
//        int selectDay = monthView.getSelectDay();
//        int selectDay = 1;
//        String date, month, day;
        //2017-05-01 json
//        if (selectMonth < 10) {
//            month = "0" + selectMonth;
//        } else {
//            month = String.valueOf(selectMonth);
//        }
//        if (selectDay < 10) {
//            day = "0" + selectDay;
//        } else {
//            day = String.valueOf(selectDay);
//        }
        String date = selectYear + "-" + selectMonth;
        if (mWeekHintMap == null || mWeekHintMap.size() <= 0) {
            return;
        }
        List<String> integers = mWeekHintMap.get(date);
        if (integers == null || integers.size() <= 0) {
            weekView.clearTaskHintList();
        } else {
            weekView.setTaskHintList(integers);
        }
    }

    /**
     * 圆点的集合
     */
    private Map<String, List<String>> mWeekHintMap;//Map<日期,List<Object>> 月视图

    /**
     * 设置圆点提示的总集合
     */
    public void setWeekHintMap(Map<String, List<String>> maphint) {
        if (mWeekAdapter == null) {
            return;
        }
        mWeekHintMap = maphint;
        int postion = getCurrentItem();//当前的
        WeekView weekView = mWeekAdapter.getViews().get(postion);
        setMonthHint(weekView);
    }

    public void restartWeek() {
        int postion = getCurrentItem();//当前的
        WeekView weekView = mWeekAdapter.getViews().get(postion);
        weekView.invalidate();
    }
}

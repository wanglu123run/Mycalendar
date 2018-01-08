package com.calendar.schedule;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.application.mycalendar.R;
import com.calendar.CalendarUtils;
import com.calendar.OnCalendarClickListener;
import com.calendar.month.MonthCalendarView;
import com.calendar.month.MonthView;
import com.calendar.week.WeekCalendarView;
import com.calendar.week.WeekView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Jimmy on 2016/10/7 0007.
 */
public class ScheduleLayout extends FrameLayout {

    private final int DEFAULT_MONTH = 0;
    private final int DEFAULT_WEEK = 1;

    private MonthCalendarView mcvCalendar;
    private WeekCalendarView wcvCalendar;
    private RelativeLayout rlMonthCalendar;
    private RelativeLayout rlScheduleList;
    private ScheduleRecyclerView rvScheduleList;

    private int mCurrentSelectYear;
    private int mCurrentSelectMonth;
    private int mCurrentSelectDay;
    private int mRowSize;
    private int mMinDistance;
    private int mAutoScrollDistance;
    private int mDefaultView;
    private float mDownPosition[] = new float[2];
    private boolean mIsScrolling = false;
    private boolean mIsAutoChangeMonthRow;
    private boolean mCurrentRowsIsSix = true;

    private ScheduleState mState;
    private OnCalendarClickListener mOnCalendarClickListener;
    private GestureDetector mGestureDetector;
    private TextView tv_schedule_hint;

    public ScheduleLayout(Context context) {
        this(context, null);
    }

    public ScheduleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScheduleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.ScheduleLayout));
        initDate();
        initGestureDetector();
    }

    private void initAttrs(TypedArray array) {
        mDefaultView = array.getInt(R.styleable.ScheduleLayout_default_view, DEFAULT_MONTH);
        mIsAutoChangeMonthRow = array.getBoolean(R.styleable.ScheduleLayout_auto_change_month_row, false);
        mState = ScheduleState.OPEN;
        mRowSize = getResources().getDimensionPixelSize(R.dimen.week_calendar_height);
        mMinDistance = getResources().getDimensionPixelSize(R.dimen.calendar_min_distance);
        mAutoScrollDistance = getResources().getDimensionPixelSize(R.dimen.auto_scroll_distance);
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new OnScheduleScrollListener(this));
    }

    private void initDate() {
        Calendar calendar = Calendar.getInstance();
        resetCurrentSelectDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mcvCalendar = (MonthCalendarView) findViewById(R.id.mcvCalendar);
        wcvCalendar = (WeekCalendarView) findViewById(R.id.wcvCalendar);
        rlMonthCalendar = (RelativeLayout) findViewById(R.id.rlMonthCalendar);
        rlScheduleList = (RelativeLayout) findViewById(R.id.rlScheduleList);
        rvScheduleList = (ScheduleRecyclerView) findViewById(R.id.rvScheduleList);
        tv_schedule_hint = (TextView) findViewById(R.id.tv_schedule_hint);
        bindingMonthAndWeekCalendar();
    }

    /**
     * 设置记录状态 进行布局的显示与隐藏
     * @param b
     */
    public void setRecordState(boolean b) {
        if (b) {
            //有记录
            rvScheduleList.setVisibility(View.VISIBLE);
            tv_schedule_hint.setVisibility(View.GONE);
        }else {
            //无记录
            rvScheduleList.setVisibility(View.GONE);
            tv_schedule_hint.setVisibility(View.VISIBLE);
        }

    }


    private void bindingMonthAndWeekCalendar() {
        mcvCalendar.setOnCalendarClickListener(mMonthCalendarClickListener);
        wcvCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
        wcvCalendar.setMonthJumpListener(new WeekCalendarView.MonthJumpListener() {
            @Override
            public void weekJumpMonthBack() {
                jumpMonthBack();
            }
            @Override
            public void weekJumpMonthNext() {
                jumpMonthNext();
            }
        });
        // 初始化视图
        Calendar calendar = Calendar.getInstance();
        if (mIsAutoChangeMonthRow) {
            mCurrentRowsIsSix = CalendarUtils.getMonthRows(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)) == 6;
        }
        if (mDefaultView == DEFAULT_MONTH) {
            wcvCalendar.setVisibility(INVISIBLE);
            mState = ScheduleState.OPEN;
            if (!mCurrentRowsIsSix) {
                rlScheduleList.setY(rlScheduleList.getY() - mRowSize);
            }
        } else if (mDefaultView == DEFAULT_WEEK) {
            wcvCalendar.setVisibility(VISIBLE);
            mState = ScheduleState.CLOSE;
            int row = CalendarUtils.getWeekRow(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            rlMonthCalendar.setY(-row * mRowSize);
            rlScheduleList.setY(rlScheduleList.getY() - 5 * mRowSize);
        }
    }

    private void resetCurrentSelectDate(int year, int month, int day) {
        mCurrentSelectYear = year;
        mCurrentSelectMonth = month;
        mCurrentSelectDay = day;
    }

    private OnCalendarClickListener mMonthCalendarClickListener = new OnCalendarClickListener() {
        @Override
        public void onClickDate(int year, int month, int day) {
            wcvCalendar.setOnCalendarClickListener(null);
            int weeks = CalendarUtils.getWeeksAgo(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay, year, month, day);
            resetCurrentSelectDate(year, month, day);
            int position = wcvCalendar.getCurrentItem() + weeks;
            if (weeks != 0) {
                wcvCalendar.setCurrentItem(position, false);
            }
            resetWeekView(position);
            wcvCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
        }

        @Override
        public void onPageChange(int year, int month, int day) {
            if (mOnCalendarClickListener != null) {
                mOnCalendarClickListener.onPageChange(year, month, day);
            }
            computeCurrentRowsIsSix(year, month);
        }
    };




    /**
     * 计算并设置月份的行高
     * @param year
     * @param month
     */
    private void computeCurrentRowsIsSix(int year, int month) {
        boolean isSixRow = CalendarUtils.getMonthRows(year, month) == 6;
        if (mIsAutoChangeMonthRow) {
            if (mCurrentRowsIsSix != isSixRow) {
                mCurrentRowsIsSix = isSixRow;
                if (mCurrentRowsIsSix) {
//                    AutoMoveAnimation animation = new AutoMoveAnimation(rlScheduleList, mRowSize);
//                    rlScheduleList.startAnimation(animation);
                    float mPositionY = rlScheduleList.getY();
                    rlScheduleList.setY(mPositionY + mRowSize);
                } else {
//                    AutoMoveAnimation animation = new AutoMoveAnimation(rlScheduleList, -mRowSize);
//                    rlScheduleList.startAnimation(animation);
                    float mPositionY = rlScheduleList.getY();
                    rlScheduleList.setY(mPositionY + -mRowSize);
                }
            }
        }
    }

    private void resetWeekView(int position) {
        WeekView weekView = wcvCalendar.getCurrentWeekView();
        if (weekView != null) {
            weekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            weekView.invalidate();
        } else {
            WeekView newWeekView = wcvCalendar.getWeekAdapter().instanceWeekView(position);
            newWeekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            newWeekView.invalidate();
            wcvCalendar.setCurrentItem(position);
        }
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        }
    }



    private OnCalendarClickListener mWeekCalendarClickListener = new OnCalendarClickListener() {
        @Override
        public void onClickDate(int year, int month, int day) {
            mcvCalendar.setOnCalendarClickListener(null);
            int months = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year, month);
            resetCurrentSelectDate(year, month, day);
            if (mCurrentSelectMonth != month) {
                newComputeCurrentRowsIsSix(year,month);
            }
            if (months != 0) {
                int position = mcvCalendar.getCurrentItem() + months;
                mcvCalendar.setCurrentItem(position, false);
            }
            resetMonthView();
            mcvCalendar.setOnCalendarClickListener(mMonthCalendarClickListener);
        }

        @Override
        public void onPageChange(int year, int month, int day) {
            if (mIsAutoChangeMonthRow) {
                if (mCurrentSelectMonth != month) {
                    mCurrentRowsIsSix = CalendarUtils.getMonthRows(year, month) == 6;
                }
            }
            if (mOnCalendarClickListener != null) {
                mOnCalendarClickListener.onPageChange(year, month, day);
            }
        }
    };


    private boolean isWeekPagerMonth = true;
    /**
     * 跳转月份
     * @param year
     * @param month
     * @param day
     */
    private void meek2Month(int year, int month, int day) {
        if (!isWeekPagerMonth) {
            return;
        }
        if (day == 1) {
            return;
        }
        if (year == mCurrentSelectYear) {
            //同年
            if (month > mCurrentSelectMonth || day > mCurrentSelectDay) {
                //下个月
                nextMonth();
            } else {
                //上个月
                backMonth();
            }
        } else {
            //不同年
            if (year > mCurrentSelectYear) {
                //下一年
                nextMonth();
            } else {
                //上一年
                backMonth();
            }
        }
    }


    /**
     * 计算并设置月份的行高
     * @param year
     * @param month
     */
    private void newComputeCurrentRowsIsSix(int year, int month) {
        if (mCurrentSelectMonth != month) {
            mCurrentRowsIsSix = CalendarUtils.getMonthRows(year, month) == 6;
        }
        boolean isSixRow = CalendarUtils.getMonthRows(year, month) == 6;
        if (mIsAutoChangeMonthRow) {
            if (mCurrentRowsIsSix != isSixRow) {
                mCurrentRowsIsSix = isSixRow;
                if (mCurrentRowsIsSix) {
//                    AutoMoveAnimation animation = new AutoMoveAnimation(rlScheduleList, mRowSize,1);
//                    rlScheduleList.startAnimation(animation);
                    float mPositionY = rlScheduleList.getY();
                    rlScheduleList.setY(mPositionY + mRowSize);
                } else {
//                    AutoMoveAnimation animation = new AutoMoveAnimation(rlScheduleList, -mRowSize,1);
//                    rlScheduleList.startAnimation(animation);
                    float mPositionY = rlScheduleList.getY();
                    rlScheduleList.setY(mPositionY + -mRowSize);
                }
            }
        }
    }

    private void resetMonthView() {
        MonthView monthView = mcvCalendar.getCurrentMonthView();
        if (monthView != null) {
            monthView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            monthView.invalidate();
        }
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        resetViewHeight(rlScheduleList, height - mRowSize);
        resetViewHeight(this, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void resetViewHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.height != height) {
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownPosition[0] = ev.getRawX();
                mDownPosition[1] = ev.getRawY();
                mGestureDetector.onTouchEvent(ev);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsScrolling) {
            return true;
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float x = ev.getRawX();
                float y = ev.getRawY();
                float distanceX = Math.abs(x - mDownPosition[0]);
                float distanceY = Math.abs(y - mDownPosition[1]);
                if (distanceY > mMinDistance && distanceY > distanceX * 2.0f) {
                    return (y > mDownPosition[1] && isRecyclerViewTouch()) || (y < mDownPosition[1] && mState == ScheduleState.OPEN);
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isRecyclerViewTouch() {
        return mState == ScheduleState.CLOSE && (rvScheduleList.getChildCount() == 0 || rvScheduleList.isScrollTop());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownPosition[0] = event.getRawX();
                mDownPosition[1] = event.getRawY();
                resetCalendarPosition();
                return true;
            case MotionEvent.ACTION_MOVE:
                transferEvent(event);
                mIsScrolling = true;
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                transferEvent(event);
                changeCalendarState();
                resetScrollingState();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void transferEvent(MotionEvent event) {
        if (mState == ScheduleState.CLOSE) {
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
            mGestureDetector.onTouchEvent(event);
        } else {
            mGestureDetector.onTouchEvent(event);
        }
    }

    private void changeCalendarState() {
        if (rlScheduleList.getY() > mRowSize * 2 &&
                rlScheduleList.getY() < mcvCalendar.getHeight() - mRowSize) { // 位于中间
            ScheduleAnimation animation = new ScheduleAnimation(this, mState, mAutoScrollDistance);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    changeState();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        } else if (rlScheduleList.getY() <= mRowSize * 2) { // 位于顶部
            ScheduleAnimation animation = new ScheduleAnimation(this, ScheduleState.OPEN, mAutoScrollDistance);
            animation.setDuration(50);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mState == ScheduleState.OPEN) {
                        changeState();
                    } else {
                        resetCalendar();
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            rlScheduleList.startAnimation(animation);
        } else {
            ScheduleAnimation animation = new ScheduleAnimation(this, ScheduleState.CLOSE, mAutoScrollDistance);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mState == ScheduleState.CLOSE) {
                        mState = ScheduleState.OPEN;
                        /**
                         * 显示出月
                         */
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            rlScheduleList.startAnimation(animation);
        }
    }


    private void resetCalendarPosition() {
        if (mState == ScheduleState.OPEN) {
            rlMonthCalendar.setY(0);
            if (mCurrentRowsIsSix) {
                rlScheduleList.setY(mcvCalendar.getHeight());
            } else {
                rlScheduleList.setY(mcvCalendar.getHeight() - mRowSize);
            }
        } else {
            rlMonthCalendar.setY(-CalendarUtils.getWeekRow(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay) * mRowSize);
            rlScheduleList.setY(mRowSize);
        }
    }

    private void resetCalendar() {
        if (mState == ScheduleState.OPEN) {
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
        } else {
            mcvCalendar.setVisibility(INVISIBLE);
            wcvCalendar.setVisibility(VISIBLE);
        }
        setMonthHintMap(mMonthHintMap);
    }


    private void changeState() {
        if (mState == ScheduleState.OPEN) {
            mState = ScheduleState.CLOSE;
            mcvCalendar.setVisibility(INVISIBLE);
            wcvCalendar.setVisibility(VISIBLE);
            rlMonthCalendar.setY((1 - mcvCalendar.getCurrentMonthView().getWeekRow()) * mRowSize);
        } else {
            mState = ScheduleState.OPEN;
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
            rlMonthCalendar.setY(0);
        }
        setMonthHintMap(mMonthHintMap);
    }

    private void resetScrollingState() {
        mDownPosition[0] = 0;
        mDownPosition[1] = 0;
        mIsScrolling = false;
    }

    protected void onCalendarScroll(float distanceY) {
        MonthView monthView = mcvCalendar.getCurrentMonthView();
        distanceY = Math.min(distanceY, mAutoScrollDistance);
        float calendarDistanceY = distanceY / (mCurrentRowsIsSix ? 5.0f : 4.0f);
//        int row = monthView.getWeekRow() - 1;//当前点击的第几行
        int row = CalendarUtils.getWeekRow(monthView.getSelectYear(), monthView.getSelectMonth(), monthView.getSelectDay());

        int calendarTop = -row * mRowSize;
        int scheduleTop = mRowSize;
        float calendarY = rlMonthCalendar.getY() - calendarDistanceY * row;
        calendarY = Math.min(calendarY, 0);
        calendarY = Math.max(calendarY, calendarTop);
        rlMonthCalendar.setY(calendarY);
        float scheduleY = rlScheduleList.getY() - distanceY;
        if (mCurrentRowsIsSix) {
            scheduleY = Math.min(scheduleY, mcvCalendar.getHeight());
        } else {
            scheduleY = Math.min(scheduleY, mcvCalendar.getHeight() - mRowSize);
        }
        scheduleY = Math.max(scheduleY, scheduleTop);
        rlScheduleList.setY(scheduleY);
    }

    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }

    private void resetMonthViewDate(final int year, final int month, final int day, final int position) {
        if (mcvCalendar.getMonthViews().get(position) == null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetMonthViewDate(year, month, day, position);
                }
            }, 50);
        } else {
            mcvCalendar.getMonthViews().get(position).clickThisMonth(year, month, day);
        }
    }

    /**
     * 初始化年月日
     *
     * @param year
     * @param month (0-11)
     * @param day   (1-31)
     */
    public void initData(int year, int month, int day) {
        int monthDis = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year, month);
        int position = mcvCalendar.getCurrentItem() + monthDis;
        mcvCalendar.setCurrentItem(position);
        resetMonthViewDate(year, month, day, position);
    }


    /**
     * 下个月
     */
    public void nextMonth() {
        MonthView currentMonthView = mcvCalendar.getCurrentMonthView();
        int selectYear = currentMonthView.getSelectYear();
        int selectMonth = currentMonthView.getSelectMonth();
        //月份0-11
        int year=selectYear;
        int month=selectMonth;
        if (selectMonth == 11) {
            month = 0;
            year=selectYear+1;
        } else {
            month=selectMonth+1;
        }
        boolean b = compareDate(2019, 6, 1, year, month, 1);
        if (b) {
            return;
        }
        if (mState == ScheduleState.OPEN) {
            //月份开
            int monthDis = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year,month);
            int position = mcvCalendar.getCurrentItem() + monthDis;
            mcvCalendar.setCurrentItem(position);
        } else {
            //月份关   周的下个月
            wcvCalendar.setOnCalendarClickListener(null);
            wcvCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
            int weeks = CalendarUtils.getWeeksAgo(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay, year, month, 1);
            int weekPosition = wcvCalendar.getCurrentItem() + weeks;
//            if (weeks != 0) {
//                wcvCalendar.setCurrentItem(weekPosition);
//            }
            wcvCalendar.setCurrentItem(weekPosition);
            WeekView currentWeekView = wcvCalendar.getCurrentWeekView();
            currentWeekView.clickThisWeek(year, month, 1);
        }
    }

    /**
     * 0是否比1小   谁大谁往后
     * @param year0
     * @param month0
     * @param day0
     * @param year1
     * @param month1
     * @param day1
     * @return
     */
    private boolean compareDate(int year0, int month0, int day0, int year1, int month1, int day1) {
        if (year0 < year1) {
            return true;
        }
        if (year0 > year1) {
            return false;
        }
        if (year0 == year1) {
            //年相等
            if (month0 < month1) {
                return true;
            }
            if (month0 > month1) {
                return false;
            }
            if (month0 == month1) {
                if (day0 < day1) {
                    return true;
                }
                if (day0 > day1) {
                    return false;
                }
                if (day0==day1) {
                    return true;
                }
            }
        }
        return true;
    }


    /**
     * 上个月
     */
    public void backMonth() {
        MonthView currentMonthView = mcvCalendar.getCurrentMonthView();
        int selectYear = currentMonthView.getSelectYear();
        int selectMonth = currentMonthView.getSelectMonth();
        int year=selectYear;
        int month=selectMonth;
        if (selectMonth == 0) {
            month = 11;
            year=selectYear-1;
        } else {
            month=selectMonth-1;
        }
        boolean b = compareDate(2015, 4, 1, year, month, 1);
        if (!b) {
            return;
        }
        if (mState == ScheduleState.OPEN) {
            //月份开
            int monthDis = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year, month);
            int position = mcvCalendar.getCurrentItem() + monthDis;
            mcvCalendar.setCurrentItem(position);
        } else {
            //月份关 周的下个月
            wcvCalendar.setOnCalendarClickListener(null);
            wcvCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
            int weeks = CalendarUtils.getWeeksAgo(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay, year, month, 1);
            int weekPosition = wcvCalendar.getCurrentItem() + weeks;
//            if (weeks != 0) {
//                wcvCalendar.setCurrentItem(weekPosition);
//            }
            wcvCalendar.setCurrentItem(weekPosition);
            WeekView currentWeekView = wcvCalendar.getCurrentWeekView();
            currentWeekView.clickThisWeek(year, month, 1);
        }
    }


    private void jumpMonthBack() {
        MonthView currentMonthView = mcvCalendar.getCurrentMonthView();
        int selectYear = currentMonthView.getSelectYear();
        int selectMonth = currentMonthView.getSelectMonth();
        int row = CalendarUtils.getMonthRows(selectYear, selectMonth);
        mState = ScheduleState.OPEN;
        mcvCalendar.setVisibility(VISIBLE);
        wcvCalendar.setVisibility(INVISIBLE);
//        float mPositionY = rlScheduleList.getY();
        rlScheduleList.setY(mRowSize*row);
        rlMonthCalendar.setY(0);
        backMonth();
    }

    private void jumpMonthNext() {
        MonthView currentMonthView = mcvCalendar.getCurrentMonthView();
        int selectYear = currentMonthView.getSelectYear();
        int selectMonth = currentMonthView.getSelectMonth();
        int row = CalendarUtils.getMonthRows(selectYear, selectMonth);
        mState = ScheduleState.OPEN;
        mcvCalendar.setVisibility(VISIBLE);
        wcvCalendar.setVisibility(INVISIBLE);
        //        float mPositionY = rlScheduleList.getY();
        rlScheduleList.setY(mRowSize*row);
        rlMonthCalendar.setY(0);
        nextMonth();
    }

    public ScheduleRecyclerView getSchedulerRecyclerView() {
        return rvScheduleList;
    }

    public MonthCalendarView getMonthCalendar() {
        return mcvCalendar;
    }

    public WeekCalendarView getWeekCalendar() {
        return wcvCalendar;
    }

    public int getCurrentSelectYear() {
        return mCurrentSelectYear;
    }

    public int getCurrentSelectMonth() {
        return mCurrentSelectMonth;
    }

    public int getCurrentSelectDay() {
        return mCurrentSelectDay;
    }



    /**
     * 圆点的集合
     */
    private Map<String, List<String>> mMonthHintMap;//Map<日期,List<Object>> 月视图
    /**
     * 设置圆点提示集合
     * @param maphint
     */
    public void setMonthHintMap(Map<String,List<String>> maphint) {
        mMonthHintMap=maphint;
        if (mcvCalendar == null) {
            return;
        }
        mcvCalendar.setMonthHintMap(maphint);
        if (wcvCalendar == null) {
            return;
        }
        wcvCalendar.setWeekHintMap(maphint);
    }
}

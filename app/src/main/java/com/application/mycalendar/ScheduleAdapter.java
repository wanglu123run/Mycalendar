package com.application.mycalendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;


/**
 * 新的日程列表
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleCenterViewHolder> {

    private Context mContext;
    private List<String> mData;
    private List<String> comments;

    public ScheduleAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<String> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    public void clearData() {
        if (mData != null) {
            mData.clear();
        }
    }


    @Override
    public ScheduleCenterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.myschedule_activity_listview_item, parent, false);
        return new ScheduleCenterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ScheduleCenterViewHolder holder, int position) {
        setViewHolder(holder,position);
    }

    private void setViewHolder(final ScheduleCenterViewHolder viewHolder, int position) {
        //内容
    }

    @Override
    public int getItemCount() {
        return mData==null?0:mData.size();
    }


    protected class ScheduleCenterViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvmyscheduleitemtime;
        public final TextView tvmyscheduleitemampm;
        public final LinearLayout llmyscheduleitemtimelayout;
        public final View viewmyscheduleline1;
        public final ImageView ivmyscheduleitemicon;
        public final TextView tvmyscheduleitemtitle;
        public final RelativeLayout rlmyscheduleitemdesc;
        public final View root;

        public ScheduleCenterViewHolder(View itemView) {
            super(itemView);
            this.root = itemView;
            tvmyscheduleitemtime = (TextView) root.findViewById(R.id.tv_myscheduleitem_time);
            tvmyscheduleitemampm = (TextView) root.findViewById(R.id.tv_myscheduleitem_ampm);
            llmyscheduleitemtimelayout = (LinearLayout) root.findViewById(R.id.ll_myscheduleitem_timelayout);
            viewmyscheduleline1 = (View) root.findViewById(R.id.view_myschedule_line1);
            ivmyscheduleitemicon = (ImageView) root.findViewById(R.id.iv_myscheduleitem_icon);
            tvmyscheduleitemtitle = (TextView) root.findViewById(R.id.tv_myscheduleitem_title);
            rlmyscheduleitemdesc = (RelativeLayout) root.findViewById(R.id.rl_myscheduleitem_desc);
        }
    }
}

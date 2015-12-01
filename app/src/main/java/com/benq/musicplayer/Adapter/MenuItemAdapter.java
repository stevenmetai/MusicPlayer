package com.benq.musicplayer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.benq.musicplayer.R;

import java.util.ArrayList;

/**
 * Created by Steven.SL.Tai on 2015/11/10.
 */
public class MenuItemAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> mTitleList;
    private int mSelectedPos = -1;

    public MenuItemAdapter(Context context) {
        mContext = context;
        mTitleList = new ArrayList<>();
        mTitleList.add(mContext.getString(R.string.album));
        mTitleList.add(mContext.getString(R.string.artist));
        mTitleList.add(mContext.getString(R.string.track));
    }

    @Override
    public int getCount() {
        return mTitleList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.nav_item, parent, false);
            viewHolder.bar = view.findViewById(R.id.side_bar);
            viewHolder.title = (TextView) view.findViewById(R.id.textView);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.title.setText(mTitleList.get(position));
        if (mSelectedPos != position) {
            viewHolder.bar.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.bar.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public void setSelectedPos(int pos) {
        mSelectedPos = pos;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        View bar;
        TextView title;
    }
}

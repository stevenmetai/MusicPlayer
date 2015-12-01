package com.benq.musicplayer.Adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.benq.musicplayer.R;

import java.util.ArrayList;

/**
 * Created by Steven.SL.Tai on 2015/11/24.
 */
public class CategorySpinnerAdapter implements SpinnerAdapter{

    private Context mContext;
    private ArrayList<String> mTitleList;

    public CategorySpinnerAdapter(Context context) {
        mContext = context;
        mTitleList = new ArrayList<>();
        mTitleList.add(mContext.getString(R.string.album));
        mTitleList.add(mContext.getString(R.string.artist));
        mTitleList.add(mContext.getString(R.string.track));
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.spinner_item, parent, false);
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.title.setText(mTitleList.get(position));
        return view;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return mTitleList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTitleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.spinner_choose_item, parent, false);
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.title.setText(mTitleList.get(position));
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    class ViewHolder {
        TextView title;
    }
}

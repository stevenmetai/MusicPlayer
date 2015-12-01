package com.benq.musicplayer.Adapter;

import android.database.Cursor;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

/**
 * Created by Steven.SL.Tai on 2015/11/5.
 */
public abstract class BaseExpandListSwapAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener {
    public abstract boolean swapCursor(Cursor cursor);
    public abstract void updateCurrentPlay(long id);
}
